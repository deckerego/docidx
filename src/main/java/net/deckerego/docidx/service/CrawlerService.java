package net.deckerego.docidx.service;

import net.deckerego.docidx.model.FileEntry;
import net.deckerego.docidx.repository.QueuedDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CrawlerService {
    private static final Logger LOG = LoggerFactory.getLogger(CrawlerService.class);

    @Autowired
    public TikaService tikaService;

    @Autowired
    public QueuedDocumentRepository documentRepository;

    public void crawl(String rootPath) {
        SubmissionPublisher<Path> publisher = new SubmissionPublisher<>();
        publisher.subscribe(new CrawlSubscriber());
        Path cwd = FileSystems.getDefault().getPath(rootPath);

        try(Stream<Path> fsStream = Files.find(cwd, Integer.MAX_VALUE, (p, a) -> a.isDirectory())) {
            fsStream.forEach(file -> publisher.offer(file, (sub, msg) -> true));
        } catch(IOException e) {
            LOG.error(String.format("Fatal exception when finding dirs under %s", cwd), e);
        } finally {
            publisher.close();
        }
    }

    private Map<String, FileEntry> getDocuments(Path path) {
        List<FileEntry> files = this.documentRepository.findByParentPath(path.toString());
        return files.stream().collect(Collectors.toMap(e -> e.fileName, Function.identity()));
    }

    private Map<String, Path> getFiles(Path path) {
        Map<String, Path> files = new HashMap<>();

        try (Stream<Path> fsStream = Files.find(path, 1, (p, a) -> a.isRegularFile())) {
            files = fsStream.collect(Collectors.toMap(p -> p.getFileName().toString(), Function.identity()));
        } catch (IOException e) {
            LOG.error(String.format("Fatal exception when finding files under %s", path), e);
        }

        LOG.debug(String.format("Found %d files for %s", files.size(), path.getFileName()));
        return files;
    }

    private DocumentActions merge(Path parent, Map<String, FileEntry> documents, Map<String, Path> files) {
        DocumentActions actions = new DocumentActions(parent);

        actions.additions = files.keySet().stream()
                .filter(f -> ! documents.containsKey(f))
                .map(files::get).collect(Collectors.toSet());
        LOG.debug(String.format("Found %d additions for %s", actions.additions.size(), parent.getFileName().toString()));

        actions.updates = files.keySet().stream()
                .filter(f -> documents.containsKey(f) && documents.get(f).lastModified < files.get(f).toFile().lastModified())
                .map(files::get).collect(Collectors.toSet());
        LOG.debug(String.format("Found %d updates for %s", actions.updates.size(), parent.getFileName().toString()));

        actions.deletions = documents.keySet().stream()
                .filter(f -> ! files.containsKey(f))
                .map(documents::get).collect(Collectors.toSet());
        LOG.debug(String.format("Found %d deletions for %s", actions.deletions.size(), parent.getFileName().toString()));

        return actions;
    }

    private class DocumentActions {
        public Path directory;
        public Set<FileEntry> deletions;
        public Set<Path> additions;
        public Set<Path> updates;

        public DocumentActions(Path directory) {
            this.directory = directory;
            this.deletions = new HashSet<>();
            this.additions = new HashSet<>();
            this.updates = new HashSet<>();
        }

        @Override
        public String toString() {
            return String.format("For %s\nAdditions: %s\nUpdates: %s\nDeletions: %s",
                    directory.getFileName().toString(), additions, updates, deletions);
        }
    }

    private class CrawlSubscriber implements Flow.Subscriber<Path> {
        private Flow.Subscription subscription;

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription= subscription;
            this.subscription.request(1);
        }

        @Override
        public void onNext(Path message) {
            CompletableFuture<Map<String, FileEntry>> futureDocuments = CompletableFuture.supplyAsync(() -> getDocuments(message));
            CompletableFuture<Map<String, Path>> futureFiles = CompletableFuture.supplyAsync(() -> getFiles(message));
            CompletableFuture<DocumentActions> futureEntries = futureDocuments.thenCombine(futureFiles, (d, p) -> merge(message, d, p));

            futureEntries
                    .whenComplete((actions, ex) -> tikaService.submit(actions.additions, documentRepository::offerUpdate))
                    .whenComplete((actions, ex) -> tikaService.submit(actions.updates, documentRepository::offerUpdate))
                    .whenComplete((actions, ex) -> actions.deletions.forEach(documentRepository::offerDelete));
            subscription.request(1);
        }

        @Override
        public void onComplete() {
            LOG.info("Completed CrawlSubscriber");
        }

        @Override
        public void onError(Throwable t) {
            LOG.error("Error when processing CrawlSubscription message", t);
        }
    }
}
