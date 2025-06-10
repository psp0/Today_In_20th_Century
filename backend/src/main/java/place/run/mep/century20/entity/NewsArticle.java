package place.run.mep.century20.entity;

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

    @Column(nullable = false, length = 10)
    private String mainCategory;

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

    @Column(name = "pub_day", insertable = false, updatable = false)
    private Integer pubDay;

    @Column(name = "pub_month", insertable = false, updatable = false)
    private Integer pubMonth;


}
