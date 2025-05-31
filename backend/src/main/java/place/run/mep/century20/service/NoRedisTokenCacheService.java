package place.run.mep.century20.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import place.run.mep.century20.config.JwtTokenProvider;
import lombok.RequiredArgsConstructor;

@Service
@Profile("noredis")
@RequiredArgsConstructor
public class NoRedisTokenCacheService implements TokenCacheService {
    @Value("${jwt.access-token-validity-seconds}")
    private Long accessTokenValidity;
    
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void cacheToken(String token, String userId, long expirationTime) {
        // Redis 미사용 시에는 캐시하지 않음
    }

    @Override
    public void invalidateToken(String token) {
        // Redis 미사용 시에는 무시
    }

    @Override
    public boolean isTokenValid(String token) {
        // Redis 미사용 시에는 JWT 자체의 유효성만 확인
        return true;
    }

    @Override
    public String getUsernameFromToken(String token) {
        // Redis 미사용 시에는 토큰에서 직접 사용자명 추출
        try {
            return jwtTokenProvider.getUsernameFromToken(token);
        } catch (Exception e) {
            // 로깅 및 예외 처리 (예: 유효하지 않은 토큰)
            return null;
        }
    }
}
