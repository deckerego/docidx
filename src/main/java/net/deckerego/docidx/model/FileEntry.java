package net.deckerego.docidx.model;

import org.apache.commons.codec.digest.DigestUtils;

import java.nio.file.Path;
import java.util.Date;

public class FileEntry {
    private String parentPath;
    private String parentPathHash;
    private String fileName;
    private String fileNameHash;
    private Long lastModified;

    public FileEntry(String parentPath, String fileName, Long lastModified) {
        this.parentPath = parentPath;
        this.fileName = fileName;
        this.lastModified = lastModified;
    }

    public FileEntry(Path file) {
        this(file.getParent().toAbsolutePath().toString(), file.getFileName().toString(), file.toFile().lastModified());
    }

    public String getParentPath() { return this.parentPath; }
    public String getFileName() { return this.fileName; }
    public long getLastModified() { return this.lastModified; }

    public String getParentPathHash() {
        if(this.parentPathHash == null)
            this.parentPathHash = DigestUtils.md5Hex(parentPath);
        return this.parentPathHash;
    }

    public String getFileNameHash() {
        if(this.fileNameHash == null)
            this.fileNameHash = DigestUtils.md5Hex(fileName);
        return this.fileNameHash;
    }

    @Override
    public String toString() {
        return String.format("FileEntry[ Parent: %s, Child %s, Last Modified %Tc ]", this.parentPath, this.fileName, new Date(this.lastModified));
    }
}
