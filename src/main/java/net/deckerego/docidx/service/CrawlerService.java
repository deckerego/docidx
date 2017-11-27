package net.deckerego.docidx.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CrawlerService {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    public List<Path> crawl() {
        Path cwd = FileSystems.getDefault().getPath(".");
        List<Path> result = new ArrayList<>();

        try(Stream<Path> fsStream = Files.walk(cwd)) {
            result = fsStream
                    .map(a -> a.getFileName())
                    .collect(Collectors.toList());
        } catch(IOException e) {
            LOG.error("Fatal exception when crawling "+cwd.toString(), e);
        }

        return result;
    }
}
