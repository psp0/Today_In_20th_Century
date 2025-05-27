package place.run.mep.century20.controller;

import lombok.Data;
import place.run.mep.century20.entity.NewsArticle;
import java.time.LocalDateTime;

@Data
public class NewsResponse {
    private Long id;
    private String title;
    private String content;
    private String source;
    private LocalDateTime publishedAt;
    private String category;
    private String message;

    public NewsResponse() {}

    public NewsResponse(NewsArticle article) {
        this.id = article.getId();
        this.title = article.getTitle();
        this.content = article.getContent();
        this.source = article.getPress();
        this.publishedAt = article.getPublishedDate().atStartOfDay();
        this.category = article.getCategoryLevel1();
    }

    public NewsResponse(String message) {
        this.message = message;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
