package net.deckerego.docidx.service;

import net.deckerego.docidx.configuration.CrawlerConfig;
import net.deckerego.docidx.model.DocumentActions;
import net.deckerego.docidx.model.FileEntry;
import net.deckerego.docidx.model.ParentEntry;
import net.deckerego.docidx.repository.DocumentRepository;
import net.deckerego.docidx.util.WorkBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CrawlerService {
    private static final Logger LOG = LoggerFactory.getLogger(CrawlerService.class);

    @Autowired
    public CrawlerConfig crawlerConfig;

    @Autowired
    public TikaService tikaService;

    @Autowired
    public DocumentRepository documentRepository;

    @Autowired
    public WorkBroker workBroker;

    private AtomicLong addCount = new AtomicLong(0);
    private AtomicLong modCount = new AtomicLong(0);
    private AtomicLong delCount = new AtomicLong(0);

    @PostConstruct
    public void initBroker() {
        this.workBroker.handleBatch(FileEntry.class, this.documentRepository::saveAll);
        this.workBroker.handle(ParentEntry.class, this::routeFiles);
    }

    public void crawl() {
        Path cwd = Paths.get(crawlerConfig.getRootPath());
        try {
            Files.walkFileTree(cwd, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attrs) {
                    if(Files.isReadable(directory)) {
                        LOG.debug(String.format("Submitting parent entry %s", directory.toString()));
                        workBroker.publish(new ParentEntry(directory));
                        return FileVisitResult.CONTINUE;
                    } else {
                        LOG.warn(String.format("Could not read %s, skipping", directory.toString()));
                        return FileVisitResult.CONTINUE;
                    }
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    LOG.error(String.format("Could not access %s, skipping", file.toString()));
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch(IOException e) {
            LOG.error(String.format("Fatal exception when finding dirs under %s", cwd), e);
        }
    }

    public Map<String, FileEntry> getDocuments(Path path) {
        List<FileEntry> files = this.documentRepository.findByParentPath(path.toString());
        LOG.debug(String.format("Found %d documents for %s", files.size(), path.toString()));
        return files.stream().collect(Collectors.toMap(e -> e.fileName, Function.identity()));
    }

    public Map<String, Path> getFiles(Path path) {
        Map<String, Path> files = new HashMap<>();

        try (Stream<Path> fsStream = Files.find(path, 1, (p, a) -> a.isRegularFile())) {
            files = fsStream.collect(Collectors.toMap(p -> p.getFileName().toString(), Function.identity()));
        } catch (IOException e) {
            LOG.error(String.format("Fatal exception when finding files under %s", path), e);
        }

        LOG.debug(String.format("Found %d files for %s", files.size(), path.getFileName()));
        return files;
    }

    public DocumentActions merge(Path parent, Map<String, FileEntry> documents, Map<String, Path> files) {
        DocumentActions actions = new DocumentActions(parent);

        actions.additions = files.keySet().stream()
                .filter(f -> ! documents.containsKey(f))
                .map(files::get).collect(Collectors.toSet());
        addCount.addAndGet(actions.additions.size());
        LOG.debug(String.format("Found %d additions for %s", actions.additions.size(), parent.getFileName().toString()));

        actions.updates = files.keySet().stream()
                .filter(f -> documents.containsKey(f) && documents.get(f).lastModified < files.get(f).toFile().lastModified())
                .map(files::get).collect(Collectors.toSet());
        modCount.addAndGet(actions.updates.size());
        LOG.debug(String.format("Found %d updates for %s", actions.updates.size(), parent.getFileName().toString()));

        actions.deletions = documents.keySet().stream()
                .filter(f -> ! files.containsKey(f))
                .map(documents::get).collect(Collectors.toSet());
        delCount.addAndGet(actions.deletions.size());
        LOG.debug(String.format("Found %d deletions for %s", actions.deletions.size(), parent.getFileName().toString()));

        return actions;
    }

    private void routeFiles(ParentEntry parent) {
        CompletableFuture<Map<String, FileEntry>> futureDocuments = CompletableFuture.supplyAsync(() -> getDocuments(parent.directory));
        CompletableFuture<Map<String, Path>> futureFiles = CompletableFuture.supplyAsync(() -> getFiles(parent.directory));
        CompletableFuture<DocumentActions> futureEntries = futureDocuments.thenCombine(futureFiles, (d, p) -> merge(parent.directory, d, p));

        futureEntries
                .whenComplete((actions, ex) -> {
                    if(ex != null) LOG.error(String.format("Error mapping additions for %s", parent.toString()), ex);
                    else tikaService.submit(actions.additions, workBroker::publish);
                })
                .whenComplete((actions, ex) -> {
                    if(ex != null) LOG.error(String.format("Error mapping updates for %s", parent.toString()), ex);
                    else tikaService.submit(actions.updates, workBroker::publish);
                })
                .whenComplete((actions, ex) -> {
                    if(ex != null) LOG.error(String.format("Error mapping deletions for %s", parent.toString()), ex);
                    else documentRepository.deleteAll(actions.deletions);
                });
    }

    public long getAddCount() { return this.addCount.get(); }
    public long getModCount() { return this.modCount.get(); }
    public long getDelCount() { return this.delCount.get(); }
}
