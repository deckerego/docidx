package net.deckerego.docidx;

import net.deckerego.docidx.service.CrawlerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DocIndexApplicationTests {
    @Autowired
    private CrawlerService crawlSvc;

    @Test
    public void directoryStreamCollector() {
        List<Path> result = crawlSvc.crawl();

        assertThat(result.size()).isGreaterThan(0);
    }
}
