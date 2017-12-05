package net.deckerego.docidx.service;

import net.deckerego.docidx.model.FileEntry;
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
import org.springframework.stereotype.Service;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Service
public class TikaService {
    private static final int CORE_COUNT = Runtime.getRuntime().availableProcessors();
    private static final Logger LOG = LoggerFactory.getLogger(TikaService.class);

    //TODO Move this to config value
    private ThreadPoolExecutor threadPool;
    private int capacity;
    private Parser documentParser;
    private ParseContext parserContext;

    public TikaService() {
        this.capacity = 1000; //TODO Move to config
        this.threadPool = new ThreadPoolExecutor(CORE_COUNT, CORE_COUNT, 3600, TimeUnit.SECONDS, new ArrayBlockingQueue<>(capacity));
        this.documentParser = createParser();
        this.parserContext = createContext(this.documentParser);
    }

    private static Parser createParser() {
        PDFParser pdfParser = new PDFParser();
        pdfParser.setOcrStrategy("ocr_and_text");

        DefaultParser defaultParser = new DefaultParser();
        LOG.info("Created Tika OCR Parser");

        return new AutoDetectParser(defaultParser, pdfParser);
    }

    private static ParseContext createContext(Parser parser) {
        TesseractOCRConfig config = new TesseractOCRConfig();
        config.setLanguage("eng");

        ParseContext context = new ParseContext();
        context.set(Parser.class, parser);
        context.set(TesseractOCRConfig.class, config);
        LOG.info("Created Tika OCR Context");

        return context;
    }

    public void submit(Collection<Path> files, Consumer<FileEntry> callback) {
        LOG.debug(String.format("Submitting parse of %s", files));
        for (Path file : files)
            this.threadPool.execute(new TikaTask(file, callback));
    }

    private class TikaTask implements Runnable {
        private Path file;
        private Consumer<FileEntry> callback;

        public TikaTask(Path file, Consumer<FileEntry> callback) {
            this.callback = callback;
            this.file = file;
        }

        public void run() {
            LOG.info(String.format("Starting Tika parsing %s", this.file));
            long startTime = System.currentTimeMillis();
            FileEntry entry = this.parse();
            LOG.info(String.format("Completed Tika parsing %s in %d seconds", this.file, (System.currentTimeMillis() - startTime) / 1000));

            callback.accept(entry);
        }

        private FileEntry parse() {
            FileEntry entry = new FileEntry();
            entry.parentPath = file.getParent().toString();
            entry.fileName = file.getFileName().toString();
            entry.lastModified = file.toFile().lastModified();
            entry.id = DigestUtils.md5Hex(entry.parentPath);

            try {
                ContentHandler body = new BodyContentHandler();
                Metadata metadata = new Metadata();
                InputStream fis = new FileInputStream(file.toFile());

                documentParser.parse(fis, body, metadata, parserContext);

                entry.body = body.toString();
                entry.metadata = new HashMap<>();
                for (String prop : metadata.names())
                    entry.metadata.put(prop, metadata.get(prop));
            } catch(IOException e) {
                LOG.error(String.format("Could not read file %s", this.file.toString()), e);
            } catch(SAXException e) {
                LOG.error(String.format("Could not read XML %s", this.file.toString()), e);
            } catch(TikaException e) {
                LOG.error(String.format("Error parsing file %s", this.file.toString()), e);
            } finally {
                return entry;
            }
        }
    }
}
