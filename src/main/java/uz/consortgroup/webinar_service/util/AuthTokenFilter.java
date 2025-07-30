package uz.consortgroup.webinar_service.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uz.consortgroup.webinar_service.security.AuthenticatedUser;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.info("JWT received: {}", token);

            if (jwtTokenProvider.validateToken(token)) {
                UUID userId = jwtTokenProvider.extractUserId(token);

                if (userId != null) {
                    AuthenticatedUser principal = new AuthenticatedUser(userId);
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(principal, null, List.of());
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    log.info("JWT valid, authenticated userId: {}", userId);
                } else {
                    log.warn("JWT valid, but userId extraction failed");
                }
            } else {
                log.warn("Invalid JWT token");
            }
        }

        filterChain.doFilter(request, response);
    }

}
