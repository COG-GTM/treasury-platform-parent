package com.harborline.treasury.platform.web;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class NotFoundErrorVaryHeaderFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        Object errorStatus = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (request.getDispatcherType() == DispatcherType.ERROR
                && errorStatus instanceof Integer
                && ((Integer) errorStatus) == HttpServletResponse.SC_NOT_FOUND) {
            ((HttpServletResponse) response).setHeader("Vary", "Origin");
        }
        chain.doFilter(request, response);
    }
}
