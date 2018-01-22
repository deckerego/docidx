package net.deckerego.docidx.util;

import net.deckerego.docidx.configuration.BrokerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

@Component
public class WorkBroker {
    private static final Logger LOG = LoggerFactory.getLogger(WorkBroker.class);

    @Autowired
    public BrokerConfig brokerConfig;

    private Map<Class, ConsumptionStrategy> consumerMap;

    private AtomicLong publishCount = new AtomicLong(0);
    private AtomicLong consumedCount = new AtomicLong(0);

    public WorkBroker() {
        this.consumerMap = new HashMap<>();
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
        this.publishCount.incrementAndGet();
        LOG.trace(String.format("Consuming message %s", message.toString()));

        ConsumptionStrategy consumer = consumerMap.get(message.getClass());
        if(consumer == null) {
            LOG.error(String.format("No consumer for class %s", message.getClass().getCanonicalName()));
        } else {
            consumer.consume(message);
        }
    }

    public long taskCount() {
        int totalTaskCount = 0;
        for(ConsumptionStrategy c : consumerMap.values())
            totalTaskCount += c.taskCount();
        return totalTaskCount;
    }

    public void shutdown() {
        for(ConsumptionStrategy c : consumerMap.values())
            c.shutdown();
        this.consumerMap.clear();
    }

    public void waitUntilComplete() throws InterruptedException {
        while(this.taskCount() > 0) {
            Thread.sleep(1000);
        }
    }

    public long getPublishCount() { return this.publishCount.get(); }
    public long getConsumedCount() { return this.consumedCount.get(); }

    private interface ConsumptionStrategy<T> {
        void consume(T message);
        long taskCount();
        void shutdown();
    }

    private class ThreadPoolStrategy<T> implements ConsumptionStrategy<T> {
        private ThreadPoolExecutor threadPool;
        private Consumer<T> handler;

        public ThreadPoolStrategy(Consumer<T> handler) {
            this.threadPool =
                    new ThreadPoolExecutor(brokerConfig.getPoolThreads(), brokerConfig.getPoolThreads(),
                            brokerConfig.getTimeout(), TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(brokerConfig.getCapacity()));
            this.handler = handler;
        }

        @Override
        public void consume(T message) {
            this.threadPool.execute(() -> {
                this.handler.accept(message);
                consumedCount.incrementAndGet();
            });
        }

        @Override
        public long taskCount() {
            return this.threadPool.getActiveCount() + this.threadPool.getQueue().size();
        }

        @Override
        public void shutdown() {
            try {
                this.threadPool.shutdown();
                this.threadPool.awaitTermination(brokerConfig.getTimeout(), TimeUnit.MILLISECONDS);
            } catch(InterruptedException e) {
                LOG.error("Error while shutting down ThreadPoolStrategy", e);
            }
        }
    }

    private class BatchStrategy<T> implements ConsumptionStrategy<T> {
        private ArrayBlockingQueue<T> batchQueue;
        private Consumer<List<T>> handler;
        private final ReadWriteLock updateLock = new ReentrantReadWriteLock();
        private Timer purgeTimer;


        public BatchStrategy(Consumer<List<T>> handler) {
            this.purgeTimer = null;
            this.batchQueue = new ArrayBlockingQueue<>(brokerConfig.getCapacity());
            this.handler = handler;
        }

        @Override
        public void consume(T message) {
            this.updateLock.writeLock().lock();
            if(this.purgeTimer == null) {
                LOG.debug(String.format("Scheduling purge timer for %d", brokerConfig.getPurgeWait()));
                this.purgeTimer = new Timer();
                this.purgeTimer.schedule(new PurgeTask(), brokerConfig.getPurgeWait());
            }
            this.updateLock.writeLock().unlock();

            if(! this.batchQueue.offer(message)) {
                LOG.error(String.format("Failure in trying to save message to batch queue, dropping: %s", message.toString()));
            }

            if(this.batchQueue.size() >= brokerConfig.getBatchSize()) {
                LOG.debug(String.format("Purging %d elements with buffer size %d", this.batchQueue.size(), brokerConfig.getBatchSize()));
                purge();
            }

            consumedCount.incrementAndGet();
        }

        private void purge() {
            this.updateLock.writeLock().lock();
            LOG.debug("Purging current batch queue");
            if(this.purgeTimer == null) {
                LOG.warn("Purge timer for thread pool was null - this shouldn't be the case...");
            } else {
                this.purgeTimer.cancel();
                this.purgeTimer = null;
            }
            this.updateLock.writeLock().unlock();

            if(this.batchQueue.size() <= 0) {
                LOG.warn("Purge was requested but batch size is 0");
            } else {
                List<T> batch = new ArrayList<>(brokerConfig.getBatchSize());
                this.batchQueue.drainTo(batch);
                LOG.trace(String.format("Purging %s", batch.toString()));
                this.handler.accept(batch);
            }
        }

        @Override
        public long taskCount() {
            return this.batchQueue.size();
        }

        @Override
        public void shutdown() {
            if(this.taskCount() > 0)
                purge();
            this.batchQueue = null;
        }

        private class PurgeTask extends TimerTask {
            @Override
            public void run() {
                LOG.debug(String.format("Purging %d elements after %d seconds", batchQueue.size(), brokerConfig.getPurgeWait()));
                purge();
            }
        }
    }
}
