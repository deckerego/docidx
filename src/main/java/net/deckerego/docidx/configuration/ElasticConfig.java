package net.deckerego.docidx.configuration;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
@ConfigurationProperties(prefix = "elasticsearch")
@EnableElasticsearchRepositories("net.deckerego.docidx.repository")
public class ElasticConfig {
    public String host;
    public int port;
    public String cluster;
    public int batchSize = 10;
    public long batchWaitMillis = 10000;

    public String getHost() { return this.host; }
    public void setHost(String host) { this.host = host; }
    public int getPort() { return this.port; }
    public void setPort(int port) { this.port = port; }
    public String getCluster() { return this.cluster; }
    public void setCluster(String cluster) { this.cluster = cluster; }
    public int getBatchSize() { return this.batchSize; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
    public long getBatchWaitMillis() { return this.batchWaitMillis; }
    public void setBatchWaitMillis(long batchWaitMillis) { this.batchWaitMillis = batchWaitMillis; }

    @Bean
    ElasticsearchOperations elasticsearchTemplate() throws UnknownHostException {
        Settings settings = Settings.builder()
                .put("cluster.name", cluster)
                .build();
        Client client = new PreBuiltTransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
        return new ElasticsearchTemplate(client);
    }
}
