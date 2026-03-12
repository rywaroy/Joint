package org.joint.common.aspect;

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
import org.joint.common.security.LoginUser;
import org.joint.common.utils.IpUtils;
import org.joint.modules.system.operlog.entity.OperLog;
import org.joint.modules.system.operlog.mapper.OperLogMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private static final ThreadLocal<Long> START_TIME = new ThreadLocal<>();

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

            OperLog operLog = new OperLog();
            operLog.setModule(logAnnotation.module());
            operLog.setBusinessType(logAnnotation.type().name());
            operLog.setDescription(logAnnotation.description());
            operLog.setMethod(joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
            operLog.setOperateTime(LocalDateTime.now());

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes == null ? null : attributes.getRequest();
            if (request != null) {
                operLog.setRequestMethod(request.getMethod());
                operLog.setRequestUrl(request.getRequestURI());
                operLog.setOperatorIp(IpUtils.getClientIp(request));
            }

            operLog.setRequestParams(serialize(joinPoint.getArgs()));
            if (result != null) {
                operLog.setResponseResult(serialize(result));
            }

            if (exception != null) {
                operLog.setStatus(1);
                operLog.setErrorMsg(truncate(exception.getMessage(), 2000));
            } else {
                operLog.setStatus(0);
            }

            LoginUser loginUser = getLoginUser();
            if (loginUser != null) {
                operLog.setOperatorId(loginUser.getUserId());
                operLog.setOperatorName(loginUser.getUsername());
            }

            Long startTime = START_TIME.get();
            if (startTime != null) {
                operLog.setCostTime(System.currentTimeMillis() - startTime);
            }
            saveLog(operLog);
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
        } finally {
            START_TIME.remove();
        }
    }

    public void saveLog(OperLog operLog) {
        operLogMapper.insert(operLog);
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
            return truncate(objectMapper.writeValueAsString(value), 2000);
        } catch (Exception exception) {
            return "序列化失败";
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}
