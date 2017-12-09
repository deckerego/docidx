package net.deckerego.docidx.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "crawler")
public class CrawlerConfig {
    private String rootPath;

    public void setRootPath(String rootPath) { this.rootPath = rootPath; }
    public String getRootPath() { return this.rootPath; }
}
