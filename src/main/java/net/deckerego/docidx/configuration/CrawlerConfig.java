package net.deckerego.docidx.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "crawler")
public class CrawlerConfig {
    private String rootPath;
    private boolean skipHidden = true;

    public void setRootPath(String rootPath) { this.rootPath = rootPath; }
    public String getRootPath() { return this.rootPath; }
    public void setSkipHidden(boolean hidden) { this.skipHidden = skipHidden; }
    public boolean getSkipHidden() { return this.skipHidden; }
}
