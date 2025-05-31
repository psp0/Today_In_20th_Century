package place.run.mep.century20.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Profile("redis")
public class RedisUserCacheService implements UserCacheServiceInterface {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("${spring.cache.redis.time-to-live}")
    private Long cacheTtl;

    @Override
    public void cacheUserInfo(String userId, String userInfo) {
        redisTemplate.opsForValue().set("user:" + userId, userInfo, cacheTtl, TimeUnit.MILLISECONDS);
    }

    @Override
    public String getCachedUserInfo(String userId) {
        return redisTemplate.opsForValue().get("user:" + userId);
    }

    @Override
    public void invalidateUserCache(String userId) {
        redisTemplate.delete("user:" + userId);
    }

    @Override
    public boolean isUserCached(String userId) {
        return redisTemplate.hasKey("user:" + userId);
    }
}
