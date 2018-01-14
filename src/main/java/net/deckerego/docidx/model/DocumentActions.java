package net.deckerego.docidx.model;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class DocumentActions {
    public Path directory;

    public Set<FileEntry> deletions;
    public Set<Path> additions;
    public Set<Path> updates;
    public Set<Path> unmodified;

    public Boolean matchTagging;

    public DocumentActions(Path directory, boolean matchTagging) {
        this.directory = directory;

        this.deletions = new HashSet<>();
        this.additions = new HashSet<>();
        this.updates = new HashSet<>();
        this.unmodified = new HashSet<>();

        this.matchTagging = matchTagging;
    }

    @Override
    public String toString() {
        if(this.matchTagging)
            return String.format("For %s\nAdditions: %s\nUpdates: %s\nDeletions: %s\nUnmodified: %s",
                    directory.getFileName().toString(), additions, updates, deletions, unmodified);
        else
            return String.format("For %s\nAdditions: %s\nUpdates: %s\nDeletions: %s",
                    directory.getFileName().toString(), additions, updates, deletions);
    }
}