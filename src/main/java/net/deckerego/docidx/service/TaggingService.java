package net.deckerego.docidx.service;

import net.deckerego.docidx.configuration.TaggingConfig;
import net.deckerego.docidx.model.FileEntry;
import net.deckerego.docidx.model.TagTemplate;
import net.deckerego.docidx.repository.TagTemplateRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TaggingService {
    private static final Logger LOG = LoggerFactory.getLogger(TaggingService.class);

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @Autowired
    public TagTemplateRepository tagTemplateRepository;

    @Autowired
    public TaggingConfig taggingConfig;

    private Collection<TagTemplate> tagTemplates;

    @PostConstruct
    public void initTemplates() {
        this.tagTemplates = new ArrayList<>();
        for(TagTemplate tagTemplate : this.tagTemplateRepository.findAll()) {
            this.tagTemplates.add(tagTemplate);
        }
    }

    public Set<FileEntry.Tag> tag(File file, String type) {
        LOG.trace(String.format("Attempting to tag file %s", file.getAbsolutePath()));
        long startTime = System.currentTimeMillis();
        Set<FileEntry.Tag> tags = new HashSet<>();

        try {
            final Mat targetImage;

            if (type.contains(MediaType.APPLICATION_PDF_VALUE)) {
                targetImage = renderPDF(file);
            } else if (type.contains(MediaType.IMAGE_JPEG_VALUE)
                    || type.contains(MediaType.IMAGE_PNG_VALUE)
                    || type.contains(MediaType.IMAGE_GIF_VALUE)) {
                targetImage = renderImage(file);
            } else {
                LOG.error(String.format("Couldn't render target image for %s type %s", file.toString(), type));
                targetImage = null;
            }

            tags = this.tagTemplates.stream()
                    .map(tt -> new FileEntry.Tag(tt.name, templateScore(tt.template, targetImage)))
                    .filter(ft -> ft.score >= taggingConfig.getThreshold())
                    .collect(Collectors.toSet());

            if(LOG.isDebugEnabled()) {
                long elapsedMillis = System.currentTimeMillis() - startTime;
                LOG.debug(String.format("Searched %s for %d templates in %f seconds", file.getName(), this.tagTemplates.size(), elapsedMillis / 1000.0));
            }
        } catch(IOException e) {
            LOG.error(String.format("Couldn't generate target image for %s type %s", file.toString(), type), e);
        }

        return tags;
    }

    private double templateScore(Mat template, Mat image) {
        if(image == null) {
            LOG.warn("Null image will not be matched against templates");
            return 0.0;
        }

        if(template.width() > image.width() || template.height() > image.height()) {
            LOG.warn(String.format("Mismatched template size: template is %d x %d but image is %d x %d",
                    template.width(), template.height(), image.width(), image.height()));
            return 0.0;
        }

        Mat result = new Mat();
        Imgproc.matchTemplate(image, template, result, Imgproc.TM_CCOEFF_NORMED);

        Core.MinMaxLocResult matchResult = Core.minMaxLoc(result);
        LOG.debug(String.format("Template %d%% match at (%f, %f) for image (%d, %d)",
                (int) (matchResult.maxVal * 100), matchResult.maxLoc.x, matchResult.maxLoc.y, image.width(), image.height()));

        return matchResult.maxVal;
    }

    private Mat renderPDF(File file) throws IOException {
        PDDocument doc = PDDocument.load(file);
        PDFRenderer renderer = new PDFRenderer(doc);
        BufferedImage image = renderer.renderImage(0, 1.0F);
        doc.close();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "BMP", outputStream);

        return Imgcodecs.imdecode(new MatOfByte(outputStream.toByteArray()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
    }

    private Mat renderImage(File file) {
        return Imgcodecs.imread(file.getAbsolutePath());
    }
}
