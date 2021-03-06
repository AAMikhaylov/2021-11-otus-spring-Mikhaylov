package ru.otus.hw11;

import io.mongock.runner.springboot.EnableMongock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableMongock
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
