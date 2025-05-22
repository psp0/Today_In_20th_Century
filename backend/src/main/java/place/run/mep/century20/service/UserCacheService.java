package place.run.mep.century20.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Profile("redis")
public class UserCacheService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("${spring.cache.redis.time-to-live}")
    private Long cacheTtl;

    public void cacheUserInfo(String userId, String userInfo) {
        redisTemplate.opsForValue().set("user:" + userId, userInfo, cacheTtl, TimeUnit.MILLISECONDS);
    }

    public String getCachedUserInfo(String userId) {
        return redisTemplate.opsForValue().get("user:" + userId);
    }

    public void invalidateUserCache(String userId) {
        redisTemplate.delete("user:" + userId);
    }

    public boolean isUserCached(String userId) {
        return redisTemplate.hasKey("user:" + userId);
    }
}
