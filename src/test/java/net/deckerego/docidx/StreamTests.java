package net.deckerego.docidx;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Random;
import java.util.concurrent.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StreamTests {

    @Test
    public void directoryStreamCollector() {
        new Random().ints().forEach(this::enhance);
    }

    public CompletableFuture<Integer> enhance(int i) {
        return CompletableFuture.supplyAsync(() -> action(i))
                .whenComplete((msg, ex) -> System.out.println("Number: " + msg));
    }

    public int action(int i) {
        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            System.err.println(e.getMessage());
        }

        return i % 2;
    }
}
