package net.deckerego.docidx.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "crawler")
@Data
public class BrokerConfig {
    private int poolThreads = Runtime.getRuntime().availableProcessors() / 2;
    private long purgeWait = 10000;
    private long timeout = 60 * 60 * 1000;
    private int capacity = 10000;
    private int batchSize = 10;
}
