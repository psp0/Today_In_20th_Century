package place.run.mep.century20;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import place.run.mep.century20.config.RedisProperties;

@SpringBootApplication
@EnableConfigurationProperties(RedisProperties.class)
public class Century20Application extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Century20Application.class);
    }
    public static void main(String[] args) {
        SpringApplication.run(Century20Application.class, args);
    }
}
