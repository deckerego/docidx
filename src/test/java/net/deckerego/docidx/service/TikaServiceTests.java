package net.deckerego.docidx.service;

import net.deckerego.docidx.configuration.ParserConfig;
import net.deckerego.docidx.model.TikaTask;
import net.deckerego.docidx.util.WorkBroker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.FileSystems;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { TikaService.class, ParserConfig.class })
public class TikaServiceTests {
    @MockBean
    private WorkBroker workBroker;

    @Autowired
    private ParserConfig parserConfig;

    @Autowired
    private TikaService tikaSvc;

    @Test
    public void submitFiles() {
        this.tikaSvc.submit(List.of(FileSystems.getDefault().getPath("./README.md")), e -> assertThat(e).isNotNull());
        then(this.workBroker).should().publish(any(TikaTask.class));
    }
}
