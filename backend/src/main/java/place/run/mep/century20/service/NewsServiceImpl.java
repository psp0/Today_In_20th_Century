package place.run.mep.century20.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import place.run.mep.century20.entity.NewsArticle;
import place.run.mep.century20.repository.NewsArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class NewsServiceImpl implements NewsService {
    private static final Logger logger = LoggerFactory.getLogger(NewsServiceImpl.class);

    private final NewsArticleRepository newsArticleRepository;
    private final NewsCacheService newsCacheService;
    private final ObjectMapper objectMapper;

    @Value("${news.recommendation.strategy:legacy}")
    private String recommendationStrategy;

    public NewsServiceImpl(NewsArticleRepository newsArticleRepository,
                           NewsCacheService newsCacheService,
                           ObjectMapper objectMapper) {
        this.newsArticleRepository = newsArticleRepository;
        this.newsCacheService = newsCacheService;
        this.objectMapper = objectMapper;
    }
    private String getAllNewsKey(int month, int day, String category) {
        // 기존 getDateNewsSetKey 와 동일한 패턴 사용 가능
        return String.format("news:%02d%02d:%s", month, day, category.toLowerCase());
    }

    private String getCachedNewsKey(int month, int day, String category) {
        return String.format("article:cached:%02d%02d:%s", month, day, category.toLowerCase());
    }

    private String getArticleDataHashKey(int month, int day, String category) {
        return String.format("article:data:%02d%02d:%s", month, day, category.toLowerCase());
    }

    private String getUserViewedKey(String userId) {
        // 실제 서비스에서는 SecurityContext 등에서 사용자 ID를 가져와야 합니다.
        return String.format("user:%s:viewed", userId);
    }


    @Override
    public Optional<NewsArticle> getRandomNews(int day, int month, String category, String userId) {

        // --- [수정] 'advanced' 전략은 설정이 활성화되어 있으면 userId와 상관없이 사용 ---
        boolean useAdvancedStrategy = "advanced".equalsIgnoreCase(recommendationStrategy);

        if (useAdvancedStrategy) {
            String logUser = (userId == null || userId.isEmpty()) ? "Anonymous" : userId;
            logger.info("Using 'advanced' recommendation strategy for user: {}", logUser);
            return getAdvancedRandomNews(day, month, category, userId);
        } else {
            String logUser = (userId == null) ? "Anonymous" : userId;
            logger.info("Using 'legacy' recommendation strategy for user: {}", logUser);
            return getLegacyRandomNews(day, month, category);
        }
    }

    /**
     * 신규 전략: 캐시 우선 랜덤 뉴스 추천 (로그인/비로그인 처리 분기)
     */
    private Optional<NewsArticle> getAdvancedRandomNews(int day, int month, String category, String userId) {
        // 1. 키 정의
        String allNewsKey = getAllNewsKey(month, day, category);
        String cachedNewsKey = getCachedNewsKey(month, day, category);
        String articleDataHashKey = getArticleDataHashKey(month, day, category);

        // 2. Cold Start: 전체 뉴스 목록 캐싱 (공통 로직)
        if (!newsCacheService.hasKey(allNewsKey)) {
            logger.debug("[Advanced] Cache miss for ALL_NEWS_KEY: {}. Fetching from DB.", allNewsKey);
            Set<String> allArticleIds = newsArticleRepository.findArticleIdsByMonthDayAndCategory(month, day, category);
            if (allArticleIds == null || allArticleIds.isEmpty()) {
                logger.warn("[Advanced] No articles found in DB for {}", allNewsKey);
                return Optional.empty();
            }
            newsCacheService.cacheArticleIdsToSetWithNoExpiration(allNewsKey, allArticleIds);
            logger.info("[Advanced] Cached {} IDs to ALL_NEWS_KEY: {}", allArticleIds.size(), allNewsKey);
        }

        // 3. 로직 분기: 로그인 유저 vs. 비로그인 유저
        if (userId != null && !userId.isEmpty()) {
            // --- 기존 로직 (로그인 유저) ---
            String userViewedKey = getUserViewedKey(userId);

            // 시나리오 A: 캐시된 뉴스 중 안 본 뉴스 추천
            logger.debug("[Advanced-User] Scenario A: Trying to find an unseen article from cached articles.");
            Set<String> unseenCachedIds = newsCacheService.getSetDifference(cachedNewsKey, userViewedKey);
            if (!unseenCachedIds.isEmpty()) {
                String randomId = new ArrayList<>(unseenCachedIds).get(new Random().nextInt(unseenCachedIds.size()));
                logger.info("[Advanced-User] Scenario A success. Found unseen cached article ID: {}", randomId);
                newsCacheService.addToSet(userViewedKey, randomId);
                return newsCacheService.getArticleFromHash(articleDataHashKey, randomId);
            }

            // 시나리오 B: 아직 캐시되지 않은 새로운 뉴스 추천
            logger.debug("[Advanced-User] Scenario B: Trying to find a new article from DB.");
            Set<String> uncachedIds = newsCacheService.getSetDifference(allNewsKey, cachedNewsKey);
            if (!uncachedIds.isEmpty()) {
                String randomIdStr = new ArrayList<>(uncachedIds).get(new Random().nextInt(uncachedIds.size()));
                logger.info("[Advanced-User] Scenario B success. Found new article ID to cache: {}", randomIdStr);

                Optional<NewsArticle> dbArticleOpt = newsArticleRepository.findById(Long.parseLong(randomIdStr));
                if (dbArticleOpt.isPresent()) {
                    NewsArticle dbArticle = dbArticleOpt.get();
                    // DB에서 가져온 데이터를 캐시에 저장
                    newsCacheService.cacheArticleToHash(articleDataHashKey, randomIdStr, dbArticle);
                    newsCacheService.addToSet(cachedNewsKey, randomIdStr); // '캐시됨' 목록에 추가
                    newsCacheService.addToSet(userViewedKey, randomIdStr); // '본 기록'에 추가
                    return Optional.of(dbArticle);
                }
            }

            // 시나리오 C: 모든 뉴스를 다 본 경우 (재시청)
            logger.debug("[Advanced-User] Scenario C: User has seen all articles. Re-showing a random one.");
            Optional<String> randomIdOpt = newsCacheService.getRandomFieldFromHash(articleDataHashKey);
            if (randomIdOpt.isPresent()) {
                logger.info("[Advanced-User] Scenario C success. Re-showing article ID: {}", randomIdOpt.get());
                return newsCacheService.getArticleFromHash(articleDataHashKey, randomIdOpt.get());
            }

            logger.warn("[Advanced-User] All scenarios failed. No article could be recommended for user {}", userId);
            return Optional.empty();

        } else {
            // --- 신규 로직 (비로그인 유저) ---

            // 시나리오 A-Anon: 캐시된 뉴스 중에서 랜덤 추천
            logger.debug("[Advanced-Anon] Scenario A: Trying to find a random article from cached articles.");
            Set<String> cachedIds = newsCacheService.getArticleIdsFromSet(cachedNewsKey);
            if (!cachedIds.isEmpty()) {
                String randomId = new ArrayList<>(cachedIds).get(new Random().nextInt(cachedIds.size()));
                logger.info("[Advanced-Anon] Scenario A success. Found cached article ID: {}", randomId);
                // 본 기록을 추적하지 않으므로 바로 반환
                return newsCacheService.getArticleFromHash(articleDataHashKey, randomId);
            }

            // 시나리오 B-Anon: 아직 캐시되지 않은 새로운 뉴스 추천 및 캐싱
            logger.debug("[Advanced-Anon] Scenario B: No cached articles found. Fetching new articles from DB to populate cache.");
            Set<String> uncachedIds = newsCacheService.getSetDifference(allNewsKey, cachedNewsKey);
            if (!uncachedIds.isEmpty()) {
                // 1. 캐시되지 않은 ID 목록을 섞는다.
                List<String> uncachedIdsList = new ArrayList<>(uncachedIds);
                Collections.shuffle(uncachedIdsList);

                // 2. 캐시할 기사 수를 정한다 (최대 3개).
                int articlesToFetch = Math.min(uncachedIdsList.size(), 3);
                List<String> idsToCache = uncachedIdsList.subList(0, articlesToFetch);

                logger.info("[Advanced-Anon] Scenario B. Attempting to cache {} new articles. IDs: {}", idsToCache.size(), idsToCache);

                Optional<NewsArticle> firstArticleToReturn = Optional.empty();

                // 3. 선택된 ID들을 순회하며 DB에서 가져와 캐시에 저장한다.
                for (String idStr : idsToCache) {
                    try {
                        Optional<NewsArticle> dbArticleOpt = newsArticleRepository.findById(Long.parseLong(idStr));
                        if (dbArticleOpt.isPresent()) {
                            NewsArticle dbArticle = dbArticleOpt.get();
                            // 해시와 Set에 모두 캐싱
                            newsCacheService.cacheArticleToHash(articleDataHashKey, idStr, dbArticle);
                            newsCacheService.addToSet(cachedNewsKey, idStr);
                            logger.debug("[Advanced-Anon] Successfully cached article ID: {}", idStr);

                            // 4. 반환할 첫 번째 기사를 저장해둔다.
                            if (firstArticleToReturn.isEmpty()) {
                                firstArticleToReturn = Optional.of(dbArticle);
                            }
                        }
                    } catch (NumberFormatException e) {
                        logger.warn("[Advanced-Anon] Invalid article ID format found in uncached set: {}", idStr);
                    }
                }

                // 5. 첫 번째 기사를 반환한다. (나머지는 캐시에 저장되어 다음 요청에 사용됨)
                if (firstArticleToReturn.isPresent()) {
                    logger.info("[Advanced-Anon] Scenario B success. Returning article ID: {} and cached {} other article(s).", firstArticleToReturn.get().getId(), idsToCache.size() - 1);
                    return firstArticleToReturn;
                }
            }

            // 시나리오 C-Anon: 모든 뉴스가 캐시된 경우 (또는 A, B 실패 시)
            logger.debug("[Advanced-Anon] Scenario C: All articles are cached or previous scenarios failed. Picking a random one from hash.");
            Optional<String> randomIdOpt = newsCacheService.getRandomFieldFromHash(articleDataHashKey);
            if (randomIdOpt.isPresent()) {
                logger.info("[Advanced-Anon] Scenario C success. Showing random article ID: {}", randomIdOpt.get());
                return newsCacheService.getArticleFromHash(articleDataHashKey, randomIdOpt.get());
            }

            logger.warn("[Advanced-Anon] All scenarios failed. No article could be recommended.");
            return Optional.empty();
        }
    }

    /**
     * 기존 전략: 전체 목록에서 랜덤 뉴스 추천
     */
    public Optional<NewsArticle> getLegacyRandomNews(int day, int month, String category) {
        boolean isTodayRequest = LocalDate.now().getMonthValue() == month && LocalDate.now().getDayOfMonth() == day;
        String newsSetKey;
        long articleTtlSeconds;

        if (isTodayRequest) {
            newsSetKey = getTodayNewsSetKey(category); // 레거시 키 생성 메서드 사용
            articleTtlSeconds = calculateTtlUntilMidnightSeconds();
        } else {
            newsSetKey = getDateNewsSetKey(month, day, category); // 레거시 키 생성 메서드 사용
            articleTtlSeconds = newsCacheService.getDefaultTtlSeconds();
        }

        logger.debug("[Legacy] Trying to get random news for key: {}", newsSetKey);

        Set<String> articleIds;
        if (!newsCacheService.hasKey(newsSetKey)) {
            logger.debug("[Legacy] Cache miss for SET: {}. Fetching from DB.", newsSetKey);
            articleIds = newsArticleRepository.findArticleIdsByMonthDayAndCategory(month, day, category);
            if (articleIds != null && !articleIds.isEmpty()) {
                if (isTodayRequest) {
                    newsCacheService.cacheArticleIdsToSetWithTtl(newsSetKey, articleIds, articleTtlSeconds);
                } else {
                    newsCacheService.cacheArticleIdsToSet(newsSetKey, articleIds);
                }
                logger.debug("[Legacy] Cached {} IDs to SET: {}", articleIds.size(), newsSetKey);
            } else {
                logger.debug("[Legacy] No articles found in DB for SET: {}", newsSetKey);
                return Optional.empty();
            }
        }

        // SPOP으로 ID를 하나 꺼내와 처리하는 것이 더 효율적일 수 있습니다. (중복 방지)
        String randomArticleId = newsCacheService.getRandomArticleIdFromSet(newsSetKey);
        if (randomArticleId == null) {
            logger.warn("[Legacy] Could not get random article ID from Redis SET: {}", newsSetKey);
            return Optional.empty();
        }

        logger.debug("[Legacy] Selected random article ID: {} from SET: {}", randomArticleId, newsSetKey);
        String articleKey = getArticleKey(randomArticleId); // 레거시 아티클 키

        Optional<NewsArticle> cachedArticle = newsCacheService.getArticle(articleKey);
        if (cachedArticle.isPresent()) {
            logger.debug("[Legacy] Cache hit for ARTICLE: {}", articleKey);
            return cachedArticle;
        }

        logger.debug("[Legacy] Cache miss for ARTICLE: {}. Fetching from DB.", articleKey);
        try {
            Long id = Long.parseLong(randomArticleId);
            Optional<NewsArticle> dbArticle = newsArticleRepository.findById(id);
            if (dbArticle.isPresent()) {
                // 이 부분은 레거시 로직의 article:{id} 형태의 키에 저장됩니다.
                newsCacheService.cacheArticle(articleKey, dbArticle.get(), articleTtlSeconds);
                logger.debug("[Legacy] Cached ARTICLE: {} with TTL: {} seconds", articleKey, articleTtlSeconds);
                return dbArticle;
            } else {
                logger.warn("[Legacy] Article with ID: {} not found in DB, though ID was in SET: {}", randomArticleId, newsSetKey);
                return Optional.empty();
            }
        } catch (NumberFormatException e) {
            logger.error("[Legacy] Invalid article ID format: {}", randomArticleId, e);
            return Optional.empty();
        }
    }

    private String getTodayNewsSetKey(String category) {
        return "news:today:" + category.toLowerCase();
    }

    private String getDateNewsSetKey(int month, int day, String category) {
        return String.format("news:%02d%02d:%s", month, day, category.toLowerCase());
    }

    private String getArticleKey(String articleId) {
        return "article:" + articleId;
    }

    private long calculateTtlUntilMidnightSeconds() {
        LocalTime now = LocalTime.now();
        LocalTime midnight = LocalTime.MIDNIGHT;
        Duration duration = Duration.between(now, midnight);
        if (duration.isNegative()) {
            duration = duration.plusDays(1);
        }
        return duration.getSeconds();
    }

    @Override
    public List<NewsArticle> searchNews(int month, int day, String category) {
        String newsSetKey = getDateNewsSetKey(month, day, category);
        long articleTtlSeconds = newsCacheService.getDefaultTtlSeconds();

        logger.debug("[Search] Trying to get random news for key: {}", newsSetKey);

        Set<String> articleIds;
        if (newsCacheService.hasKey(newsSetKey)) {
            articleIds = newsCacheService.getArticleIdsFromSet(newsSetKey);
            logger.debug("[Search] Cache hit for SET: {}. Found {} IDs.", newsSetKey, articleIds != null ? articleIds.size() : 0);
        } else {
            logger.debug("[Search] Cache miss for SET: {}. Fetching from DB.", newsSetKey);
            articleIds = newsArticleRepository.findArticleIdsByMonthDayAndCategory(month, day, category);
            if (articleIds != null && !articleIds.isEmpty()) {
                newsCacheService.cacheArticleIdsToSet(newsSetKey, articleIds);
                logger.debug("[Search] Cached {} IDs to SET: {}", articleIds.size(), newsSetKey);
            } else {
                logger.debug("[Search] No articles found in DB for SET: {}", newsSetKey);
                return Collections.emptyList();
            }
        }

        if (articleIds == null || articleIds.isEmpty()) {
            return Collections.emptyList();
        }

        String randomArticleId = newsCacheService.getRandomArticleIdFromSet(newsSetKey);
        if (randomArticleId == null) {
            if (!articleIds.isEmpty()) {
                randomArticleId = articleIds.stream().skip((int) (articleIds.size() * Math.random())).findFirst().orElse(null);
            }
            if(randomArticleId == null) return Collections.emptyList();
            logger.warn("[Search] Could not get random article ID from Redis SET: {} (fallback used).", newsSetKey);
        }

        logger.debug("[Search] Selected random article ID: {} from SET: {}", randomArticleId, newsSetKey);
        String articleKey = getArticleKey(randomArticleId);

        Optional<NewsArticle> cachedArticle = newsCacheService.getArticle(articleKey);
        if (cachedArticle.isPresent()) {
            logger.debug("[Search] Cache hit for ARTICLE: {}", articleKey);
            return List.of(cachedArticle.get());
        }

        logger.debug("[Search] Cache miss for ARTICLE: {}. Fetching from DB.", articleKey);
        try {
            Long id = Long.parseLong(randomArticleId);
            Optional<NewsArticle> dbArticle = newsArticleRepository.findById(id);
            if (dbArticle.isPresent()) {
                newsCacheService.cacheArticle(articleKey, dbArticle.get(), articleTtlSeconds);
                logger.debug("[Search] Cached ARTICLE: {} with TTL: {} seconds", articleKey, articleTtlSeconds);
                return List.of(dbArticle.get());
            } else {
                logger.warn("[Search] Article with ID: {} not found in DB, though ID was in SET: {}", randomArticleId, newsSetKey);
                return Collections.emptyList();
            }
        } catch (NumberFormatException e) {
            logger.error("[Search] Invalid article ID format: {}", randomArticleId, e);
            return Collections.emptyList();
        }
    }
}