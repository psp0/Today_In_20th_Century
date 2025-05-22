package place.run.mep.century20;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "news_articles")
public class NewsArticle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MainCategory mainCategory;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private LocalDate publishedDate;

    @Column(nullable = false)
    private String press;

    @Column(nullable = false)
    private String reporter;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String categoryLevel1;

    @Column(nullable = false)
    private String categoryLevel2;

    @Column(nullable = false)
    private String categoryLevel3;

    @Column(nullable = false, length = 500)
    private String content;

    public enum MainCategory {
        물가, 가계대출, 기준금리, 출산율, 파업
    }
}
