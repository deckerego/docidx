package net.deckerego.docidx.repository;

import net.deckerego.docidx.configuration.ElasticConfig;
import net.deckerego.docidx.model.FileEntry;
import net.deckerego.docidx.util.BatchProcessingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;

@Repository
public class DocumentRepository {
    private static final Logger LOG = LoggerFactory.getLogger(DocumentRepository.class);

    @Autowired
    ElasticsearchOperations elasticsearchTemplate;

    @Autowired
    ElasticConfig elasticConfig;

    BatchProcessingQueue<FileEntry> updateBatchQueue;
    BatchProcessingQueue<FileEntry> deleteBatchQueue;

    @PostConstruct
    public void initQueues() {
        this.updateBatchQueue = new BatchProcessingQueue<>(this::batchUpdate,
                elasticConfig.batchSize,
                elasticConfig.batchSize * 1000,
                elasticConfig.batchWaitMillis);

        this.deleteBatchQueue = new BatchProcessingQueue<>(this::batchDelete,
                elasticConfig.batchSize,
                elasticConfig.batchSize * 1000,
                elasticConfig.batchWaitMillis);
    }

    public boolean createOrUpdate(FileEntry entry) {
        return this.updateBatchQueue.offer(entry);
    }

    public boolean delete(FileEntry entry) {
        return this.deleteBatchQueue.offer(entry);
    }

    public List<FileEntry> findAllByParent(String parent) {
        return List.of();
    }

    private void batchUpdate(List<FileEntry> entries) {
        LOG.info("Batch Updating to ES:");
        LOG.info(entries.toString());
    }

    private void batchDelete(List<FileEntry> entries) {
        LOG.info("Batch Deleting from ES:");
        LOG.info(entries.toString());
    }
}
