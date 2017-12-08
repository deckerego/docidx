package net.deckerego.docidx.model;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class DocumentActions {
    public Path directory;
    public Set<FileEntry> deletions;
    public Set<Path> additions;
    public Set<Path> updates;

    public DocumentActions(Path directory) {
        this.directory = directory;
        this.deletions = new HashSet<>();
        this.additions = new HashSet<>();
        this.updates = new HashSet<>();
    }

    @Override
    public String toString() {
        return String.format("For %s\nAdditions: %s\nUpdates: %s\nDeletions: %s",
                directory.getFileName().toString(), additions, updates, deletions);
    }
}