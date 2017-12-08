package net.deckerego.docidx.model;

import java.nio.file.Path;
import java.util.function.Consumer;

public class TikaTask {
    public Path file;
    public Consumer<FileEntry> callback;

    public TikaTask(Path file, Consumer<FileEntry> callback) {
        this.callback = callback;
        this.file = file;
    }
}
