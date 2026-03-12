package org.joint.common.aspect;

import org.joint.common.annotation.Log;
import org.joint.common.enums.BusinessType;
import org.joint.common.security.LoginUser;
import org.joint.modules.system.operlog.entity.OperLog;
import org.joint.modules.system.operlog.mapper.OperLogMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LogAspectTest {

    private final OperLogMapper operLogMapper = mock(OperLogMapper.class);
    private final LogAspect logAspect = new LogAspect(operLogMapper, new ObjectMapper());

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        SecurityContextHolder.clearContext();
    }

    @Test
    void annotatedMethodCreatesSuccessLog() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/system/user");
        request.setRemoteAddr("127.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new LoginUser("u-1", "admin", java.util.List.of("admin")),
                "token"
        ));

        SampleService target = new SampleService();
        AspectJProxyFactory proxyFactory = new AspectJProxyFactory(target);
        proxyFactory.addAspect(logAspect);
        SampleService proxy = proxyFactory.getProxy();

        proxy.create("alice");

        ArgumentCaptor<OperLog> captor = ArgumentCaptor.forClass(OperLog.class);
        verify(operLogMapper).insert(captor.capture());
        assertThat(captor.getValue().getModule()).isEqualTo("用户管理");
        assertThat(captor.getValue().getBusinessType()).isEqualTo(BusinessType.INSERT.name());
        assertThat(captor.getValue().getStatus()).isEqualTo(0);
        assertThat(captor.getValue().getOperatorName()).isEqualTo("admin");
        assertThat(captor.getValue().getRequestUrl()).isEqualTo("/system/user");
    }

    @Test
    void annotatedMethodCreatesFailureLog() {
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/system/user/u-1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        SampleService target = new SampleService();
        AspectJProxyFactory proxyFactory = new AspectJProxyFactory(target);
        proxyFactory.addAspect(logAspect);
        SampleService proxy = proxyFactory.getProxy();

        assertThatThrownBy(proxy::delete)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");

        ArgumentCaptor<OperLog> captor = ArgumentCaptor.forClass(OperLog.class);
        verify(operLogMapper).insert(captor.capture());
        assertThat(captor.getValue().getBusinessType()).isEqualTo(BusinessType.DELETE.name());
        assertThat(captor.getValue().getStatus()).isEqualTo(1);
        assertThat(captor.getValue().getErrorMsg()).contains("boom");
        assertThat(captor.getValue().getRequestMethod()).isEqualTo("DELETE");
    }

    static class SampleService {

        @Log(module = "用户管理", type = BusinessType.INSERT, description = "创建用户")
        public String create(String username) {
            return username;
        }

        @Log(module = "用户管理", type = BusinessType.DELETE, description = "删除用户")
        public void delete() {
            throw new IllegalStateException("boom");
        }
    }
}
