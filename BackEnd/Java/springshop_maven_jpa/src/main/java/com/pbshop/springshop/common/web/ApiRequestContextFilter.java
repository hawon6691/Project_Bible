package com.pbshop.springshop.common.web;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ApiRequestContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String requestId = request.getHeader("X-Request-Id");

        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        String locale = resolveLocale(request.getHeader("Accept-Language"));

        ApiRequestContext context = new ApiRequestContext(requestId, locale, "KRW");
        request.setAttribute(ApiRequestContext.ATTRIBUTE_NAME, context);
        response.setHeader("X-Request-Id", requestId);

        filterChain.doFilter(request, response);
    }

    private String resolveLocale(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isBlank()) {
            return "ko-KR";
        }

        Locale locale = Locale.forLanguageTag(acceptLanguage.split(",")[0].trim());

        if (locale.getCountry().isBlank()) {
            return switch (locale.getLanguage().toLowerCase()) {
                case "en" -> "en-US";
                case "ja" -> "ja-JP";
                default -> "ko-KR";
            };
        }

        return locale.toLanguageTag();
    }
}
