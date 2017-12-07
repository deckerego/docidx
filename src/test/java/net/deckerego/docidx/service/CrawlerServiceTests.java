package net.deckerego.docidx.service;

import net.deckerego.docidx.repository.QueuedDocumentRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { CrawlerService.class })
public class CrawlerServiceTests {

    @MockBean
    private TikaService tikaService;

    @MockBean
    private QueuedDocumentRepository documentRepository;

    @Autowired
    private CrawlerService crawlerService;

    @Test
    public void directoryStreamCollector() {
        this.crawlerService.crawl("tests");
        then(this.tikaService).should().submit(ArgumentMatchers.anyCollection(), any());
        then(this.documentRepository).should().offerUpdate(any());
    }
}
