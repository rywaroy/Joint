package org.joint.common.security;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.joint.common.annotation.Logical;
import org.joint.common.annotation.RequirePermission;
import org.joint.common.exception.BusinessException;
import org.joint.common.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Set;

@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private final PermissionService permissionService;

    @Before("@annotation(org.joint.common.annotation.RequirePermission)")
    public void checkPermission(JoinPoint joinPoint) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (loginUser.getRoles().contains("admin")) {
            return;
        }

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        RequirePermission annotation = method.getAnnotation(RequirePermission.class);
        Set<String> permissions = permissionService.getUserPermissions(loginUser.getUserId());

        boolean hasPermission = annotation.logical() == Logical.AND;
        for (String required : annotation.value()) {
            boolean contains = permissions.contains(required);
            if (annotation.logical() == Logical.AND && !contains) {
                hasPermission = false;
                break;
            }
            if (annotation.logical() == Logical.OR && contains) {
                hasPermission = true;
                break;
            }
        }

        if (!hasPermission) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
