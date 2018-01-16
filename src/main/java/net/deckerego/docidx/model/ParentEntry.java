package net.deckerego.docidx.model;

import java.nio.file.Path;

public class ParentEntry {
    public Path directory;

    public ParentEntry(Path directory) {
        this.directory = directory;
    }

    @Override
    public String toString() {
        return directory.toAbsolutePath().toString();
    }
}
