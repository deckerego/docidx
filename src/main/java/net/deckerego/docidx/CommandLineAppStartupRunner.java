package net.deckerego.docidx;

import net.deckerego.docidx.configuration.CrawlerConfig;
import net.deckerego.docidx.model.FileEntry;
import net.deckerego.docidx.model.TagTemplate;
import net.deckerego.docidx.repository.DocumentRepository;
import net.deckerego.docidx.repository.TagTemplateRepository;
import net.deckerego.docidx.service.CrawlerService;
import net.deckerego.docidx.util.WorkBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Component
public class CommandLineAppStartupRunner implements CommandLineRunner {
    private static final Logger LOG = LoggerFactory.getLogger(CommandLineAppStartupRunner.class);

    @Autowired
    private CrawlerConfig crawlerConfig;

    @Autowired
    private CrawlerService crawlerService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private TagTemplateRepository tagTemplateRepository;

    @Autowired
    private WorkBroker workBroker;

    private boolean isRunning = true;

    @Override
    public void run(String... args) {
        FileEntry latestDoc = documentRepository.findFirstByOrderByIndexUpdatedDesc();
        long lastIndexUpdate = latestDoc != null ? latestDoc.indexUpdated.getTime() : 0L;

        while(this.isRunning) {
            long startTime = System.currentTimeMillis();
            TagTemplate latestTemplate = tagTemplateRepository.findFirstByOrderByIndexUpdatedDesc();
            long lastTagUpdate = latestTemplate != null ? latestTemplate.indexUpdated.getTime() : 0L;

            this.crawlerService.crawl(lastIndexUpdate <= lastTagUpdate);

            try {
                this.workBroker.waitUntilComplete();
                lastIndexUpdate = System.currentTimeMillis();

                LOG.info(String.format("Published %d and consumed %d messages in %d seconds",
                        this.workBroker.getPublishCount(), this.workBroker.getConsumedCount(), (lastIndexUpdate - startTime) / 1000));
                LOG.info(String.format("Added %d, Modified %d, Unmodified %d, Deleted %d records",
                        this.crawlerService.getAddCount(), this.crawlerService.getModCount(), this.crawlerService.getUnmodCount(), this.crawlerService.getDelCount()));
                LOG.info(String.format("Indexing complete, will resume in %d seconds", this.crawlerConfig.getWaitSeconds()));

                Thread.sleep(this.crawlerConfig.getWaitSeconds() * 1000L);
            } catch(InterruptedException e) {
                LOG.info("Crawling service interrupted, shutting down now...");
                this.isRunning = false;
                this.workBroker.shutdown();
            }
        }

        this.workBroker.shutdown();
    }
}
