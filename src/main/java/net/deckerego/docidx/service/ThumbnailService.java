package net.deckerego.docidx.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Service
public class ThumbnailService {
    private static final Logger LOG = LoggerFactory.getLogger(ThumbnailService.class);
    private final BufferedImage blankImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

    BufferedImage render(File file, String type, float scale) {
        BufferedImage image;

        try {
            if (type.contains(MediaType.APPLICATION_PDF_VALUE)) {
                image = renderPDF(file, scale);
            } else if (type.contains(MediaType.IMAGE_JPEG_VALUE)
                    || type.contains(MediaType.IMAGE_PNG_VALUE)
                    || type.contains(MediaType.IMAGE_GIF_VALUE)) {
                image = renderImage(file, scale);
            } else {
                image = blankImage;
            }
        } catch(IOException e) {
            LOG.error(String.format("Couldn't generate thumbnail for %s type %s", file.toString(), type), e);
            image = blankImage;
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
