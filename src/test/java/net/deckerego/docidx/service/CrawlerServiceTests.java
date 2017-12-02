package net.deckerego.docidx.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CrawlerServiceTests {
    @Autowired
    private CrawlerService crawlSvc;

    @Test
    public void directoryStreamCollector() {
        crawlSvc.crawl("tests");
        try { //FIXME Because it's all background processing
            Thread.sleep(5000);
        } catch(InterruptedException e) {
            fail("Error during hackish sleep");
        }
    }
}
