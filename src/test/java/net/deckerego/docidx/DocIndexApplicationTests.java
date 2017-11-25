package net.deckerego.docidx;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DocIndexApplicationTests {

	@Test
	public void contextLoads() {
	}

	@Test
	public void flatmap() {
        List<Integer> numbers = List.of(1, 2, 3, 3, 5);
        Map<Integer, Long> result = numbers.stream()
                .collect(Collectors.groupingBy(i -> i,
                        Collectors.filtering(val -> val > 3, Collectors.counting())));
	}
}
