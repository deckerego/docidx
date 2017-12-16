package net.deckerego.docidx.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import javax.imageio.ImageIO;
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
@Mapping(mappingPath = "fileentry-mapping.json")
@Setting(settingPath = "docidx-settings.json")
public class FileEntry {
    private static final Logger LOG = LoggerFactory.getLogger(FileEntry.class);
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    public static final BufferedImage blankImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

    @Id
    public String id;
    public String parentPath;
    public String fileName;
    public Long lastModified;
    public String body;
    public Map<String, String> metadata;
    public BufferedImage thumbnail;

    public FileEntry() {
        this.id = "NOENTRY";
        this.parentPath = "";
        this.fileName = "";
        this.lastModified = new Long(0L);
        this.body = "";
        this.metadata = new HashMap<>(0);
        this.thumbnail = blankImage;
    }

    public String getLastModified() {
        return dateFormat.format(new Date(this.lastModified));
    }

    public void setLastModified(String lastModified) {
        try {
            this.lastModified = dateFormat.parse(lastModified).getTime();
        } catch(NumberFormatException e) {
            LOG.warn(String.format("Couldn't parse lastModified %s for ID %s, defaulting to epoch", lastModified, this.id), e);
            this.lastModified = new Long(0);
        } catch(ParseException e) {
            LOG.error(String.format("Couldn't deserialize lastModified %s for ID %s", lastModified, this.id), e);
            this.lastModified = new Long(0);
        }
    }

    public byte[] getThumbnail() {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(this.thumbnail, "PNG", outputStream);
            return outputStream.toByteArray();
        } catch(IOException e) {
            LOG.error(String.format("Could not serialize thumbnail of %s", this.id), e);
            return new byte[0];
        }
    }

    public void setThumbnail(byte[] image) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(image);
            this.thumbnail = ImageIO.read(inputStream);
        } catch(IOException e) {
            LOG.error(String.format("Could not deserialize thumbnail of %s", this.id), e);
            this.thumbnail = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        }
    }

    @Override
    public String toString() {
        return String.format("FileEntry[ ID: %s, File: %s, Modified %s ]",
                this.id, this.fileName, this.lastModified);
    }
}
