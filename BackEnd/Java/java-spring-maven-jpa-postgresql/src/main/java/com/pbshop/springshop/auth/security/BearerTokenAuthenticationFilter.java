package com.pbshop.springshop.auth.security;

import java.io.IOException;
import java.time.OffsetDateTime;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.pbshop.springshop.auth.AuthAccessToken;
import com.pbshop.springshop.auth.AuthAccessTokenRepository;
import com.pbshop.springshop.user.UserRepository;

@Component
public class BearerTokenAuthenticationFilter extends OncePerRequestFilter {

    private final AuthAccessTokenRepository accessTokenRepository;
    private final UserRepository userRepository;

    public BearerTokenAuthenticationFilter(AuthAccessTokenRepository accessTokenRepository, UserRepository userRepository) {
        this.accessTokenRepository = accessTokenRepository;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            accessTokenRepository.findByTokenAndRevokedAtIsNullAndExpiresAtAfter(token, OffsetDateTime.now())
                    .flatMap(this::toPrincipal)
                    .ifPresent(principal -> SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities())
                    ));
        }

        filterChain.doFilter(request, response);
    }

    private java.util.Optional<AuthenticatedUserPrincipal> toPrincipal(AuthAccessToken token) {
        return userRepository.findById(token.getUserId())
                .filter(user -> "ACTIVE".equalsIgnoreCase(user.getStatus()))
                .map(user -> new AuthenticatedUserPrincipal(user.getId(), user.getEmail(), user.getRole(), token.getToken()));
    }
}
