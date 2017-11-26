package net.deckerego.docidx;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DocIndexApplicationTests {

    @Test
    public void filtering() {
        List<Integer> numbers = List.of(1, 2, 3, 3, 5);
        Map<Integer, Long> result = numbers.stream()
                .collect(Collectors.groupingBy(i -> i,
                        Collectors.filtering(val -> val > 3, Collectors.counting())));

        assertThat(result.size()).isEqualTo(4);
    }


    @Test
    public void flatmap() {
        class FileNode {
            public List<String> childHashes;

            public FileNode(String... childHashes) {
                this.childHashes = Arrays.asList(childHashes);
            }

            public List<String> getChildHashes() { return childHashes; }
        }

        List<FileNode> files = new ArrayList<>();
        files.add(new FileNode("1234AE", "564FE1"));
        files.add(new FileNode("AB9873", "CEFBAC", "869743"));

        List<String> results = files.stream()
                .map(a -> a.getChildHashes())
                .flatMap(c -> c.stream())
                .collect(Collectors.toList());

        assertThat(results.size()).isEqualTo(5);
    }

    @Test
    public void flatmapCollector() {
        class FileNode {
            public String parentHash;
            public List<String> childHashes;

            public FileNode(String parentHash, String... childHashes) {
                this.parentHash = parentHash;
                this.childHashes = Arrays.asList(childHashes);
            }

            public String getParentHash() { return parentHash; }
            public List<String> getChildHashes() { return childHashes; }
        }

        List<FileNode> files = new ArrayList<>();
        files.add(new FileNode("1234AE", "564FE1"));
        files.add(new FileNode("AB9873", "CEFBAC", "869743"));

        Map<String, List<String>> results = files.stream()
                .collect(Collectors.groupingBy(FileNode::getParentHash,
                        Collectors.flatMapping(blog -> blog.getChildHashes().stream(),
                                Collectors.toList())));

        assertThat(results.size()).isEqualTo(2);
    }
}
