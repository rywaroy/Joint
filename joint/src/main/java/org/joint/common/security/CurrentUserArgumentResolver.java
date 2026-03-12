package org.joint.common.security;

import lombok.RequiredArgsConstructor;
import org.joint.common.annotation.CurrentUser;
import org.joint.modules.system.user.UserService;
import org.joint.modules.system.user.entity.User;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final UserService userService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            return null;
        }

        Class<?> parameterType = parameter.getParameterType();
        if (parameterType.equals(String.class)) {
            return loginUser.getUserId();
        }
        if (parameterType.equals(LoginUser.class)) {
            return loginUser;
        }
        if (parameterType.equals(User.class)) {
            return userService.findById(loginUser.getUserId());
        }
        return null;
    }
}
