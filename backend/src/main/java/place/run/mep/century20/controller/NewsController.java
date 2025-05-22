package place.run.mep.century20;

import place.run.mep.century20.dto.NewsResponse;
import place.run.mep.century20.entity.NewsArticle;
import place.run.mep.century20.service.NewsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/news")
public class NewsController {
    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping("/random/today")
    public ResponseEntity<NewsResponse> getTodayRandomNews(
            @RequestParam(required = false, defaultValue = "전체") NewsArticle.MainCategory category) {
        try {
            LocalDate today = LocalDate.now();
            return newsService.getRandomNews(today.getDayOfMonth(), today.getMonthValue(), category)
                    .map(article -> {
                        NewsResponse response = new NewsResponse(article);
                        response.setTotalNewsCount(newsService.getTotalNewsCount(today.getDayOfMonth(), today.getMonthValue(), category));
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new NewsResponse("오늘의 뉴스를 찾을 수 없습니다.")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new NewsResponse("오늘의 뉴스를 불러오는데 실패했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<NewsResponse> searchNews(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int day,
            @RequestParam(required = false, defaultValue = "전체") NewsArticle.MainCategory category) {
        try {
            LocalDate today = LocalDate.now();
            return newsService.searchNews(year, month, day, category)
                    .map(article -> {
                        NewsResponse response = new NewsResponse();
                        response.setNews(article);
                        response.setTotalNewsCount(newsService.getTotalNewsCount(day, month, category));
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new NewsResponse("해당 날짜의 뉴스를 찾을 수 없습니다.")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new NewsResponse("뉴스를 불러오는데 실패했습니다: " + e.getMessage()));
        }
    }
}
