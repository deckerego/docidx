package net.deckerego.docidx.repository;

import net.deckerego.docidx.configuration.ElasticConfig;
import net.deckerego.docidx.model.FileEntry;
import net.deckerego.docidx.util.BatchProcessingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;

@Repository
public class QueuedDocumentRepository {
    private static final Logger LOG = LoggerFactory.getLogger(QueuedDocumentRepository.class);

    @Autowired
    ElasticConfig elasticConfig;

    @Autowired
    DocumentRepository documentRepository;

    BatchProcessingQueue<FileEntry> updateBatchQueue;
    BatchProcessingQueue<FileEntry> deleteBatchQueue;

    @PostConstruct
    public void initQueues() {
        this.updateBatchQueue = new BatchProcessingQueue<>(this.documentRepository::saveAll,
                elasticConfig.batchSize,
                elasticConfig.batchSize * 1000,
                elasticConfig.batchWaitMillis);

        this.deleteBatchQueue = new BatchProcessingQueue<>(this.documentRepository::deleteAll,
                elasticConfig.batchSize,
                elasticConfig.batchSize * 1000,
                elasticConfig.batchWaitMillis);
    }

    //FIXME Need to fix dates for indexing
    public boolean offerUpdate(FileEntry entry) {
        LOG.trace(String.format("Offering update for %s", entry.toString()));
        return this.updateBatchQueue.offer(entry);
    }

    public boolean offerDelete(FileEntry entry) {
        LOG.trace(String.format("Offering delete for %s", entry.toString()));
        return this.deleteBatchQueue.offer(entry);
    }

    public List<FileEntry> findByParentPath(String parent) {
        return this.documentRepository.findByParentPath(parent);
    }
}
