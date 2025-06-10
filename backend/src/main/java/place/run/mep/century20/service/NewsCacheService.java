package place.run.mep.century20.service;

import place.run.mep.century20.entity.NewsArticle;
import java.util.Optional;
import java.util.Set;

public interface NewsCacheService {
    void cacheArticleIdsToSet(String key, Set<String> articleIds);
    void cacheArticleIdsToSetWithNoExpiration(String key, Set<String> articleIds);
    void cacheArticleIdsToSetWithTtl(String key, Set<String> articleIds, long ttlSeconds);
    Set<String> getArticleIdsFromSet(String key);
    String getRandomArticleIdFromSet(String key);
    boolean hasKey(String key);
    void cacheArticle(String key, NewsArticle article, long ttlSeconds);
    Optional<NewsArticle> getArticle(String key);
    void deleteKey(String key);
    long getDefaultTtlSeconds();
    /**
     * 두 Set의 차집합(difference)을 구합니다. (key1 - key2)
     */
    Set<String> getSetDifference(String key1, String key2);

    /**
     * Set에 멤버(ID)를 추가합니다.
     */
    void addToSet(String key, String... values);

    /**
     * Hash에서 특정 필드(뉴스 ID)의 데이터(뉴스 JSON)를 조회합니다.
     */
    Optional<NewsArticle> getArticleFromHash(String hashKey, String field);

    /**
     * Hash에 특정 필드와 데이터(뉴스)를 저장합니다.
     */
    void cacheArticleToHash(String hashKey, String field, NewsArticle article);

    /**
     * Hash에서 임의의 필드(뉴스 ID)를 조회합니다.
     */
    Optional<String> getRandomFieldFromHash(String hashKey);
}
