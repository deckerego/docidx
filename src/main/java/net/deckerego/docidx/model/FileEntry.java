package net.deckerego.docidx.model;

import org.springframework.data.elasticsearch.annotations.Document;

import java.util.Date;
import java.util.Map;

@Document(indexName = "docidx")
public class FileEntry {
    public String id;
    public String parentPath;
    public String fileName;
    public Long lastModified;
    public String body;
    public Map<String, String> metadata;

    @Override
    public String toString() {
        return String.format("FileEntry[ Parent: %s, Last Modified %Tc ]",
                this.fileName, new Date(this.lastModified));
    }
}
