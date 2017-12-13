package net.deckerego.docidx.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "crawler")
public class BrokerConfig {
    private int poolThreads = Runtime.getRuntime().availableProcessors() / 2;
    private long purgeWait = 10000;
    private long timeout = 60 * 60 * 1000;
    private int capacity = 10000;
    private int batchSize = 10;

    public void setPoolThreads(int poolThreads) { this.poolThreads = poolThreads; }
    public int getPoolThreads() { return this.poolThreads; }
    public void setPurgeWait(long purgeWait) { this.purgeWait = purgeWait; }
    public long getPurgeWait() { return purgeWait; }
    public void setTimeout(long timeout) { this.timeout = timeout; }
    public long getTimeout() { return timeout; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public int getCapacity() { return this.capacity; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
    public int getBatchSize() { return batchSize; }
}
