package com.pbshop.springshop.common.web;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public record ApiRequestContext(
        String requestId,
        String locale,
        String currency
) {
    public static final String ATTRIBUTE_NAME = ApiRequestContext.class.getName();

    public static ApiRequestContext current() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            HttpServletRequest request = servletRequestAttributes.getRequest();
            Object value = request.getAttribute(ATTRIBUTE_NAME);

            if (value instanceof ApiRequestContext context) {
                return context;
            }
        }

        return new ApiRequestContext("system", "ko-KR", "KRW");
    }
}
