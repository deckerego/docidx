package net.deckerego.docidx;

import net.deckerego.docidx.configuration.CrawlerConfig;
import net.deckerego.docidx.service.CrawlerService;
import net.deckerego.docidx.util.WorkBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CommandLineAppStartupRunner implements CommandLineRunner {
    private static final Logger LOG = LoggerFactory.getLogger(CommandLineAppStartupRunner.class);

    @Autowired
    private CrawlerService crawlerService;

    @Autowired
    private WorkBroker workBroker;

    @Override
    public void run(String... args) throws Exception {
        long startTime = System.currentTimeMillis();

        this.crawlerService.crawl();
        this.workBroker.awaitShutdown();

        long elapsedTime = System.currentTimeMillis() - startTime;

        LOG.info("Indexing complete!");
        LOG.info(String.format("Published %d and consumed %d messages in %d seconds",
                this.workBroker.getPublishCount(), this.workBroker.getConsumedCount(), elapsedTime / 1000));
        LOG.info(String.format("Added %d, Modified %d, Deleted %d records",
                this.crawlerService.getAddCount(), this.crawlerService.getModCount(), this.crawlerService.getDelCount()));
    }
}
