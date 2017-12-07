package net.deckerego.docidx.repository;

import net.deckerego.docidx.configuration.ElasticConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.then;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { QueuedDocumentRepository.class, ElasticConfig.class })
public class QueuedDocumentRepositoryTests {

    @MockBean
    private DocumentRepository documentRepository;

    @Autowired
    private QueuedDocumentRepository queuedDocumentRepository;

    @Test
    public void testConstruct() {
        assertThat(this.queuedDocumentRepository).isNotNull();
        then(this.documentRepository).shouldHaveZeroInteractions();
    }
}
