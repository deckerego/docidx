package net.deckerego.docidx.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Consumer;

public class BatchProcessingQueue<E> {
    private static final Logger LOG = LoggerFactory.getLogger(BatchProcessingQueue.class);

    private final ArrayBlockingQueue<E> batchQueue;
    private final int batchSize;
    private final long purgeWaitMillis;
    private final Consumer<List<E>> callback;
    //FIXME The purge timer is horribly broken still and totally not threadsafe
    private Timer purgeTimer;

    public BatchProcessingQueue(Consumer<List<E>> callback, int batchSize, int capacity, long purgeWaitMillis) {
        this.batchSize = batchSize;
        this.callback = callback;
        this.purgeWaitMillis = purgeWaitMillis;
        this.batchQueue = new ArrayBlockingQueue<>(capacity);
    }

    public boolean offer(E element) {
        if(this.purgeTimer == null) {
            LOG.debug(String.format("Scheduling purge timer for %d", this.purgeWaitMillis));
            this.purgeTimer = new Timer();
            this.purgeTimer.schedule(new PurgeTask(), this.purgeWaitMillis);
        }

        if(batchQueue.size() >= this.batchSize) {
            LOG.debug(String.format("Purging %d elements with buffer size %d", this.batchQueue.size(), this.batchSize));
            purge();
        }

        return this.batchQueue.offer(element);
    }

    public void purge() {
        LOG.debug("Purging current batch queue");
        this.purgeTimer.cancel();
        this.purgeTimer.purge();
        this.purgeTimer = null;

        List<E> batch = new ArrayList<>(this.batchSize);
        this.batchQueue.drainTo(batch, batchSize);
        this.callback.accept(batch);
    }

    private class PurgeTask extends TimerTask {
        @Override
        public void run() {
            LOG.debug(String.format("Purging %d elements after %d seconds", batchQueue.size(), purgeWaitMillis));
            purge();
        }
    }
}
