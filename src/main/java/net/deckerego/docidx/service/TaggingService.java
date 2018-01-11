package net.deckerego.docidx.service;

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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class TaggingService {
    private static final Logger LOG = LoggerFactory.getLogger(TaggingService.class);

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @Autowired
    public TagTemplateRepository tagTemplateRepository;

    public Set<String> tag(File file, String type) {
        LOG.trace(String.format("Attempting to tag file %s", file.getAbsolutePath()));
        Mat targetImage = null;
        Set<String> tags = new HashSet<>();

        try {
            if (type.contains(MediaType.APPLICATION_PDF_VALUE)) {
                targetImage = renderPDF(file);
            } else if (type.contains(MediaType.IMAGE_JPEG_VALUE)
                    || type.contains(MediaType.IMAGE_PNG_VALUE)
                    || type.contains(MediaType.IMAGE_GIF_VALUE)) {
                targetImage = renderImage(file);
            } else {
                LOG.error(String.format("Couldn't render target image for %s type %s", file.toString(), type));
            }
        } catch(IOException e) {
            LOG.error(String.format("Couldn't generate target image for %s type %s", file.toString(), type), e);
        }

        if(targetImage == null) {
            LOG.error(String.format("Could not create target image for %s", file.toString()));
            return null;
        }

        //TODO Move to fork/join or stream
        for (Map.Entry<Mat, String> entry : tagTemplateRepository.getAllTemplates().entrySet()) {
            if(templateFound(entry.getKey(), targetImage)) tags.add(entry.getValue());
        }

        return tags;
    }

    private boolean templateFound(Mat template, Mat image) {
        if(template.width() > image.width() || template.height() > image.height()) {
            LOG.warn(String.format("Mismatched template size: template is %d x %d but image is %d x %d",
                    template.width(), template.height(), image.width(), image.height()));
            return false;
        }

        Mat result = new Mat();
        Imgproc.matchTemplate(image, template, result, Imgproc.TM_CCOEFF_NORMED);

        Core.MinMaxLocResult matchResult = Core.minMaxLoc(result);
        LOG.debug(String.format("Template %d%% match at min(%f, %f), max(%f, %f) for image (%d, %d)",
                (int) (matchResult.maxVal * 100), matchResult.minLoc.x, matchResult.minLoc.y, matchResult.maxLoc.x, matchResult.maxLoc.y, image.width(), image.height()));

        return matchResult.maxVal > 0.9;
    }

    private Mat renderPDF(File file) throws IOException {
        PDDocument doc = PDDocument.load(file);
        PDFRenderer renderer = new PDFRenderer(doc);
        BufferedImage image = renderer.renderImage(0, 1.0F);
        doc.close();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", outputStream);

        return Imgcodecs.imdecode(new MatOfByte(outputStream.toByteArray()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
    }

    private Mat renderImage(File file) {
        return Imgcodecs.imread(file.getAbsolutePath());
    }
}
