package net.deckerego.docidx.service;

import net.deckerego.docidx.configuration.CrawlerConfig;
import net.deckerego.docidx.configuration.TaggingConfig;
import net.deckerego.docidx.model.FileEntry;
import net.deckerego.docidx.model.TagTemplate;
import net.deckerego.docidx.repository.DocumentRepository;
import net.deckerego.docidx.repository.TagTemplateRepository;
import net.deckerego.docidx.util.WorkBroker;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.*;

import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TaggingService.class)
public class TaggingServiceTests {

    @Autowired
    private TaggingService taggingService;

    @MockBean
    private TagTemplateRepository tagTemplateRepository;

    @MockBean
    private TaggingConfig taggingConfig;

    @MockBean
    private DocumentRepository documentRepository;

    @MockBean
    private CrawlerConfig crawlerConfig;

    @MockBean
    private WorkBroker workBroker;

    @Test
    public void positiveMatchPNG() {
        TagTemplate tagTemplate = new TagTemplate();
        File template = new File(System.getProperty("user.dir"), "src/test/docs/template.png");
        tagTemplate.template = Imgcodecs.imread(template.getAbsolutePath());
        tagTemplate.name = "myTag";

        when(tagTemplateRepository.findAll()).thenReturn(Arrays.asList(tagTemplate));
        when(taggingConfig.getThreshold()).thenReturn(0.99);

        taggingService.initTemplates();
        File file = new File(System.getProperty("user.dir"), "src/test/docs/test.png");
        Set<FileEntry.Tag> tags = taggingService.tag(file, "image/png");

        Assertions.assertThat(tags).contains(new FileEntry.Tag("myTag", 0.0));
    }

    @Test
    public void positiveMatchPDF() {
        TagTemplate tagTemplate = new TagTemplate();
        File template = new File(System.getProperty("user.dir"), "src/test/docs/template.png");
        tagTemplate.template = Imgcodecs.imread(template.getAbsolutePath());
        tagTemplate.name = "myTag";

        when(tagTemplateRepository.findAll()).thenReturn(Arrays.asList(tagTemplate));
        when(taggingConfig.getThreshold()).thenReturn(0.91);

        taggingService.initTemplates();
        File file = new File(System.getProperty("user.dir"), "src/test/docs/test.pdf");
        Set<FileEntry.Tag> tags = taggingService.tag(file, "application/pdf");

        Assertions.assertThat(tags).contains(new FileEntry.Tag("myTag", 0.0));
    }

    @Test
    public void pickTheBest() {
        TagTemplate tagTemplateOne = new TagTemplate();
        File templateGood = new File(System.getProperty("user.dir"), "src/test/docs/template.png");
        tagTemplateOne.template = Imgcodecs.imread(templateGood.getAbsolutePath());
        tagTemplateOne.name = "goodTag";

        TagTemplate tagTemplateTwo = new TagTemplate();
        File templateBad = new File(System.getProperty("user.dir"), "src/test/docs/template_bad.png");
        tagTemplateTwo.template = Imgcodecs.imread(templateBad.getAbsolutePath());
        tagTemplateTwo.name = "badTag";

        when(tagTemplateRepository.findAll()).thenReturn(Arrays.asList(tagTemplateOne, tagTemplateTwo));
        when(taggingConfig.getThreshold()).thenReturn(0.8);

        taggingService.initTemplates();
        File file = new File(System.getProperty("user.dir"), "src/test/docs/test.png");
        Set<FileEntry.Tag> tags = taggingService.tag(file, "image/png");

        Assertions.assertThat(tags).containsOnly(new FileEntry.Tag("goodTag", 0.0));
    }

    @Test
    public void negativeMatchExpectedDimensions() {
        TagTemplate tagTemplate = new TagTemplate();
        File template = new File(System.getProperty("user.dir"), "src/test/docs/template_bad.png");
        tagTemplate.template = Imgcodecs.imread(template.getAbsolutePath());
        tagTemplate.name = "myTag";

        when(tagTemplateRepository.findAll()).thenReturn(Arrays.asList(tagTemplate));
        when(taggingConfig.getThreshold()).thenReturn(0.32);

        taggingService.initTemplates();
        File file = new File(System.getProperty("user.dir"), "src/test/docs/test.pdf");
        Set<FileEntry.Tag> tags = taggingService.tag(file, "application/pdf");

        Assertions.assertThat(tags).doesNotContain(new FileEntry.Tag("myTag", 0.0));
    }

    @Test
    public void negativeMatchMismatchedDimensions() {
        TagTemplate tagTemplate = new TagTemplate();
        File template = new File(System.getProperty("user.dir"), "src/test/docs/template_bad_big.png");
        tagTemplate.template = Imgcodecs.imread(template.getAbsolutePath());
        tagTemplate.name = "myTag";

        when(tagTemplateRepository.findAll()).thenReturn(Arrays.asList(tagTemplate));
        when(taggingConfig.getThreshold()).thenReturn(0.01);

        taggingService.initTemplates();
        File file = new File(System.getProperty("user.dir"), "src/test/docs/test.pdf");
        Set<FileEntry.Tag> tags = taggingService.tag(file, "application/pdf");

        Assertions.assertThat(tags).doesNotContain(new FileEntry.Tag("myTag", 0.0));
    }
}
