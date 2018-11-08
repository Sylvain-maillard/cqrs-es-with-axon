package com.prez.axon;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class Bank implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(Bank.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.debug("Hello, Axon world");
    }
}
