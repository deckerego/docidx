package net.deckerego.docidx.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Consumer;

public class BatchProcessingQueue<E> {
    private final ArrayBlockingQueue<E> batchQueue;
    private final int batchSize;
    private final Consumer<List<E>> callback;

    public BatchProcessingQueue(Consumer<List<E>> callback, int batchSize, int capacity, long purgeWaitMillis) {
        this.batchSize = batchSize;
        this.callback = callback;

        this.batchQueue = new ArrayBlockingQueue<>(capacity);
    }

    public boolean offer(E element) {
        if(batchQueue.size() >= this.batchSize) {
            List<E> batch = new ArrayList<>(this.batchSize);
            this.batchQueue.drainTo(batch);
            process(batch);
        }

        return this.batchQueue.offer(element);
    }

    public int size() {
        return this.batchQueue.size();
    }

    public void process(List<E> batch) {
        this.callback.accept(batch);
    }
}
