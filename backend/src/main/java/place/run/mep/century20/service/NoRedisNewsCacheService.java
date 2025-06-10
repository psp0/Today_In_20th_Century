package place.run.mep.century20.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import place.run.mep.century20.entity.NewsArticle;

@Service
@Profile("noredis")
public class NoRedisNewsCacheService implements NewsCacheService {
    private static final Logger logger = LoggerFactory.getLogger(NoRedisNewsCacheService.class);

    @Override
    public void cacheArticleIdsToSet(String key, Set<String> articleIds) {
        logger.debug("[NoRedisCache] Attempted to cache article IDs for key: {}", key);
        // 아무것도 하지 않음
    }

    @Override
    public void cacheArticleIdsToSetWithNoExpiration(String key, Set<String> articleIds) {
        logger.debug("[NoRedisCache] Attempted to cache article IDs with no expiration for key: {}", key);
        // 아무것도 하지 않음
    }

    @Override
    public void cacheArticleIdsToSetWithTtl(String key, Set<String> articleIds, long ttlSeconds) {
        logger.debug("[NoRedisCache] Attempted to cache article IDs with TTL for key: {}", key);
        // 아무것도 하지 않음
    }

    @Override
    public boolean hasKey(String key) {
        logger.debug("[NoRedisCache] Checked key: {}", key);
        return false; // 항상 캐시에 없다고 가정
    }

    @Override
    public long getDefaultTtlSeconds() {
        return 3600L; // 기본값 반환 (1시간)
    }

    @Override
    public void deleteKey(String key) {
        logger.debug("[NoRedisCache] Attempted to delete key: {}", key);
        // 아무것도 하지 않음
    }

    @Override
    public Set<String> getArticleIdsFromSet(String key) {
        logger.debug("[NoRedisCache] Attempted to get article IDs from set: {}", key);
        return Collections.emptySet(); // 빈 Set 반환
    }

    @Override
    public String getRandomArticleIdFromSet(String key) {
        logger.debug("[NoRedisCache] Attempted to get random article ID from set: {}", key);
        return null; // 아무것도 반환하지 않음
    }

    @Override
    public void cacheArticle(String key, NewsArticle article, long ttlSeconds) {
        logger.debug("[NoRedisCache] Attempted to cache article for key: {}", key);
        // 아무것도 하지 않음
    }

    @Override
    public Optional<NewsArticle> getArticle(String key) {
        logger.debug("[NoRedisCache] Attempted to get article for key: {}", key);
        return Optional.empty(); // 항상 비어있는 Optional 반환
    }

    @Override
    public Optional<String> getRandomFieldFromHash(String hashKey) {
        logger.debug("[NoRedisCache] Attempted to get random field from hash: {}", hashKey);
        return Optional.empty(); // 아무것도 반환하지 않음
    }

    @Override
    public Set<String> getSetDifference(String key1, String key2) {
        logger.debug("[NoRedisCache] Attempted to get set difference between {} and {}", key1, key2);
        return Collections.emptySet(); // 빈 Set 반환
    }

    @Override
    public void addToSet(String key, String... values) {
        logger.debug("[NoRedisCache] Attempted to add to set: {}", key);
        // 아무것도 하지 않음
    }

    @Override
    public Optional<NewsArticle> getArticleFromHash(String hashKey, String field) {
        logger.debug("[NoRedisCache] Attempted to get article from hash: {} field: {}", hashKey, field);
        return Optional.empty(); // 항상 비어있는 Optional 반환
    }

    @Override
    public void cacheArticleToHash(String hashKey, String field, NewsArticle article) {
        logger.debug("[NoRedisCache] Attempted to cache article to hash: {} field: {}", hashKey, field);
        // 아무것도 하지 않음
    }
}
