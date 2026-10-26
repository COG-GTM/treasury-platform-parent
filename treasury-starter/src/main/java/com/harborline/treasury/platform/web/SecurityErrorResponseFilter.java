package com.harborline.treasury.platform.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.IOException;

public class SecurityErrorResponseFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse wrappedResponse = new HttpServletResponseWrapper((HttpServletResponse) response) {
            @Override
            public void sendError(int status) throws IOException {
                if (isSecurityStatus(status)) {
                    setStatus(status);
                } else {
                    super.sendError(status);
                }
            }

            @Override
            public void sendError(int status, String message) throws IOException {
                if (isSecurityStatus(status)) {
                    setStatus(status);
                } else {
                    super.sendError(status, message);
                }
            }

            private boolean isSecurityStatus(int status) {
                return status == HttpServletResponse.SC_UNAUTHORIZED || status == HttpServletResponse.SC_FORBIDDEN;
            }
        };
        chain.doFilter(request, wrappedResponse);
    }
}
