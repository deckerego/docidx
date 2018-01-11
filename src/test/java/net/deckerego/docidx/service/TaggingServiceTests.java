package net.deckerego.docidx.service;

import net.deckerego.docidx.repository.TagTemplateRepository;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TaggingService.class)
public class TaggingServiceTests {

    @Autowired
    private TaggingService taggingService;

    @MockBean
    private TagTemplateRepository tagTemplateRepository;

    @Test
    public void positiveMatchPNG() {
        File template = new File(System.getProperty("user.dir"), "src/test/docs/template.png");
        Mat templateImage = Imgcodecs.imread(template.getAbsolutePath());
        Map<Mat, String> templates = new HashMap<>();
        templates.put(templateImage, "myTag");
        when(tagTemplateRepository.getAllTemplates()).thenReturn(templates);

        File file = new File(System.getProperty("user.dir"), "src/test/docs/test.png");
        Set<String> tags = taggingService.tag(file, "image/png");

        Assertions.assertThat(tags).contains("myTag");
    }

    @Test
    public void positiveMatchPDF() {
        File template = new File(System.getProperty("user.dir"), "src/test/docs/template.png");
        Mat templateImage = Imgcodecs.imread(template.getAbsolutePath());
        Map<Mat, String> templates = new HashMap<>();
        templates.put(templateImage, "myTag");
        when(tagTemplateRepository.getAllTemplates()).thenReturn(templates);

        File file = new File(System.getProperty("user.dir"), "src/test/docs/test.pdf");
        Set<String> tags = taggingService.tag(file, "application/pdf");

        Assertions.assertThat(tags).contains("myTag");
    }

    @Test
    public void negativeMatchExpectedDimensions() {
        File template = new File(System.getProperty("user.dir"), "src/test/docs/template_bad.png");
        Mat templateImage = Imgcodecs.imread(template.getAbsolutePath());
        Map<Mat, String> templates = new HashMap<>();
        templates.put(templateImage, "myTag");
        when(tagTemplateRepository.getAllTemplates()).thenReturn(templates);

        File file = new File(System.getProperty("user.dir"), "src/test/docs/test.pdf");
        Set<String> tags = taggingService.tag(file, "application/pdf");

        Assertions.assertThat(tags).doesNotContain("myTag");
    }

    @Test
    public void negativeMatchMismatchedDimensions() {
        File template = new File(System.getProperty("user.dir"), "src/test/docs/template_bad_big.png");
        Mat templateImage = Imgcodecs.imread(template.getAbsolutePath());
        Map<Mat, String> templates = new HashMap<>();
        templates.put(templateImage, "myTag");
        when(tagTemplateRepository.getAllTemplates()).thenReturn(templates);

        File file = new File(System.getProperty("user.dir"), "src/test/docs/test.pdf");
        Set<String> tags = taggingService.tag(file, "application/pdf");

        Assertions.assertThat(tags).doesNotContain("myTag");
    }
}
