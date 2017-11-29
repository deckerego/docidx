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
        SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>();
        publisher.subscribe(new PrintSubscriber());

        new Random().ints().forEach(i -> publisher.offer(i, (sub, msg) -> false));

        publisher.close();
    }

    public int action(int i) {
        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            System.err.println(e.getMessage());
        }

        return i % 2;
    }

    public class PrintSubscriber implements Flow.Subscriber<Integer> {
        private Flow.Subscription subscription;

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription= subscription;
            this.subscription.request(1);
        }

        @Override
        public void onNext(Integer message) {
            subscription.request(1);
            CompletableFuture.supplyAsync(() -> action(message)).whenComplete((i, ex) -> System.err.println("Number: " + i));
        }

        @Override
        public void onComplete() {}

        @Override
        public void onError(Throwable t) {
            System.err.println(t.getMessage());
        }
    }
}
