package place.run.mep.century20.dto;

import lombok.Getter;
import lombok.Setter;
import place.run.mep.century20.entity.NewsArticle;

import java.time.LocalDate;

@Getter
@Setter
public class NewsResponse {
    private Long id;
    private String mainCategory;
    private String address;
    private LocalDate publishedDate;
    private String press;
    private String reporter;
    private String title;
    private String categoryLevel1;
    private String categoryLevel2;
    private String categoryLevel3;
    private String content;

    public NewsResponse(NewsArticle article) {
        this.id = article.getId();
        this.mainCategory = article.getMainCategory();
        this.address = article.getAddress();
        this.publishedDate = article.getPublishedDate();
        this.press = article.getPress();
        this.reporter = article.getReporter();
        this.title = article.getTitle();
        this.categoryLevel1 = article.getCategoryLevel1();
        this.categoryLevel2 = article.getCategoryLevel2();
        this.categoryLevel3 = article.getCategoryLevel3();
        this.content = article.getContent();
    }

    // 메시지 전달용 생성자(예: 에러 메시지)
    public NewsResponse(String message) {
        this.title = message;
    }
}
