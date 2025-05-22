package place.run.mep.century20.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Profile("redis")
public class TokenCacheService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.access-token-validity-seconds}")
    private Long accessTokenValidity;

    public void cacheToken(String token, String username) {
        redisTemplate.opsForValue().set("token:" + token, username, accessTokenValidity, TimeUnit.SECONDS);
    }

    public String getUsernameFromToken(String token) {
        return redisTemplate.opsForValue().get("token:" + token);
    }

    public void invalidateToken(String token) {
        redisTemplate.delete("token:" + token);
    }

    public boolean isTokenValid(String token) {
        return redisTemplate.hasKey("token:" + token);
    }
}
