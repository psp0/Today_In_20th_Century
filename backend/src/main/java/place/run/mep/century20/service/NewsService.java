package place.run.mep.century20.service;

import place.run.mep.century20.entity.NewsArticle;
import java.util.List;
import java.util.Optional;

public interface NewsService {
    Optional<NewsArticle> getRandomNews(int day, int month, String category, String userId);
    List<NewsArticle> searchNews(int month, int day, String category);
}
