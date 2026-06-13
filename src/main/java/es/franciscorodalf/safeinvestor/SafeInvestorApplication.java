package es.franciscorodalf.safeinvestor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SafeInvestorApplication {

    public static void main(String[] args) {
        SpringApplication.run(SafeInvestorApplication.class, args);
    }
}
