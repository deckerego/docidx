package net.deckerego.docidx.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.annotations.Document;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    @Override
    public String toString() {
        return String.format("FileEntry[ ID: %s, File: %s, Modified %s ]",
                this.id, this.fileName, this.lastModified);
    }
}
