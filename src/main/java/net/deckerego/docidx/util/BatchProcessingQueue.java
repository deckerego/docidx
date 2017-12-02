package net.deckerego.docidx.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;

public class BatchProcessingQueue<E> {
    private static final Logger LOG = LoggerFactory.getLogger(BatchProcessingQueue.class);

    private final ArrayBlockingQueue<E> batchQueue;
    private final int batchSize;
    private final Consumer<List<E>> callback;
    private final Timer purgeTimer;
    private boolean shuttingDown;

    public BatchProcessingQueue(Consumer<List<E>> callback, int batchSize, int capacity, long purgeWaitMillis) {
        this.batchSize = batchSize;
        this.callback = callback;
        this.shuttingDown = false;

        this.batchQueue = new ArrayBlockingQueue<>(capacity);

        this.purgeTimer = new Timer();
        this.purgeTimer.scheduleAtFixedRate(new PurgeTask(), purgeWaitMillis, purgeWaitMillis);
    }

    public boolean offer(E element) {
        if(this.shuttingDown)
            throw new RejectedExecutionException("Shutting down queue, no further requests");

        if(batchQueue.size() >= this.batchSize) {
            List<E> batch = new ArrayList<>(this.batchSize);
            this.batchQueue.drainTo(batch, batchSize);
            process(batch);
        }

        return this.batchQueue.offer(element);
    }

    public void shutdown() {
        LOG.debug("Shutting down BatchProcessingQueue");
        this.shuttingDown = true;
        this.purgeTimer.cancel();
        List<E> batch = new ArrayList<>();
        this.batchQueue.drainTo(batch);
        process(batch);
    }

    public void process(List<E> batch) {
        this.callback.accept(batch);
    }

    private class PurgeTask extends TimerTask {
        @Override
        public void run() {
            List<E> batch = new ArrayList<>();
            batchQueue.drainTo(batch, batchSize);
            process(batch);
        }
    }
}
