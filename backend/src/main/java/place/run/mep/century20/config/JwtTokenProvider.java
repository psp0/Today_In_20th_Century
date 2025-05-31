package place.run.mep.century20.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import place.run.mep.century20.entity.UserRefreshToken;
import place.run.mep.century20.repository.UserRefreshTokenRepository;
import java.util.Optional;
import javax.crypto.SecretKey;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import place.run.mep.century20.service.TokenCacheService;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenProvider {
    private final SecretKey key;
    private final long accessTokenExpirationMs; // 밀리초 단위로 저장
    private final long refreshTokenExpirationMs; // 밀리초 단위로 저장

    public long getAccessTokenValidityInSeconds() {
        return accessTokenExpirationMs / 1000;
    }

    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }

    public long getRefreshTokenValidityInSeconds() {
        return refreshTokenExpirationMs / 1000;
    }
    private final UserDetailsService userDetailsService;
    private final UserRefreshTokenRepository userRefreshTokenRepository;
    
    @Autowired
    public JwtTokenProvider(
            UserDetailsService userDetailsService,
            UserRefreshTokenRepository userRefreshTokenRepository,
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpirationMs, // 밀리초 단위로 받음
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpirationMs) {
        this.userDetailsService = userDetailsService;
        this.userRefreshTokenRepository = userRefreshTokenRepository;
        // Initialize key and expiration times after values are injected
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String createAccessToken(String username, Map<String, Object> claims) {
        // UTC 기준 시간 사용
        Date now = new Date();
        long expirationTime = now.getTime() + accessTokenExpirationMs;
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setExpiration(new Date(expirationTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(String username) {
        // UTC 기준 시간 사용
        Date now = new Date();
        
        return Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(now.getTime() + refreshTokenExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public String resolveToken(String token) {
        return token; // 토큰 문자열을 그대로 반환
    }

    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            return claims.getSubject();
        } catch (Exception e) {
            // Log the exception for debugging
            System.err.println("JWT validation error: " + e.getMessage());
            return null;
        }
    }

    public TokenValidationResult validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            
            // Check if token has expired
            Date expiration = claims.getExpiration();
            // UTC 기준 시간 사용
            Date now = new Date();
            
            // 토큰 만료 시간이 현재 시간보다 이전인 경우만 만료 처리
            if (expiration.before(now)) {
                return new TokenValidationResult(false, "Token has expired", HttpStatus.UNAUTHORIZED);
            }
            
            // Only validate refresh token in database when explicitly requested
            if (claims.getSubject() != null && token.startsWith("refresh:")) {
                Optional<UserRefreshToken> refreshTokenRecord = userRefreshTokenRepository.findByUser_UserIdAndRevokedFalse(claims.getSubject());
                if (!refreshTokenRecord.isPresent()) {
                    return new TokenValidationResult(false, "Refresh token not found or revoked", HttpStatus.UNAUTHORIZED);
                }
            }
            
            return new TokenValidationResult(true, "Token is valid", HttpStatus.OK);
        } catch (Exception e) {
            // Log the exception for debugging
            System.err.println("JWT validation error: " + e.getMessage());
            return new TokenValidationResult(false, "Invalid token format", HttpStatus.UNAUTHORIZED);
        }
    }

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(getUsernameFromToken(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public long getRefreshTokenValidityInMs() {
        return refreshTokenExpirationMs;
    }
}
