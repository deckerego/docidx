package net.deckerego.docidx.util;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class WorkBrokerTests {
    @Test
    public void testTaskCount() {
        WorkBroker broker = new WorkBroker(1000, 10, 10);
        assertThat(broker.taskCount()).isEqualTo(0);
    }

    @Test
    public void testPublishConsume() throws InterruptedException {
        final AtomicInteger callCount = new AtomicInteger(0);

        WorkBroker broker = new WorkBroker(1000, 10, 10);
        broker.handle(TestMessage.class, (m) -> {
            assertThat(m).isNotNull();
            callCount.incrementAndGet();
        });

        broker.publish(new TestMessage());
        broker.waitUntilEmpty();

        assertThat(callCount.get()).isEqualTo(1);
    }

    @Test
    public void testConsume() throws InterruptedException {
        final AtomicInteger callCount = new AtomicInteger(0);

        WorkBroker broker = new WorkBroker(1000, 10, 10);
        broker.handle(TestMessage.class, (m) -> {
            assertThat(m).isNotNull();
            assertThat(m.id).isEqualTo("TESTING");
            callCount.incrementAndGet();
        });

        TestMessage testMsg = new TestMessage();
        testMsg.id = "TESTING";

        broker.publish(testMsg);
        broker.waitUntilEmpty();

        assertThat(callCount.get()).isEqualTo(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoHandler() {
        WorkBroker broker = new WorkBroker(1000, 1000, 10);
        broker.publish(new TestMessage());
    }

    class TestMessage {
        public String id;
    }

    @Test
    public void testBatchSizeOne() throws InterruptedException {
        final AtomicInteger callCount = new AtomicInteger(0);

        WorkBroker broker = new WorkBroker(1000, 10, 1);
        broker.handleBatch(TestMessage.class, (m) -> {
            assertThat(m.size()).isEqualTo(1);
            callCount.incrementAndGet();
        });

        broker.publish(new TestMessage());
        broker.waitUntilEmpty();

        assertThat(callCount.get()).isEqualTo(1);
    }

    @Test(expected = IllegalStateException.class)
    public void testDuplicateHandler() {
        WorkBroker broker = new WorkBroker(1000, 10, 10);
        broker.handle(TestMessage.class, (m) -> { });
        broker.handle(TestMessage.class, (m) -> { });
    }
}
