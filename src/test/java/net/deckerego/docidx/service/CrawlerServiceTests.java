package net.deckerego.docidx.service;

import net.deckerego.docidx.repository.DocumentRepository;
import net.deckerego.docidx.util.WorkBroker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { CrawlerService.class, WorkBroker.class })
public class CrawlerServiceTests {

    @MockBean
    private TikaService tikaService;

    @MockBean
    private DocumentRepository documentRepository;

    @MockBean
    private WorkBroker workBroker;

    @Autowired
    private CrawlerService crawlerService;

    @Test
    public void directoryStreamCollector() {
        this.crawlerService.crawl("tests");

        then(this.workBroker).should().publish(any(Path.class));
    }
}
