package org.example.springlab02;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = {"org.example.springlab02", "kz.iitu.springlab"})
@ConfigurationPropertiesScan
public class SpringLab02Application {
    public static void main(String[] args) {
        SpringApplication.run(SpringLab02Application.class, args);
    }
}