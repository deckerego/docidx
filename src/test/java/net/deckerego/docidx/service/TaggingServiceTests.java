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

import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TaggingService.class)
public class TaggingServiceTests {

    @Autowired
    private TaggingService taggingService;

    @MockBean
    private TagTemplateRepository tagTemplateRepository;

    @Test
    public void positiveMatch() {
        File template = new File(System.getProperty("user.dir"), "src/test/docs/test.jpg");
        Mat templateImage = Imgcodecs.imread(template.getAbsolutePath());
        Map<Mat, String> templates = new HashMap<>();
        templates.put(templateImage, "myTag");
        when(tagTemplateRepository.getAllTemplates()).thenReturn(templates);

        File file = new File(System.getProperty("user.dir"), "src/test/docs/test.jpg");
        String tag = taggingService.tag(file, "image/jpeg");

        Assertions.assertThat(tag).isEqualTo("myTag");
    }
}
