package net.deckerego.docidx.service;

import net.deckerego.docidx.configuration.CrawlerConfig;
import net.deckerego.docidx.configuration.ElasticConfig;
import net.deckerego.docidx.model.DocumentActions;
import net.deckerego.docidx.model.FileEntry;
import net.deckerego.docidx.model.ParentEntry;
import net.deckerego.docidx.repository.DocumentRepository;
import net.deckerego.docidx.util.WorkBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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
    public ElasticConfig elasticConfig;

    @Autowired
    public TikaService tikaService;

    @Autowired
    public ThumbnailService thumbnailService;

    @Autowired
    public TaggingService taggingService;

    @Autowired
    public DocumentRepository documentRepository;

    @Autowired
    public WorkBroker workBroker;

    private AtomicLong addCount = new AtomicLong(0);
    private AtomicLong modCount = new AtomicLong(0);
    private AtomicLong unmodCount = new AtomicLong(0);
    private AtomicLong delCount = new AtomicLong(0);

    @PostConstruct
    public void initBroker() {
        this.workBroker.handleBatch(FileEntry.class, this.documentRepository::saveAll);
        this.workBroker.handle(ParentEntry.class, this::routeFiles);
    }

    public void crawl(boolean updateTags) {
        //Reset our diagnostic counters
        this.addCount.set(0L);
        this.modCount.set(0L);
        this.unmodCount.set(0L);
        this.delCount.set(0L);

        //Walk the given directory and issue ParentEntry messages for later processing
        Path cwd = Paths.get(crawlerConfig.getRootPath());
        try {
            Files.walkFileTree(cwd, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attrs) {
                    try {
                        if(crawlerConfig.getSkipHidden() && Files.isHidden(directory)) {
                            LOG.debug(String.format("Skipping hidden directory %s", directory));
                        } else if (Files.isReadable(directory)) {
                            LOG.debug(String.format("Submitting parent entry %s", directory.toString()));
                            workBroker.publish(new ParentEntry(directory, updateTags));
                        } else {
                            LOG.warn(String.format("Could not read %s, skipping", directory.toString()));
                        }
                    } catch (IOException e) {
                        LOG.error(String.format("IO Exception trying to determine attributes of %s", directory.toString()), e);
                    } finally {
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
        Path rootPath = Paths.get(crawlerConfig.getRootPath());
        Path parentPath = rootPath.relativize(path);

        //Stream support is really weird in Spring Data ES right now, so fall back to Pageable
        Map<String, FileEntry> fileMap = new HashMap<>();
        int currentPage = 0;

        //Fetch the first page
        PageRequest pageable = PageRequest.of(currentPage++, elasticConfig.getMaxResults());
        Page<FileEntry> files = this.documentRepository.findByParentPath(parentPath.toString(), pageable);
        files.forEach(f -> fileMap.put(f.fileName, f));

        //Fetch any additional pages
        while(currentPage < files.getTotalPages()) {
            LOG.debug(String.format("Found over %d document results, fetching page %d", elasticConfig.getMaxResults(), currentPage));
            pageable = PageRequest.of(currentPage++, elasticConfig.getMaxResults());
            files = this.documentRepository.findByParentPath(parentPath.toString(), pageable);
            files.forEach(f -> fileMap.put(f.fileName, f));
        }

        LOG.debug(String.format("Found %d documents for %s", fileMap.size(), parentPath.toString()));
        return fileMap;
    }

    public Map<String, Path> getFiles(Path path) {
        Map<String, Path> files = new HashMap<>();

        try (Stream<Path> fsStream = Files.find(path, 1, (p, a) -> a.isRegularFile())) {
            files = fsStream
                    .filter(f -> {
                        try {
                            return ! (crawlerConfig.getSkipHidden() && Files.isHidden(f));
                        } catch(IOException e) {
                            LOG.error(String.format("IO error trying to determine if file %s is hidden", f.toString()), e);
                            return true;
                        }
                    })
                    .collect(Collectors.toMap(p -> p.getFileName().toString(), Function.identity()));
        } catch (IOException e) {
            LOG.error(String.format("Fatal exception when finding files under %s", path), e);
        }

        LOG.debug(String.format("Found %d files for %s", files.size(), path.getFileName()));
        return files;
    }

    private boolean isBefore(FileEntry document, Path file) {
        Date fileLastModified = new Date(file.toFile().lastModified());
        return document.lastModified.before(fileLastModified);
    }

    public DocumentActions merge(Path parent, Map<String, FileEntry> documents, Map<String, Path> files, boolean updateTags) {
        DocumentActions actions = new DocumentActions(parent, updateTags);

        actions.additions = files.keySet().stream()
                .filter(f -> ! documents.containsKey(f))
                .map(files::get).collect(Collectors.toSet());
        addCount.addAndGet(actions.additions.size());
        LOG.debug(String.format("Found %d additions for %s", actions.additions.size(), parent.getFileName().toString()));

        actions.updates = files.keySet().stream()
                .filter(f -> documents.containsKey(f) && isBefore(documents.get(f), files.get(f)))
                .map(files::get).collect(Collectors.toSet());
        modCount.addAndGet(actions.updates.size());
        LOG.debug(String.format("Found %d updates for %s", actions.updates.size(), parent.getFileName().toString()));

        actions.unmodified = files.keySet().stream()
                .filter(f -> documents.containsKey(f) && ! isBefore(documents.get(f), files.get(f)))
                .map(files::get).collect(Collectors.toSet());
        unmodCount.addAndGet(actions.unmodified.size());
        LOG.debug(String.format("Found %d unmodified for %s", actions.unmodified.size(), parent.getFileName().toString()));

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
        CompletableFuture<DocumentActions> futureEntries = futureDocuments.thenCombine(futureFiles, (d, p) -> merge(parent.directory, d, p, parent.updateTags));

        try {
            futureEntries //Submit new document
                    .whenComplete((actions, ex) -> tikaService.submit(actions.additions))
                    .whenComplete((actions, ex) -> tikaService.submit(actions.updates))
                    .whenComplete((actions, ex) -> documentRepository.deleteAll(actions.deletions))
                    .get();

            futureEntries //Submit re-tagging work
                    .whenComplete((actions, ex) -> { if(actions.matchTagging) taggingService.submit(actions.unmodified); })
                    .get();
        } catch(InterruptedException e) {
            LOG.error(String.format("Interrupted while routing files for %s", parent.toString()), e);
        } catch(ExecutionException e) {
            LOG.error(String.format("Error when trying to route files for %s", parent.toString()), e);
        }
    }

    public long getAddCount() { return this.addCount.get(); }
    public long getModCount() { return this.modCount.get(); }
    public long getUnmodCount() { return this.unmodCount.get(); }
    public long getDelCount() { return this.delCount.get(); }
}
