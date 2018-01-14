package net.deckerego.docidx.model;

import java.nio.file.Path;

public class ParentEntry {
    public Path directory;
    public boolean updateTags = false;

    public ParentEntry(Path directory, boolean updateTags) {
        this.updateTags = updateTags;
        this.directory = directory;
    }

    @Override
    public String toString() {
        return directory.toAbsolutePath().toString();
    }
}
