package net.deckerego.docidx.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import org.apache.commons.imaging.ImageFormat;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.annotations.Document;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Document(indexName = "docidx", type = "fileentry")
public class FileEntry {
    private static final Logger LOG = LoggerFactory.getLogger(FileEntry.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public String id;
    public String parentPath;
    public String fileName;
    public Long lastModified;
    public String body;
    public Map<String, String> metadata;
    public BufferedImage thumbnail;

    @JsonGetter("lastModified")
    public String getLastModified() {
        return dateFormat.format(new Date(this.lastModified));
    }

    @JsonSetter("lastModified")
    public void setLastModified(String lastModified) {
        try {
            this.lastModified = dateFormat.parse(lastModified).getTime();
        } catch(ParseException e) {
            LOG.error(String.format("Could not deserialize lastModified date of %s", this.id), e);
            this.lastModified = null;
        }
    }

    @JsonGetter("thumbnail")
    public byte[] getThumbnail() {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Imaging.writeImage(this.thumbnail, outputStream, ImageFormat.IMAGE_FORMAT_PNG, new HashMap<>());
            return outputStream.toByteArray();
        } catch(IOException e) {
            LOG.error(String.format("Could not serialize thumbnail of %s", this.id), e);
            return new byte[0];
        } catch(ImageWriteException e) {
            LOG.error(String.format("Could not write thumbnail of %s", this.id), e);
            return new byte[0];
        }
    }

    @JsonSetter("thumbnail")
    public void setThumbnail(byte[] image) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(image);
            this.thumbnail = Imaging.getBufferedImage(inputStream);
        } catch(IOException e) {
            LOG.error(String.format("Could not deserialize thumbnail of %s", this.id), e);
            this.thumbnail = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        } catch(ImageReadException e) {
            LOG.error(String.format("Could not read thumbnail of %s", this.id), e);
            this.thumbnail = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        }
    }

    @Override
    public String toString() {
        return String.format("FileEntry[ ID: %s, File: %s, Modified %s ]",
                this.id, this.fileName, this.lastModified);
    }
}
