package net.deckerego.docidx.model;

import java.nio.file.Path;
import java.util.Date;

public class FileEntry {
    private String id;
    private String parentPath;
    private String fileName;
    private Long lastModified;
    private Status mergeStatus = Status.UNKNOWN;

    public FileEntry() {
    }

    public FileEntry(String id) {
        this.id = id;
    }

    //FIXME Hack Hack Hack Hack for cloning
    public FileEntry merge(Path file) {
        FileEntry entry = new FileEntry();
        entry.setParentPath(file.getParent().toAbsolutePath().toString());
        entry.setFileName(file.getFileName().toString());
        entry.setLastModified(file.toFile().lastModified());

        if (this.getId() != null && file != null && file.toFile().lastModified() != this.getLastModified()) {
            entry.setMergeStatus(Status.UPDATED);
        } else if (this.getId() == null && file != null) {
            entry.setMergeStatus(Status.ADDED);
        } else if (this.getId() != null && file == null) {
            entry.setMergeStatus(Status.DELETED);
        } else {
            entry.setMergeStatus(Status.SAME);
        }

        return entry;
    }

    public String getId() { return this.id; }
    public String getParentPath() { return this.parentPath; }
    public void setParentPath(String name) { this.parentPath = name; }
    public String getFileName() { return this.fileName; }
    public void setFileName(String name) { this.fileName = name; }
    public long getLastModified() { return this.lastModified; }
    public void setLastModified(long time) { this.lastModified = time; }
    public void setMergeStatus(Status status) { this.mergeStatus = status; }
    public Status getMergeStatus() { return this.mergeStatus; }

    @Override
    public String toString() {
        return String.format("FileEntry[ Parent: %s, Child %s, Last Modified %Tc ]", this.getParentPath(), this.getFileName(), new Date(this.getLastModified()));
    }

    public enum Status { ADDED, DELETED, UPDATED, SAME, UNKNOWN }
}
