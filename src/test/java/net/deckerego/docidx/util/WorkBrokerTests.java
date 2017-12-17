package net.deckerego.docidx.util;

import net.deckerego.docidx.configuration.BrokerConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WorkBroker.class)
public class WorkBrokerTests {
    @Autowired
    WorkBroker broker;

    @MockBean
    BrokerConfig brokerConfig;

    class TestMessage {
        public String id;
    }

    @Before
    public void clearBroker() {
        broker.shutdown();
    }

    @Test
    public void testTaskCount() {
        assertThat(broker.taskCount()).isEqualTo(0);
    }

    @Test
    public void testPublishConsume() throws InterruptedException {
        when(brokerConfig.getBatchSize()).thenReturn(1);
        when(brokerConfig.getPurgeWait()).thenReturn(10000L);
        when(brokerConfig.getPoolThreads()).thenReturn(1);
        when(brokerConfig.getCapacity()).thenReturn(1);
        when(brokerConfig.getTimeout()).thenReturn(10000L);

        final AtomicInteger callCount = new AtomicInteger(0);

        broker.handle(TestMessage.class, (m) -> {
            assertThat(m).isNotNull();
            callCount.incrementAndGet();
        });

        broker.publish(new TestMessage());
        broker.waitUntilComplete();

        assertThat(callCount.get()).isEqualTo(1);
    }

    @Test
    public void testConsume() throws InterruptedException {
        when(brokerConfig.getBatchSize()).thenReturn(1);
        when(brokerConfig.getPurgeWait()).thenReturn(10000L);
        when(brokerConfig.getPoolThreads()).thenReturn(1);
        when(brokerConfig.getCapacity()).thenReturn(1);
        when(brokerConfig.getTimeout()).thenReturn(10000L);

        final AtomicInteger callCount = new AtomicInteger(0);

        broker.handle(TestMessage.class, (m) -> {
            assertThat(m).isNotNull();
            assertThat(m.id).isEqualTo("TESTING");
            callCount.incrementAndGet();
        });

        TestMessage testMsg = new TestMessage();
        testMsg.id = "TESTING";

        broker.publish(testMsg);
        broker.waitUntilComplete();

        assertThat(callCount.get()).isEqualTo(1);
    }

    @Test
    public void testBatchSizeOne() throws InterruptedException {
        when(brokerConfig.getBatchSize()).thenReturn(1);
        when(brokerConfig.getPurgeWait()).thenReturn(10000L);
        when(brokerConfig.getPoolThreads()).thenReturn(1);
        when(brokerConfig.getCapacity()).thenReturn(1);
        when(brokerConfig.getTimeout()).thenReturn(10000L);

        final AtomicInteger callCount = new AtomicInteger(0);

        broker.handleBatch(TestMessage.class, (m) -> {
            assertThat(m.size()).isEqualTo(1);
            callCount.incrementAndGet();
        });

        broker.publish(new TestMessage());
        broker.waitUntilComplete();

        assertThat(callCount.get()).isEqualTo(1);
    }

    @Test(expected = IllegalStateException.class)
    public void testDuplicateHandler() {
        when(brokerConfig.getBatchSize()).thenReturn(1);
        when(brokerConfig.getPurgeWait()).thenReturn(10000L);
        when(brokerConfig.getPoolThreads()).thenReturn(1);
        when(brokerConfig.getCapacity()).thenReturn(1);
        when(brokerConfig.getTimeout()).thenReturn(10000L);

        broker.handle(TestMessage.class, (m) -> { });
        broker.handle(TestMessage.class, (m) -> { });
    }
}
