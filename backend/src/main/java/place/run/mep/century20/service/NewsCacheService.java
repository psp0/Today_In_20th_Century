package place.run.mep.century20.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Profile("redis")
public class NewsCacheService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("${spring.cache.redis.time-to-live}")
    private Long cacheTtl;

    public void cacheNews(String date, String newsData) {
        redisTemplate.opsForValue().set("news:" + date, newsData, cacheTtl, TimeUnit.MILLISECONDS);
    }

    public String getCachedNews(String date) {
        return redisTemplate.opsForValue().get("news:" + date);
    }

    public void invalidateNewsCache(String date) {
        redisTemplate.delete("news:" + date);
    }

    public boolean isNewsCached(String date) {
        return redisTemplate.hasKey("news:" + date);
    }
}
