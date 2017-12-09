package net.deckerego.docidx.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import org.springframework.data.elasticsearch.annotations.Document;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Document(indexName = "docidx")
public class FileEntry {
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

    @Override
    public String toString() {
        return String.format("FileEntry[ ID: %s, File: %s, Modified %s ]",
                this.id, this.fileName, this.lastModified);
    }
}
