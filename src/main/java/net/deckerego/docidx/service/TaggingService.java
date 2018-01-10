package net.deckerego.docidx.service;

import net.deckerego.docidx.repository.TagTemplateRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
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
import java.util.Map;

@Service
public class TaggingService {
    private static final Logger LOG = LoggerFactory.getLogger(TaggingService.class);

    @Autowired
    public TagTemplateRepository tagTemplateRepository;

    public String tag(File file, String type) {
        LOG.trace(String.format("Attempting to tag file %s", file.getAbsolutePath()));
        Mat targetImage = null;

        try {
            if (type.contains(MediaType.APPLICATION_PDF_VALUE)) {
                targetImage = renderPDF(file);
            } else if (type.contains(MediaType.IMAGE_JPEG_VALUE)
                    || type.contains(MediaType.IMAGE_PNG_VALUE)
                    || type.contains(MediaType.IMAGE_GIF_VALUE)) {
                targetImage = renderImage(file);
            }
        } catch(IOException e) {
            LOG.error(String.format("Couldn't generate target image for %s type %s", file.toString()), e);
        }

        if(targetImage == null) {
            LOG.error(String.format("Could not create target image for %s", file.toString()));
            return null;
        }

        String bestTag = null;

        //TODO Move to fork/join or stream
        for (Map.Entry<Mat, String> entry : tagTemplateRepository.getAllTemplates().entrySet()) {
            Mat result = new Mat();
            Imgproc.matchTemplate(targetImage, entry.getKey(), result, Imgproc.TM_CCOEFF_NORMED);
            Core.normalize( result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat() );
            Core.MinMaxLocResult location = Core.minMaxLoc(result);
            LOG.debug(String.format("Found template match at max(%d, %d), min(%d, %d)",
                    location.maxLoc.x, location.maxLoc.y, location.minLoc.x, location.minLoc.y));

            //FIXME Just a pretend value for now until we figure out what location should be
            bestTag = entry.getValue();
        }

        return bestTag;
    }

    private Mat renderPDF(File file) throws IOException {
        PDDocument doc = PDDocument.load(file);
        PDFRenderer renderer = new PDFRenderer(doc);
        BufferedImage image = renderer.renderImage(0, 1.0F);
        doc.close();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", outputStream);

        Mat rendered = new Mat(image.getWidth(), image.getHeight(), CvType.CV_8UC3);
        rendered.put(0, 0, outputStream.toByteArray());
        return rendered;
    }

    private Mat renderImage(File file) {
        return Imgcodecs.imread(file.getAbsolutePath());
    }
}
