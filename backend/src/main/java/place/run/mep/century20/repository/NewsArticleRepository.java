package place.run.mep.century20.repository;

import place.run.mep.century20.entity.NewsArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {
    // 기존 메서드 (필요에 따라 유지 또는 삭제)
    @Query("SELECT na FROM NewsArticle na WHERE DAY(na.publishedDate) = :day AND MONTH(na.publishedDate) = :month ORDER BY RAND() LIMIT 1")
    Optional<NewsArticle> findByPublishedDate(@Param("day") int day, @Param("month") int month);

    @Query("SELECT na FROM NewsArticle na WHERE DAY(na.publishedDate) = :day AND MONTH(na.publishedDate) = :month AND (na.mainCategory = :category OR :category = 'all') ORDER BY RAND() LIMIT 1")
    Optional<NewsArticle> findByPublishedDateAndCategory(@Param("day") int day, @Param("month") int month, @Param("category") String category);

    // --- 수정/추가된 메서드 ---
    /**
     * 특정 월, 일, 카테고리에 해당하는 모든 기사 ID를 조회합니다.
     * 카테고리가 "all"인 경우 해당 날짜의 모든 기사 ID를 조회합니다.
     */
    @Query("SELECT na.id FROM NewsArticle na WHERE MONTH(na.publishedDate) = :month AND DAY(na.publishedDate) = :day AND (:category = 'all' OR na.mainCategory = :category)")
    Set<String> findArticleIdsByMonthDayAndCategory(@Param("month") int month, @Param("day") int day, @Param("category") String category);

    /**
     * 특정 월, 일에 해당하는 모든 기사 ID를 조회합니다. (카테고리 "all" 전용)
     */
    @Query("SELECT na.id FROM NewsArticle na WHERE MONTH(na.publishedDate) = :month AND DAY(na.publishedDate) = :day")
    Set<String> findArticleIdsByMonthDay(@Param("month") int month, @Param("day") int day);

      /**
     * pub_month, pub_day 인덱스를 활용한 무작위 기사 1건 조회
     */
    @Query("SELECT na FROM NewsArticle na WHERE na.pubDay = :day AND na.pubMonth = :month ORDER BY FUNCTION('RAND')")
    Optional<NewsArticle> findByPubDayMonth(@Param("day") int day, @Param("month") int month);

    /**
     * pub_month, pub_day, main_category 인덱스를 활용한 무작위 기사 1건 조회
     */
    @Query("SELECT na FROM NewsArticle na WHERE na.pubDay = :day AND na.pubMonth = :month AND (:category = 'all' OR na.mainCategory = :category) ORDER BY FUNCTION('RAND')")
    Optional<NewsArticle> findByPubDayMonthAndCategory(@Param("day") int day, @Param("month") int month, @Param("category") String category);

    /**
     * pub_month, pub_day, main_category 인덱스를 활용한 ID 조회
     */
    @Query("SELECT na.id FROM NewsArticle na WHERE na.pubMonth = :month AND na.pubDay = :day AND (:category = 'all' OR na.mainCategory = :category)")
    Set<String> findArticleIdsByPubMonthDayAndCategory(@Param("month") int month, @Param("day") int day, @Param("category") String category);

    /**
     * pub_month, pub_day 인덱스를 활용한 ID 조회
     */
    @Query("SELECT na.id FROM NewsArticle na WHERE na.pubMonth = :month AND na.pubDay = :day")
    Set<String> findArticleIdsByPubMonthDay(@Param("month") int month, @Param("day") int day);


}