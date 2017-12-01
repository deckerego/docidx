package net.deckerego.docidx.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Consumer;

public class BatchProcessingQueue<E> {
    private final ArrayBlockingQueue<E> batchQueue;
    private final int batchSize;
    private final Consumer<List<E>> callback;
    private final Timer purgeTimer;

    public BatchProcessingQueue(Consumer<List<E>> callback, int batchSize, int capacity, long purgeWaitMillis) {
        this.batchSize = batchSize;
        this.callback = callback;

        this.batchQueue = new ArrayBlockingQueue<>(capacity);

        this.purgeTimer = new Timer();
        this.purgeTimer.scheduleAtFixedRate(new PurgeTask(), purgeWaitMillis, purgeWaitMillis);
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

    private class PurgeTask extends TimerTask {
        @Override
        public void run() {
            List<E> batch = new ArrayList<>();
            batchQueue.drainTo(batch);
            process(batch);
        }
    }
}
