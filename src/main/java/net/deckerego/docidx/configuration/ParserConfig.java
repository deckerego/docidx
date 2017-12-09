package net.deckerego.docidx.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "parser")
public class ParserConfig {
    private int ocrTimeoutSeconds;
    private String ocrLanguage;

    public void setOcrTimeoutSeconds(int ocrTimeoutSeconds) { this.ocrTimeoutSeconds = ocrTimeoutSeconds; }
    public int getOcrTimeoutSeconds() { return ocrTimeoutSeconds; }
    public void setOcrLanguage(String ocrLanguage) { this.ocrLanguage = ocrLanguage; }
    public String getOcrLanguage() { return ocrLanguage; }
}
