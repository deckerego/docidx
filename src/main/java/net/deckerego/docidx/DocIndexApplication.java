package net.deckerego.docidx;

import net.deckerego.docidx.service.CrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DocIndexApplication implements ApplicationRunner {

    @Autowired
    CrawlerService cralwerService;

    @Override
    public void run(ApplicationArguments args) {
        cralwerService.crawl("tests");
    }

	public static void main(String[] args) {
		SpringApplication.run(DocIndexApplication.class, args);
	}
}
