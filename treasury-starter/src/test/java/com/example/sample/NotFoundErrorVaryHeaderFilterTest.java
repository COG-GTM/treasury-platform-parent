package com.example.sample;

import com.harborline.treasury.platform.web.NotFoundErrorVaryHeaderFilter;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class NotFoundErrorVaryHeaderFilterTest {

    private final NotFoundErrorVaryHeaderFilter filter = new NotFoundErrorVaryHeaderFilter();

    @Test
    void appendsOriginWithoutReplacingExistingVaryValues() throws Exception {
        MockHttpServletRequest request = notFoundErrorRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.addHeader("Vary", "Accept-Language");

        filter.doFilter(request, response, (servletRequest, servletResponse) -> { });

        assertThat(response.getHeaders("Vary")).containsExactly("Accept-Language", "Origin");
    }

    @Test
    void doesNotDuplicateOriginAlreadyListedInVaryValues() throws Exception {
        MockHttpServletRequest request = notFoundErrorRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.addHeader("Vary", "Accept-Language, oRiGiN");

        filter.doFilter(request, response, (servletRequest, servletResponse) -> { });

        assertThat(response.getHeaders("Vary")).containsExactly("Accept-Language, oRiGiN");
    }

    private MockHttpServletRequest notFoundErrorRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setDispatcherType(DispatcherType.ERROR);
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 404);
        return request;
    }
}
