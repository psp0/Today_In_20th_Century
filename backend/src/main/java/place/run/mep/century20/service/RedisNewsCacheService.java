package place.run.mep.century20.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import place.run.mep.century20.entity.NewsArticle;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Profile("redis")
public class RedisNewsCacheService implements NewsCacheService {
    private final RedisTemplate<String, String> redisTemplate;
    private final SetOperations<String, String> setOperations;
    private final ValueOperations<String, String> valueOperations;
    private final ObjectMapper objectMapper;
    private final org.springframework.data.redis.core.HashOperations<String, String, String> hashOperations;

    @Value("${spring.cache.redis.time-to-live:3600000}")
    private Long defaultCacheTtlMillis;

    @Autowired
    public RedisNewsCacheService(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.setOperations = redisTemplate.opsForSet();
        this.valueOperations = redisTemplate.opsForValue();
        this.hashOperations = redisTemplate.opsForHash();        
        this.objectMapper = objectMapper;
    }

    @Override
    public void cacheArticleIdsToSet(String key, Set<String> articleIds) {
        if (articleIds != null && !articleIds.isEmpty()) {
            setOperations.add(key, articleIds.toArray(new String[0]));
            redisTemplate.expire(key, Duration.ofMillis(defaultCacheTtlMillis));
        }
    }

    @Override
    public void cacheArticleIdsToSetWithNoExpiration(String key, Set<String> articleIds) {
        if (articleIds != null && !articleIds.isEmpty()) {
            setOperations.add(key, articleIds.toArray(new String[0]));
        }
    }

    @Override
    public void cacheArticleIdsToSetWithTtl(String key, Set<String> articleIds, long ttlSeconds) {
        if (articleIds != null && !articleIds.isEmpty()) {
            setOperations.add(key, articleIds.toArray(new String[0]));
            redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
        }
    }

    @Override
    public Set<String> getArticleIdsFromSet(String key) {
        return setOperations.members(key);
    }

    @Override
    public String getRandomArticleIdFromSet(String key) {
        return setOperations.randomMember(key);
    }

    @Override
    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    @Override
    public void cacheArticle(String key, NewsArticle article, long ttlSeconds) {
        try {
            String articleJson = objectMapper.writeValueAsString(article);
            if (ttlSeconds > 0) {
                valueOperations.set(key, articleJson, ttlSeconds, TimeUnit.SECONDS);
            } else {
                valueOperations.set(key, articleJson);
            }
        } catch (JsonProcessingException e) {
            System.err.println("Error caching article to JSON: " + e.getMessage());
        }
    }

    @Override
    public Optional<NewsArticle> getArticle(String key) {
        try {
            String articleJson = valueOperations.get(key);
            if (articleJson != null) {
                return Optional.of(objectMapper.readValue(articleJson, NewsArticle.class));
            }
        } catch (JsonProcessingException e) {
            System.err.println("Error retrieving article from JSON: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public long getDefaultTtlSeconds() {
        return defaultCacheTtlMillis / 1000;
    }
    
    @Override
    public Set<String> getSetDifference(String key1, String key2) {
        return setOperations.difference(key1, key2);
    }

    @Override
    public void addToSet(String key, String... values) {
        setOperations.add(key, values);
    }

    @Override
    public Optional<NewsArticle> getArticleFromHash(String hashKey, String field) {
        try {
            String articleJson = hashOperations.get(hashKey, field);
            if (articleJson != null) {
                return Optional.of(objectMapper.readValue(articleJson, NewsArticle.class));
            }
        } catch (JsonProcessingException e) {
            System.err.println("Error retrieving article from JSON (Hash): " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void cacheArticleToHash(String hashKey, String field, NewsArticle article) {
        try {
            String articleJson = objectMapper.writeValueAsString(article);
            hashOperations.put(hashKey, field, articleJson);
        } catch (JsonProcessingException e) {
            System.err.println("Error caching article to JSON (Hash): " + e.getMessage());
        }
    }

    @Override
    public Optional<String> getRandomFieldFromHash(String hashKey) {
        // hrandfield 명령어는 Spring Data Redis에서 직접 지원하지 않으므로,
        // 모든 키를 가져와서 랜덤 선택하는 방식으로 구현합니다.
        Set<String> fields = hashOperations.keys(hashKey);
        if (fields == null || fields.isEmpty()) {
            return Optional.empty();
        }
        int randomIndex = new java.util.Random().nextInt(fields.size());
        return fields.stream().skip(randomIndex).findFirst();
    }
}
