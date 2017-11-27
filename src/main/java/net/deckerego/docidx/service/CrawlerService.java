package net.deckerego.docidx.service;

import net.deckerego.docidx.model.FileEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Service
public class CrawlerService {
    private static final Logger LOG = LoggerFactory.getLogger(CrawlerService.class);

    public void crawl() {
        Path cwd = FileSystems.getDefault().getPath(".");

        try(Stream<Path> fsStream = Files.walk(cwd)) {
            fsStream.forEach(CrawlerService::action);
        } catch(IOException e) {
            LOG.error("Fatal exception when crawling "+cwd.toString(), e);
        }
    }

    private static void action(Path path) {
        if(path.getParent() == null) return; // Root directory
        FileEntry entry = new FileEntry(path);
        LOG.info(entry.toString());
    }
}
