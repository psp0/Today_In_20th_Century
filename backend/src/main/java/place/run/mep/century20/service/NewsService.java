package place.run.mep.century20;

import place.run.mep.century20.entity.NewsArticle;
import place.run.mep.century20.repository.NewsArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional(readOnly = true)
public class NewsService {
    private final NewsArticleRepository newsArticleRepository;

    public NewsService(NewsArticleRepository newsArticleRepository) {
        this.newsArticleRepository = newsArticleRepository;
    }

    public Optional<NewsArticle> getRandomNews(int day, int month, NewsArticle.MainCategory category) {
        List<NewsArticle> articles;
        if (category == null || category == NewsArticle.MainCategory.valueOf("전체")) {
            articles = newsArticleRepository.findByPublishedDate(day, month);
        } else {
            articles = newsArticleRepository.findByPublishedDateAndCategory(day, month, category);
        }

        if (articles.isEmpty()) {
            return Optional.empty();
        }

        Random random = new Random();
        return Optional.of(articles.get(random.nextInt(articles.size())));
    }
}
