package net.deckerego.docidx.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "crawler")
@Data
public class CrawlerConfig {
    private String rootPath;
    private Boolean skipHidden = Boolean.TRUE;
    private int waitSeconds = 5 * 60;
}
