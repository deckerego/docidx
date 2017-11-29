package net.deckerego.docidx.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CrawlerServiceTests {
    @Autowired
    private CrawlerService crawlSvc;

    @Test
    public void directoryStreamCollector() {
        crawlSvc.crawl();
    }
}
