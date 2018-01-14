package net.deckerego.docidx.service;

import net.deckerego.docidx.configuration.CrawlerConfig;
import net.deckerego.docidx.configuration.ParserConfig;
import net.deckerego.docidx.model.FileEntry;
import net.deckerego.docidx.model.TaggingTask;
import net.deckerego.docidx.model.ThumbnailTask;
import net.deckerego.docidx.model.TikaTask;
import net.deckerego.docidx.util.WorkBroker;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.DefaultParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Consumer;

@Service
public class TikaService {
    private static final Logger LOG = LoggerFactory.getLogger(TikaService.class);

    @Autowired
    public WorkBroker workBroker;

    @Autowired
    public CrawlerConfig crawlerConfig;

    @Autowired
    public ParserConfig parserConfig;

    private Parser documentParser;
    private ParseContext parserContext;

    @PostConstruct
    public void initBroker() {
        this.workBroker.handle(TikaTask.class, task -> {
            LOG.info(String.format("Starting Tika parsing %s", task.file));
            long startTime = System.currentTimeMillis();

            FileEntry entry = this.parse(task.file);

            LOG.info(String.format("Completed Tika parsing %s in %d seconds", task.file, (System.currentTimeMillis() - startTime) / 1000));
            workBroker.publish(new TaggingTask(entry));
            workBroker.publish(new ThumbnailTask(entry));
        });
    }

    @PostConstruct
    private void createContext() {
        DefaultParser defaultParser = new DefaultParser();

        if(parserConfig.getEnableOcr()) {
            PDFParser pdfParser = new PDFParser();
            pdfParser.setOcrStrategy("ocr_and_text");
            this.documentParser = new AutoDetectParser(defaultParser, pdfParser);
        } else {
            this.documentParser = new AutoDetectParser(defaultParser);
        }
        LOG.info("Created Tika OCR Parser");

        TesseractOCRConfig config = new TesseractOCRConfig();
        config.setLanguage(this.parserConfig.getOcrLanguage());
        config.setTimeout(this.parserConfig.getOcrTimeoutSeconds());

        this.parserContext = new ParseContext();
        this.parserContext.set(Parser.class, this.documentParser);
        this.parserContext.set(TesseractOCRConfig.class, config);
        LOG.info("Created Tika OCR Context");
    }

    public void submit(Collection<Path> files) {
        LOG.debug(String.format("Submitting parse of %s", files));
        for (Path file : files)
            this.workBroker.publish(new TikaTask(file));
    }

    public FileEntry parse(Path file) {
        Path rootPath = Paths.get(crawlerConfig.getRootPath());
        Path parentPath = rootPath.relativize(file.getParent());

        FileEntry entry = new FileEntry();
        entry.parentPath = parentPath.toString();
        entry.fileName = file.getFileName().toString();
        entry.lastModified = new Date(file.toFile().lastModified());
        entry.id = DigestUtils.md5Hex(file.toString());
        entry.indexUpdated = Calendar.getInstance().getTime();

        ContentHandler body = new BodyContentHandler();
        Metadata metadata = new Metadata();

        try {
            InputStream fis = new FileInputStream(file.toFile());
            documentParser.parse(fis, body, metadata, parserContext);
        } catch(IOException e) {
            LOG.error(String.format("Could not read file %s", file.toString()), e);
        } catch(SAXException e) {
            LOG.warn(String.format("Error parsing %s to XML", file.toString()), e);
        } catch(TikaException e) {
            LOG.error(String.format("Error parsing file %s", file.toString()), e);
        } finally {
            entry.body = body.toString();
            entry.metadata = new HashMap<>();
            for (String prop : metadata.names())
                entry.metadata.put(prop, metadata.get(prop));
        }

        return entry;
    }
}
