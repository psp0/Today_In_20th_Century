package place.run.mep.century20.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import place.run.mep.century20.config.TokenValidationResult;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // Skip filter for login endpoint
            if (request.getRequestURI().equals("/api/users/login")) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = jwtTokenProvider.resolveToken(request);
            
            if (token != null) {
                TokenValidationResult result = jwtTokenProvider.validateToken(token);
                if (result.isValid()) {
                    Authentication authentication = jwtTokenProvider.getAuthentication(token);
                    if (authentication instanceof org.springframework.security.authentication.UsernamePasswordAuthenticationToken) {
                        ((org.springframework.security.authentication.UsernamePasswordAuthenticationToken) authentication)
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    }
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            logger.error("Could not set user authentication in security context", e);
        }
    }
}
