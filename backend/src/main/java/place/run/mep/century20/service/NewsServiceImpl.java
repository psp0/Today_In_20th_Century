package place.run.mep.century20.service;

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
public class NewsServiceImpl implements NewsService {
    private final NewsArticleRepository newsArticleRepository;

    public NewsServiceImpl(NewsArticleRepository newsArticleRepository) {
        this.newsArticleRepository = newsArticleRepository;
    }

    public Optional<NewsArticle> getRandomNews(int day, int month, String category) {
        if (category == null || "all".equals(category)) {
            return newsArticleRepository.findRandomByPublishedDate(day, month);
        } else {
            return newsArticleRepository.findByPublishedDateAndCategory(day, month, category);
        }
    }

    public Optional<NewsArticle> getRandomNews(int day, int month) {
        return getRandomNews(day, month, null);
    }

    public List<NewsArticle> searchNews(int year, int month, int day, String category) {
        LocalDate date = LocalDate.of(year, month, day);
        int dayOfMonth = date.getDayOfMonth();
        int monthValue = date.getMonthValue();

        if (category == null || "all".equals(category)) {
            return newsArticleRepository.findByPublishedDate(dayOfMonth, monthValue)
                .map(List::of)
                .orElseGet(List::of);
        } else {
            return newsArticleRepository.findByPublishedDateAndCategory(dayOfMonth, monthValue, category)
                .map(List::of)
                .orElseGet(List::of);
        }
    }
}
