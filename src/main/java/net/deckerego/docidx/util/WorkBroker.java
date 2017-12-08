package net.deckerego.docidx.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

public class WorkBroker {
    private static final Logger LOG = LoggerFactory.getLogger(WorkBroker.class);

    private int threadPoolSize;
    private long purgeWaitMillis;
    private long timeoutMillis;
    private int capacity;
    private int batchSize;
    private Map<Class, ConsumptionStrategy> consumerMap;

    public WorkBroker(long purgeWaitMillis, int capacity, int batchSize) {
        this(Runtime.getRuntime().availableProcessors(), purgeWaitMillis, 60 * 60 * 1000, capacity, batchSize);
    }

    public WorkBroker(int threadPoolSize, long purgeWaitMillis, long timeoutMillis, int capacity, int batchSize) {
        this.consumerMap = new HashMap<>();
        this.threadPoolSize = threadPoolSize;
        this.timeoutMillis = timeoutMillis;
        this.purgeWaitMillis = purgeWaitMillis;
        this.batchSize = batchSize;
        this.capacity = capacity;
    }

    public <T> void handle(Class<T> messageClass, Consumer<T> handler) {
        LOG.info(String.format("Adding threadpool handler for message type %s", messageClass.getCanonicalName()));

        if(this.consumerMap.containsKey(messageClass)) {
            String msg = String.format("Cannot add handler for %s, one already exists!", messageClass.getCanonicalName());
            LOG.error(msg);
            throw new IllegalStateException(msg);
        }

        this.consumerMap.put(messageClass, new ThreadPoolStrategy<>(handler));
    }

    public <T> void handleBatch(Class<T> messageClass, Consumer<List<T>> handler) {
        LOG.info(String.format("Adding batch handler for message type %s", messageClass.getCanonicalName()));

        if(this.consumerMap.containsKey(messageClass)) {
            String msg = String.format("Cannot add handler for %s, one already exists!", messageClass.getCanonicalName());
            LOG.error(msg);
            throw new IllegalStateException(msg);
        }

        this.consumerMap.put(messageClass, new BatchStrategy(handler));
    }

    public <T> void publish(T message) {
        LOG.trace(String.format("Consuming message %s", message.toString()));

        ConsumptionStrategy consumer = consumerMap.get(message.getClass());
        if(consumer == null)
            LOG.error(String.format("No consumer for class %s", message.getClass().getCanonicalName()));

        consumer.consume(message);
    }

    public long taskCount() {
        int totalTaskCount = 0;
        for(ConsumptionStrategy c : consumerMap.values())
            totalTaskCount += c.taskCount();
        return totalTaskCount;
    }

    public void waitUntilEmpty() throws InterruptedException {
        while(this.taskCount() > 0) {
            Thread.sleep(100);
        }
    }

    private interface ConsumptionStrategy<T> {
        void consume(T message);
        long taskCount();
    }

    private class ThreadPoolStrategy<T> implements ConsumptionStrategy<T> {
        private ThreadPoolExecutor threadPool;
        private Consumer<T> handler;

        public ThreadPoolStrategy(Consumer<T> handler) {
            this.threadPool =
                    new ThreadPoolExecutor(threadPoolSize, threadPoolSize, timeoutMillis, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(capacity));
            this.handler = handler;
        }

        @Override
        public void consume(T message) {
            this.threadPool.execute(() -> this.handler.accept(message));
        }

        @Override
        public long taskCount() {
            return this.threadPool.getActiveCount() + this.threadPool.getQueue().size();
        }
    }

    private class BatchStrategy<T> implements ConsumptionStrategy<T> {
        private ArrayBlockingQueue<T> batchQueue;
        private Consumer<List<T>> handler;
        private final ReentrantReadWriteLock updateLock = new ReentrantReadWriteLock();
        private AtomicBoolean timerActive;
        private Timer purgeTimer;


        public BatchStrategy(Consumer<List<T>> handler) {
            this.timerActive = new AtomicBoolean(false);
            this.purgeTimer = new Timer();
            this.batchQueue = new ArrayBlockingQueue<>(capacity);
            this.handler = handler;
        }

        @Override
        public void consume(T message) {
            this.updateLock.writeLock().lock(); //TODO This is kinda gross
            if(! this.timerActive.get()) {
                this.timerActive.set(true);
                LOG.debug(String.format("Scheduling purge timer for %d", purgeWaitMillis));
                this.purgeTimer.schedule(new PurgeTask(), purgeWaitMillis);
            }
            this.updateLock.writeLock().unlock();

            if(! this.batchQueue.offer(message)) {
                LOG.error(String.format("Failure in trying to save message to batch queue, dropping: %s", message.toString()));
            }

            if(this.batchQueue.size() >= batchSize) {
                LOG.debug(String.format("Purging %d elements with buffer size %d", this.batchQueue.size(), batchSize));
                purge();
            }
        }

        private void purge() {
            this.timerActive.set(false);
            this.updateLock.writeLock().lock();
            LOG.debug("Purging current batch queue");
            this.purgeTimer.cancel();
            this.purgeTimer.purge();
            this.updateLock.writeLock().unlock();

            List<T> batch = new ArrayList<>(batchSize);
            this.batchQueue.drainTo(batch);
            LOG.trace(String.format("Purging %s", batch.toString()));
            this.handler.accept(batch);
        }

        @Override
        public long taskCount() {
            return this.batchQueue.size();
        }

        private class PurgeTask extends TimerTask {
            @Override
            public void run() {
                LOG.debug(String.format("Purging %d elements after %d seconds", batchQueue.size(), purgeWaitMillis));
                purge();
            }
        }
    }
}
