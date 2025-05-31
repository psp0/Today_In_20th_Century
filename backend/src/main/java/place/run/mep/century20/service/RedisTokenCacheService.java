package place.run.mep.century20.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Profile("redis")
public class RedisTokenCacheService implements TokenCacheService {
    private static final Logger logger = LoggerFactory.getLogger(RedisTokenCacheService.class);
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenValidityMs; // 밀리초 단위로 받음

    @Override
    public void cacheToken(String token, String userId, long expirationTime) {
        logger.debug("Caching token for user: {} with TTL: {} milliseconds", userId, expirationTime);
        redisTemplate.opsForValue().set(token, userId, expirationTime, TimeUnit.MILLISECONDS);
    }

    @Override
    public void invalidateToken(String token) {
        logger.debug("Invalidating token from cache: {}", token);
        redisTemplate.delete(token);
    }

    @Override
    public boolean isTokenValid(String token) {
        boolean isValid = redisTemplate.hasKey(token);
        logger.debug("Token validation check for token: {}. Is valid: {}", token, isValid);
        return isValid;
    }

    @Override
    public String getUsernameFromToken(String token) {
        String userId = redisTemplate.opsForValue().get(token);
        logger.debug("Retrieved userId from cache for token: {}. UserId: {}", token, userId);
        return userId;
    }
}
