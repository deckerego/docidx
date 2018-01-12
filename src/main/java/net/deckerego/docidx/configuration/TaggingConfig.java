package net.deckerego.docidx.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "tagging")
@Data
public class TaggingConfig {
    private double threshold = 0.8;
}
