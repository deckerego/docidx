package net.deckerego.docidx.model;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FileEntry.class)
public class FileEntryTests {
    @Test
    public void testEquals() {
        Assertions.assertThat(new FileEntry.Tag("tester", 0.9)).isEqualTo(new FileEntry.Tag("tester", 0.0));
    }

    @Test
    public void testNotEquals() {
        Assertions.assertThat(new FileEntry.Tag("tester", 0.9)).isNotEqualTo(new FileEntry.Tag("testing", 0.9));
    }
}
