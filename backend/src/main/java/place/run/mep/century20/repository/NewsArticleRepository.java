package place.run.mep.century20;

import place.run.mep.century20.entity.NewsArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {
    @Query("SELECT na FROM NewsArticle na WHERE DAY(na.publishedDate) = :day AND MONTH(na.publishedDate) = :month")
    List<NewsArticle> findByPublishedDate(int day, int month);

    @Query("SELECT na FROM NewsArticle na WHERE DAY(na.publishedDate) = :day AND MONTH(na.publishedDate) = :month AND na.mainCategory = :category")
    List<NewsArticle> findByPublishedDateAndCategory(int day, int month, NewsArticle.MainCategory category);
}
