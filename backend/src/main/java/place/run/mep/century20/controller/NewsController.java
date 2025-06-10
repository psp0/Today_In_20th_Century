package place.run.mep.century20.controller;

import place.run.mep.century20.dto.NewsResponse;
import place.run.mep.century20.entity.NewsArticle;
import place.run.mep.century20.service.NewsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/news")
public class NewsController {
    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping("/random/today")
    public ResponseEntity<List<NewsResponse>> getTodayRandomNews(
            @RequestParam(required = false, defaultValue = "all") String category,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            LocalDate today = LocalDate.now();
            String userId = (userDetails != null) ? userDetails.getUsername() : null;
            return newsService.getRandomNews(today.getDayOfMonth(), today.getMonthValue(), category, userId)
                    .map(article -> ResponseEntity.ok(Collections.singletonList(new NewsResponse(article))))
                    .orElse(ResponseEntity.ok(Collections.emptyList()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonList(new NewsResponse("오늘의 뉴스를 불러오는데 실패했습니다: " + e.getMessage())));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<NewsResponse>> searchNews(       
            @RequestParam int month,
            @RequestParam int day,
            @RequestParam(required = false, defaultValue = "all") String category) {
        try {
            return ResponseEntity.ok(newsService.searchNews( month, day, category)
                    .stream()
                    .map(NewsResponse::new)
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonList(new NewsResponse("뉴스를 불러오는데 실패했습니다: " + e.getMessage())));
        }
    }
}
