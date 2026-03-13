package org.joint.common.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.joint.common.annotation.Log;
import org.joint.common.enums.BusinessType;
import org.joint.common.security.LoginUser;
import org.joint.common.utils.IpUtils;
import org.joint.modules.system.operlog.entity.OperLog;
import org.joint.modules.system.operlog.mapper.OperLogMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private static final ThreadLocal<Long> START_TIME = new ThreadLocal<>();
    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "password",
            "oldPassword",
            "newPassword",
            "confirmPassword",
            "token",
            "accessToken",
            "refreshToken",
            "secret",
            "apiKey"
    );

    private final OperLogMapper operLogMapper;
    private final ObjectMapper objectMapper;

    @Pointcut("@annotation(org.joint.common.annotation.Log)")
    public void logPointcut() {
    }

    @Before("logPointcut()")
    public void doBefore() {
        START_TIME.set(System.currentTimeMillis());
    }

    @AfterReturning(pointcut = "logPointcut()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {
        handleLog(joinPoint, null, result);
    }

    @AfterThrowing(pointcut = "logPointcut()", throwing = "exception")
    public void doAfterThrowing(JoinPoint joinPoint, Exception exception) {
        handleLog(joinPoint, exception, null);
    }

    private void handleLog(JoinPoint joinPoint, Exception exception, Object result) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Log logAnnotation = method.getAnnotation(Log.class);
            if (logAnnotation == null) {
                return;
            }

            LoginUser loginUser = getLoginUser();
            if (loginUser == null) {
                return;
            }

            OperLog operLog = new OperLog();
            operLog.setTitle(logAnnotation.module());
            operLog.setBusinessType(logAnnotation.type().getCode());
            operLog.setMethod(signature.getDeclaringType().getSimpleName() + "." + method.getName() + "()");
            operLog.setOperName(loginUser.getUsername());
            operLog.setOperLocation("");
            operLog.setOperTime(LocalDateTime.now());

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes == null ? null : attributes.getRequest();
            if (request != null) {
                operLog.setRequestMethod(request.getMethod());
                operLog.setOperUrl(request.getRequestURI());
                operLog.setOperIp(IpUtils.getClientIp(request));
            }

            if (logAnnotation.saveRequestData()) {
                operLog.setOperParam(serialize(joinPoint.getArgs()));
            }
            if (logAnnotation.saveResponseData() && logAnnotation.type() != BusinessType.EXPORT && result != null) {
                operLog.setJsonResult(serialize(result));
            }

            if (exception != null) {
                operLog.setStatus(1);
                operLog.setErrorMsg(truncate(exception.getMessage(), 2000));
            } else {
                operLog.setStatus(0);
            }

            Long startTime = START_TIME.get();
            if (startTime != null) {
                operLog.setCostTime(System.currentTimeMillis() - startTime);
            }
            operLogMapper.insert(operLog);
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
        } finally {
            START_TIME.remove();
        }
    }

    private LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser;
        }
        return null;
    }

    private String serialize(Object value) {
        try {
            JsonNode tree = objectMapper.valueToTree(value);
            sanitize(tree);
            return truncate(objectMapper.writeValueAsString(tree), 2000);
        } catch (Exception exception) {
            return "序列化失败";
        }
    }

    private void sanitize(JsonNode node) {
        if (node == null) {
            return;
        }
        if (node instanceof ObjectNode objectNode) {
            for (var entry : objectNode.properties()) {
                if (SENSITIVE_FIELDS.contains(entry.getKey())) {
                    objectNode.put(entry.getKey(), "******");
                    continue;
                }
                sanitize(entry.getValue());
            }
            return;
        }
        if (node instanceof ArrayNode arrayNode) {
            for (JsonNode child : arrayNode) {
                sanitize(child);
            }
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}
