package net.deckerego.docidx.configuration;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOG = LoggerFactory.getLogger(ElasticConfig.class);

    private String host;
    private int port;
    private String cluster;
    private int batchSize = 10;
    private long batchWaitMillis = 10000;
    private int maxResults = 10000;

    public void setHost(String host) { this.host = host; }
    public String getHost() { return this.host; }
    public void setPort(int port) { this.port = port; }
    public int getPort() { return this.port; }
    public void setCluster(String cluster) { this.cluster = cluster; }
    public String getCluster() { return this.cluster; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
    public int getBatchSize() { return this.batchSize; }
    public void setBatchWaitMillis(long batchWaitMillis) { this.batchWaitMillis = batchWaitMillis; }
    public long getBatchWaitMillis() { return this.batchWaitMillis; }
    public void setMaxResults(int maxResults) { this.maxResults = maxResults; }
    public int getMaxResults() { return this.maxResults; }

    private void waitForClientConnection(Client client) throws InterruptedException {
        boolean portClosed = true;

        while(portClosed) {
            try {
                client.admin().cluster().health(new ClusterHealthRequest());
                portClosed = false;
            } catch (NoNodeAvailableException e) {
                if(LOG.isDebugEnabled()) LOG.warn(String.format("Could not connect to %s:%d, retrying...", host, port), e);
                else LOG.warn(String.format("Could not connect to %s:%d, retrying...", host, port));
                Thread.sleep(1000);
            }
        }
    }

    @Bean
    ElasticsearchOperations elasticsearchTemplate() throws UnknownHostException {
        Settings settings = Settings.builder()
                .put("cluster.name", cluster)
                .build();
        Client client = new PreBuiltTransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));

        try {
            waitForClientConnection(client);
        } catch(InterruptedException e) {
            LOG.error(String.format("Couldn't wait to connect to %s:%d", host, port), e);
        }

        return new ElasticsearchTemplate(client);
    }
}
