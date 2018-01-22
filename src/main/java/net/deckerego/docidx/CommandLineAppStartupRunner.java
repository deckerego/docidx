package net.deckerego.docidx;

import net.deckerego.docidx.configuration.CrawlerConfig;
import net.deckerego.docidx.repository.IndexStatsRepository;
import net.deckerego.docidx.service.CrawlerService;
import net.deckerego.docidx.service.TaggingService;
import net.deckerego.docidx.util.WorkBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class CommandLineAppStartupRunner implements CommandLineRunner {
    private static final Logger LOG = LoggerFactory.getLogger(CommandLineAppStartupRunner.class);

    @Autowired
    private CrawlerConfig crawlerConfig;

    @Autowired
    private CrawlerService crawlerService;

    @Autowired
    private WorkBroker workBroker;

    private boolean isRunning = true;

    @Override
    public void run(String... args) {
        while(this.isRunning) {
            long startTime = System.currentTimeMillis();

            this.crawlerService.crawl();

            try {
                this.workBroker.waitUntilComplete();

                LOG.info(String.format("Published %d and consumed %d messages in %d seconds",
                        this.workBroker.getPublishCount(), this.workBroker.getConsumedCount(), (System.currentTimeMillis() - startTime) / 1000));
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
