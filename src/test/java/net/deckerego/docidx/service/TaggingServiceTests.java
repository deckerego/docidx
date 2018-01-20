package net.deckerego.docidx.service;

import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayF32;
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
        tagTemplate.template = UtilImageIO.loadImage(System.getProperty("user.dir"), "src/test/docs/template.png", GrayF32.class);
        tagTemplate.name = "myTag";

        when(tagTemplateRepository.findAll()).thenReturn(Arrays.asList(tagTemplate));
        when(taggingConfig.getThreshold()).thenReturn(0.9);

        taggingService.initTemplates();
        File file = new File(System.getProperty("user.dir"), "src/test/docs/test.png");
        Set<FileEntry.Tag> tags = taggingService.tag(file, "image/png");

        Assertions.assertThat(tags).contains(new FileEntry.Tag("myTag", 0.0));
    }

    @Test
    public void positiveMatchPDF() {
        TagTemplate tagTemplate = new TagTemplate();
        tagTemplate.template = UtilImageIO.loadImage(System.getProperty("user.dir"), "src/test/docs/template.png", GrayF32.class);
        tagTemplate.name = "myTag";

        when(tagTemplateRepository.findAll()).thenReturn(Arrays.asList(tagTemplate));
        when(taggingConfig.getThreshold()).thenReturn(0.9);

        taggingService.initTemplates();
        File file = new File(System.getProperty("user.dir"), "src/test/docs/test.pdf");
        Set<FileEntry.Tag> tags = taggingService.tag(file, "application/pdf");

        Assertions.assertThat(tags).contains(new FileEntry.Tag("myTag", 0.0));
    }

    @Test
    public void pickTheBest() {
        TagTemplate tagTemplateOne = new TagTemplate();
        tagTemplateOne.template = UtilImageIO.loadImage(System.getProperty("user.dir"), "src/test/docs/template.png", GrayF32.class);
        tagTemplateOne.name = "goodTag";

        TagTemplate tagTemplateTwo = new TagTemplate();
        tagTemplateTwo.template = UtilImageIO.loadImage(System.getProperty("user.dir"), "src/test/docs/template_bad.png", GrayF32.class);
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
        tagTemplate.template = UtilImageIO.loadImage(System.getProperty("user.dir"), "src/test/docs/template_bad.png", GrayF32.class);
        tagTemplate.name = "myTag";

        when(tagTemplateRepository.findAll()).thenReturn(Arrays.asList(tagTemplate));
        when(taggingConfig.getThreshold()).thenReturn(0.3);

        taggingService.initTemplates();
        File file = new File(System.getProperty("user.dir"), "src/test/docs/test.pdf");
        Set<FileEntry.Tag> tags = taggingService.tag(file, "application/pdf");

        Assertions.assertThat(tags).doesNotContain(new FileEntry.Tag("myTag", 0.0));
    }

    @Test
    public void negativeMatchMismatchedDimensions() {
        TagTemplate tagTemplate = new TagTemplate();
        tagTemplate.template = UtilImageIO.loadImage(System.getProperty("user.dir"), "src/test/docs/template_bad_big.png", GrayF32.class);
        tagTemplate.name = "myTag";

        when(tagTemplateRepository.findAll()).thenReturn(Arrays.asList(tagTemplate));
        when(taggingConfig.getThreshold()).thenReturn(0.01);

        taggingService.initTemplates();
        File file = new File(System.getProperty("user.dir"), "src/test/docs/test.pdf");
        Set<FileEntry.Tag> tags = taggingService.tag(file, "application/pdf");

        Assertions.assertThat(tags).doesNotContain(new FileEntry.Tag("myTag", 0.0));
    }

    @Test
    public void mergeTags() {
        FileEntry.Tag tagOne = new FileEntry.Tag("testOne", 0.1);
        FileEntry.Tag tagTwo = new FileEntry.Tag("testTwo", 0.2);
        FileEntry.Tag tagThree = new FileEntry.Tag("testOne", 0.3);
        FileEntry.Tag tagFour = new FileEntry.Tag("testFour", 0.4);

        Set<FileEntry.Tag> setOne = new HashSet<>();
        setOne.add(tagOne);
        setOne.add(tagTwo);
        setOne.add(tagThree);

        Set<FileEntry.Tag> setTwo = new HashSet<>();
        setTwo.add(tagThree);
        setTwo.add(tagFour);

        Set<FileEntry.Tag> setThree = TaggingService.merge(setOne, setTwo);

        Assertions.assertThat(setThree.size()).isEqualTo(3);

        Map<String, Double> mapThree = new HashMap<>();
        for(FileEntry.Tag tag : setThree) mapThree.put(tag.name, tag.score);

        Assertions.assertThat(mapThree).containsOnlyKeys("testOne", "testTwo", "testFour");
        Assertions.assertThat(mapThree.get("testOne")).isEqualTo(0.3);
    }
}
