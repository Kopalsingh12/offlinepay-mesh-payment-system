package OfflinePay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "OfflinePay")
public class OfflinepayApplication {

    public static void main(String[] args) {
        SpringApplication.run(OfflinepayApplication.class, args);
    }
}