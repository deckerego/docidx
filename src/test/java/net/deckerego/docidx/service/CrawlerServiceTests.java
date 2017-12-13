package net.deckerego.docidx.service;

import net.deckerego.docidx.configuration.CrawlerConfig;
import net.deckerego.docidx.model.DocumentActions;
import net.deckerego.docidx.model.FileEntry;
import net.deckerego.docidx.model.ParentEntry;
import net.deckerego.docidx.repository.DocumentRepository;
import net.deckerego.docidx.util.WorkBroker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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

    @MockBean
    private CrawlerConfig crawlerConfig;

    @Test
    public void directoryStreamCollector() {
        when(crawlerConfig.getRootPath()).thenReturn("tests");

        this.crawlerService.crawl();

        then(this.workBroker).should().publish(any(ParentEntry.class));
    }

    @Test
    public void testMergeEverything() {
        FileEntry deletion01 = new FileEntry();
        deletion01.lastModified = 123412543124L;
        deletion01.parentPath = "tests";
        deletion01.fileName = "deletion01.pdf";
        deletion01.id = "FEEDFACE33373";

        FileEntry existing01 = new FileEntry();
        existing01.lastModified = 123412543024L;
        existing01.parentPath = "tests";
        existing01.fileName = "existing01.pdf";
        existing01.id = "FEEDFACE33773";

        File addition_file01 = mock(File.class);
        when(addition_file01.lastModified()).thenReturn(122412543124L);

        Path addition01 = mock(Path.class);
        when(addition01.getFileName()).thenReturn(addition01);
        when(addition01.toFile()).thenReturn(addition_file01);
        when(addition01.toString()).thenReturn("addition01.pdf");

        File existing_file02 = mock(File.class);
        when(existing_file02.lastModified()).thenReturn(133412543024L);

        Path existing02 = mock(Path.class);
        when(existing02.getFileName()).thenReturn(existing02);
        when(existing02.toFile()).thenReturn(existing_file02);
        when(existing02.toString()).thenReturn("existing01.pdf");

        Path parent = FileSystems.getDefault().getPath("tests");

        Map<String, FileEntry> documents = new HashMap<>();
        documents.put(deletion01.fileName, deletion01);
        documents.put(existing01.fileName, existing01);

        Map<String, Path> files = new HashMap<>();
        files.put(addition01.getFileName().toString(), addition01);
        files.put(existing02.getFileName().toString(), existing02);

        DocumentActions actions = crawlerService.merge(parent, documents, files);

        assertThat(actions).isNotNull();
        assertThat(actions.additions.size()).isEqualTo(1);
        assertThat(actions.additions.contains("addition01.pdf"));
        assertThat(actions.updates.size()).isEqualTo(1);
        assertThat(actions.updates.contains("existing01.pdf"));
        assertThat(actions.deletions.size()).isEqualTo(1);
        assertThat(actions.deletions.contains("deletion01.pdf"));
    }

    @Test
    public void testMergeDeletions() {
        FileEntry deletion01 = new FileEntry();
        deletion01.lastModified = 123412543124L;
        deletion01.parentPath = "tests";
        deletion01.fileName = "deletion01.pdf";
        deletion01.id = "FEEDFACE33373";

        Path parent = FileSystems.getDefault().getPath("tests");

        Map<String, FileEntry> documents = new HashMap<>();
        documents.put(deletion01.fileName, deletion01);

        Map<String, Path> files = new HashMap<>();

        DocumentActions actions = crawlerService.merge(parent, documents, files);

        assertThat(actions).isNotNull();
        assertThat(actions.additions.size()).isEqualTo(0);
        assertThat(actions.updates.size()).isEqualTo(0);
        assertThat(actions.deletions.size()).isEqualTo(1);
        assertThat(actions.deletions.contains("deletion01.pdf"));
    }

    @Test
    public void testMergeModifications() {
        FileEntry existing01 = new FileEntry();
        existing01.lastModified = 123412543124L;
        existing01.parentPath = "tests";
        existing01.fileName = "existing01.pdf";
        existing01.id = "FEEDFACE33373";

        File existing_file02 = mock(File.class);
        when(existing_file02.lastModified()).thenReturn(133412543024L);

        Path existing02 = mock(Path.class);
        when(existing02.getFileName()).thenReturn(existing02);
        when(existing02.toFile()).thenReturn(existing_file02);
        when(existing02.toString()).thenReturn("existing01.pdf");

        Path parent = FileSystems.getDefault().getPath("tests");

        Map<String, FileEntry> documents = new HashMap<>();
        documents.put(existing01.fileName, existing01);

        Map<String, Path> files = new HashMap<>();
        files.put(existing02.getFileName().toString(), existing02);

        DocumentActions actions = crawlerService.merge(parent, documents, files);

        assertThat(actions).isNotNull();
        assertThat(actions.additions.size()).isEqualTo(0);
        assertThat(actions.deletions.size()).isEqualTo(0);
        assertThat(actions.updates.size()).isEqualTo(1);
        assertThat(actions.updates.contains("existing01.pdf"));
    }

    @Test
    public void testMergeAdditions() {
        Path addition01 = mock(Path.class);
        when(addition01.getFileName()).thenReturn(addition01);
        when(addition01.toString()).thenReturn("addition01.pdf");

        Path parent = FileSystems.getDefault().getPath("tests");

        Map<String, FileEntry> documents = new HashMap<>();

        Map<String, Path> files = new HashMap<>();
        files.put(addition01.getFileName().toString(), addition01);

        DocumentActions actions = crawlerService.merge(parent, documents, files);

        assertThat(actions).isNotNull();
        assertThat(actions.updates.size()).isEqualTo(0);
        assertThat(actions.deletions.size()).isEqualTo(0);
        assertThat(actions.additions.size()).isEqualTo(1);
        assertThat(actions.additions.contains("addition01.pdf"));
    }

    @Test
    public void testGetDocuments() {
        FileEntry existing01 = new FileEntry();
        existing01.lastModified = 123412543124L;
        existing01.parentPath = "tests";
        existing01.fileName = "existing01.pdf";
        existing01.id = "FEEDFACE33373";

        FileEntry existing02 = new FileEntry();
        existing02.lastModified = 123412543114L;
        existing02.parentPath = "tests";
        existing02.fileName = "existing02.pdf";
        existing02.id = "FEEDFACE33374";

        List<FileEntry> results = new ArrayList<>();
        results.add(existing01);
        results.add(existing02);

        when(documentRepository.findByParentPath("tests")).thenReturn(results);

        Path parent = FileSystems.getDefault().getPath("tests");
        Map<String, FileEntry> docs = crawlerService.getDocuments(parent);

        assertThat(docs.size()).isEqualTo(2);
        assertThat(docs).containsKeys("existing01.pdf", "existing02.pdf");
    }

    @Test
    public void testGetFiles() {
        Path parent = FileSystems.getDefault().getPath("tests");
        Map<String, Path> files = crawlerService.getFiles(parent);

        assertThat(files.size()).isGreaterThan(0);
        assertThat(files).containsKeys("ScannedBill_20171104.pdf", "ScannedBill_20171204.pdf");
    }
}
