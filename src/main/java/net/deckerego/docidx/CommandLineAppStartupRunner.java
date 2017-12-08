package net.deckerego.docidx;

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
        crawlerService.crawl("tests");
        workBroker.waitUntilEmpty();
        LOG.info("Indexing completed!");
    }
}
