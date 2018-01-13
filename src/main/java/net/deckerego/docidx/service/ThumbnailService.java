package net.deckerego.docidx.service;

import net.deckerego.docidx.configuration.CrawlerConfig;
import net.deckerego.docidx.model.FileEntry;
import net.deckerego.docidx.model.ThumbnailTask;
import net.deckerego.docidx.model.TikaTask;
import net.deckerego.docidx.repository.DocumentRepository;
import net.deckerego.docidx.util.WorkBroker;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Consumer;

@Service
public class ThumbnailService {
    private static final Logger LOG = LoggerFactory.getLogger(ThumbnailService.class);

    @Autowired
    public WorkBroker workBroker;

    @Autowired
    public CrawlerConfig crawlerConfig;


    @PostConstruct
    public void initBroker() {
        this.workBroker.handle(ThumbnailTask.class, task -> {
            long startTime = System.currentTimeMillis();

            String contentType = task.document.metadata.getOrDefault("Content-Type", "application/octet-stream");
            File relativeFile = new File(task.document.parentPath, task.document.fileName);
            File absoluteFile = new File(crawlerConfig.getRootPath(), relativeFile.getPath());
            LOG.info(String.format("Starting thumbnail rendering %s", relativeFile.toString()));
            task.document.thumbnail = this.render(absoluteFile, contentType, 0.5F);

            LOG.info(String.format("Completed thumbnail rendering %s in %d seconds", task.document.fileName, (System.currentTimeMillis() - startTime) / 1000));
            workBroker.publish(task.document); //TODO Is Spring Data smart enough to do partial updates?
        });
    }

    public BufferedImage render(File file, String type, float scale) {
        BufferedImage image;

        try {
            if (type.contains(MediaType.APPLICATION_PDF_VALUE)) {
                image = renderPDF(file, scale);
            } else if (type.contains(MediaType.IMAGE_JPEG_VALUE)
                    || type.contains(MediaType.IMAGE_PNG_VALUE)
                    || type.contains(MediaType.IMAGE_GIF_VALUE)) {
                image = renderImage(file, scale);
            } else {
                image = FileEntry.blankImage;
            }
        } catch(IOException e) {
            LOG.error(String.format("Couldn't generate thumbnail for %s type %s", file.toString(), type), e);
            image = FileEntry.blankImage;
        }

        if(image != null && image.getHeight() >= 32) {
            return image.getSubimage(0, 0, image.getWidth(), image.getHeight() / 2);
        } else {
            return image;
        }
    }

    private BufferedImage renderPDF(File file, float scale) throws IOException {
        PDDocument doc = PDDocument.load(file);
        PDFRenderer renderer = new PDFRenderer(doc);
        BufferedImage image = renderer.renderImage(0, scale);
        doc.close();
        return image;
    }

    private BufferedImage renderImage(File file, float scale) throws IOException {
        float finalScale = scale / 2; // Double the reduction in scale. Because I said so.

        BufferedImage image = ImageIO.read(file);
        BufferedImage transformedImage =
                new BufferedImage((int)(image.getWidth() * finalScale), (int)(image.getHeight() * finalScale), BufferedImage.TYPE_INT_ARGB);

        AffineTransform transform = new AffineTransform();
        transform.scale(finalScale, finalScale);
        AffineTransformOp scaleOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        return scaleOp.filter(image, transformedImage);
    }
}
