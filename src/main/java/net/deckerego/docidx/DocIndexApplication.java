package net.deckerego.docidx;

import net.deckerego.docidx.util.WorkBroker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class
DocIndexApplication {
	public static void main(String[] args) {
		SpringApplication.run(DocIndexApplication.class, args);
	}
}
