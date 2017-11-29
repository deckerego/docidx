package net.deckerego.docidx.service;

import net.deckerego.docidx.model.FileEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CrawlerService {
    private static final Logger LOG = LoggerFactory.getLogger(CrawlerService.class);

    public void crawl(String rootPath) {
        SubmissionPublisher<Path> publisher = new SubmissionPublisher<>();
        publisher.subscribe(new CrawlSubscriber());
        Path cwd = FileSystems.getDefault().getPath(rootPath);

        try(Stream<Path> fsStream = Files.find(cwd, Integer.MAX_VALUE, (p, a) -> a.isDirectory())) {
            fsStream.forEach(file -> publisher.offer(file, (sub, msg) -> true));
        } catch(IOException e) {
            LOG.error(String.format("Fatal exception when finding dirs under %s", cwd), e);
        }
    }

    private List<FileEntry> getDocuments(Path path) {
        List<FileEntry> files = List.of();

        return files;
    }

    private List<Path> getFiles(Path path) {
        List<Path> files = List.of();

        try(Stream<Path> fsStream = Files.find(path, 1, (p, a) -> a.isRegularFile())) {
            files = fsStream.collect(Collectors.toList());
        } catch(IOException e) {
            LOG.error(String.format("Fatal exception when finding files under %s", path), e);
        }

        LOG.info(String.format("Found %d files for %s", files.size(), path.getFileName()));
        return files;
    }

    private List<FileEntry> merge(List<FileEntry> entries, List<Path> paths) {
        List<FileEntry> files = paths.stream().map(p -> new FileEntry(p)).collect(Collectors.toList());

        return files;
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
            CompletableFuture<List<FileEntry>> futureDocuments = CompletableFuture.supplyAsync(() -> getDocuments(message));
            CompletableFuture<List<Path>> futureFiles = CompletableFuture.supplyAsync(() -> getFiles(message));
            CompletableFuture<List<FileEntry>> futureEntries = futureDocuments.thenCombine(futureFiles, (d, p) -> merge(d, p));

            futureEntries.whenComplete((val, ex) -> LOG.info(val.toString()));
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
