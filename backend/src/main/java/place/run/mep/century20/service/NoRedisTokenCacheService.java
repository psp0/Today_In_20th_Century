package place.run.mep.century20.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("noredis")
public class NoRedisTokenCacheService {
    @Value("${jwt.access-token-validity-seconds}")
    private Long accessTokenValidity;

    public void cacheToken(String token, String username) {
        // Redis 미사용 시에는 캐시하지 않음
    }

    public String getUsernameFromToken(String token) {
        return null;
    }

    public void invalidateToken(String token) {
        // Redis 미사용 시에는 무시
    }

    public boolean isTokenValid(String token) {
        return false;
    }
}
