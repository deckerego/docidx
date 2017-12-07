package net.deckerego.docidx.util;

import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;

public class BatchProcessingQueueTests {

    @MockBean
    TestDoc testDoc;

    @Test
    public void testBatchSize() {
        BatchProcessingQueue<TestDoc> queue = new BatchProcessingQueue<>(e -> assertThat(e).isNotNull(), 2, 10, Long.MAX_VALUE);

    }

    @Test
    public void testBatchWait() {
        BatchProcessingQueue<TestDoc> queue = new BatchProcessingQueue<>(e -> assertThat(e).isNotNull(), 2, 10, Long.MAX_VALUE);

    }

    @Test
    public void testEmptyPurge() {
        BatchProcessingQueue<TestDoc> queue = new BatchProcessingQueue<>(e -> assertThat(e).isNotNull(), 2, 10, Long.MAX_VALUE);

    }

    class TestDoc {

    }
}
