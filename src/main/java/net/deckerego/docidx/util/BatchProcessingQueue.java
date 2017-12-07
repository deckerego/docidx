package net.deckerego.docidx.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

public class BatchProcessingQueue<E> {
    private static final Logger LOG = LoggerFactory.getLogger(BatchProcessingQueue.class);

    private final ArrayBlockingQueue<E> batchQueue;
    private final int batchSize;
    private final long purgeWaitMillis;
    private final Consumer<List<E>> callback;
    private final ReentrantReadWriteLock updateLock = new ReentrantReadWriteLock();
    private Timer purgeTimer;

    public BatchProcessingQueue(Consumer<List<E>> callback, int batchSize, int capacity, long purgeWaitMillis) {
        this.batchSize = batchSize;
        this.callback = callback;
        this.purgeWaitMillis = purgeWaitMillis;
        this.batchQueue = new ArrayBlockingQueue<>(capacity);
    }

    public boolean offer(E element) {
        this.updateLock.readLock().lock();
        if(this.purgeTimer == null) {
            this.updateLock.readLock().unlock();
            this.updateLock.writeLock().lock();

            LOG.debug(String.format("Scheduling purge timer for %d", this.purgeWaitMillis));
            this.purgeTimer = new Timer();
            this.purgeTimer.schedule(new PurgeTask(), this.purgeWaitMillis);

            this.updateLock.writeLock().unlock();
        } else {
            this.updateLock.readLock().unlock();
        }

        if(this.batchQueue.size() >= this.batchSize) {
            LOG.debug(String.format("Purging %d elements with buffer size %d", this.batchQueue.size(), this.batchSize));
            purge();
        }

        return this.batchQueue.offer(element);
    }

    public void purge() {
        this.updateLock.writeLock().lock();
        LOG.debug("Purging current batch queue");
        this.purgeTimer.cancel();
        this.purgeTimer.purge();
        this.purgeTimer = null;
        this.updateLock.writeLock().unlock();

        List<E> batch = new ArrayList<>(this.batchSize);
        this.batchQueue.drainTo(batch);
        LOG.trace(String.format("Purging %s", batch.toString()));
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
