package net.deckerego.docidx.service;

import net.deckerego.docidx.configuration.CrawlerConfig;
import net.deckerego.docidx.repository.DocumentRepository;
import net.deckerego.docidx.util.WorkBroker;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.awt.image.BufferedImage;
import java.io.File;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ThumbnailService.class)
public class ThumbnailServiceTests {

    @Autowired
    private ThumbnailService thumbSvc;

    @MockBean
    private WorkBroker workBroker;

    @MockBean
    private CrawlerConfig crawlerConfig;

    @Test
    public void pdf() {
        File file = new File(System.getProperty("user.dir"), "src/test/docs/test.pdf");
        BufferedImage image = thumbSvc.render(file, "application/pdf", 0.5f);
        Assertions.assertThat(image).isNotNull();
        Assertions.assertThat(image.getHeight()).isGreaterThan(1);
        Assertions.assertThat(image.getWidth()).isGreaterThan(1);
    }

    @Test
    public void jpeg() {
        File file = new File(System.getProperty("user.dir"), "src/test/docs/test.jpg");
        BufferedImage image = thumbSvc.render(file, "image/jpeg", 0.5f);
        Assertions.assertThat(image).isNotNull();
        Assertions.assertThat(image.getHeight()).isGreaterThan(1);
        Assertions.assertThat(image.getWidth()).isGreaterThan(1);
    }

    @Test
    public void gif() {
        File file = new File(System.getProperty("user.dir"), "src/test/docs/test.gif");
        BufferedImage image = thumbSvc.render(file, "image/gif", 0.5f);
        Assertions.assertThat(image).isNotNull();
        Assertions.assertThat(image.getHeight()).isGreaterThan(1);
        Assertions.assertThat(image.getWidth()).isGreaterThan(1);
    }

    @Test
    public void png() {
        File file = new File(System.getProperty("user.dir"), "src/test/docs/test.png");
        BufferedImage image = thumbSvc.render(file, "image/png", 0.5f);
        Assertions.assertThat(image).isNotNull();
        Assertions.assertThat(image.getHeight()).isGreaterThan(1);
        Assertions.assertThat(image.getWidth()).isGreaterThan(1);
    }

    @Test
    public void nothing() {
        File file = new File(System.getProperty("user.dir"), "src/test/docs/test.pdf");
        BufferedImage image = thumbSvc.render(file, "application/binary", 0.5f);
        Assertions.assertThat(image).isNotNull();
        Assertions.assertThat(image.getHeight()).isEqualTo(1);
        Assertions.assertThat(image.getWidth()).isEqualTo(1);
    }
}
