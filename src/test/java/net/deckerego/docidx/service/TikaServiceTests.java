package net.deckerego.docidx.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.FileSystems;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { TikaService.class })
public class TikaServiceTests {
    @Autowired
    private TikaService tikaSvc;

    @Test
    public void submitFiles() {
        tikaSvc.submit(List.of(FileSystems.getDefault().getPath("./README.md")), e -> assertThat(e).isNotNull());
    }
}
