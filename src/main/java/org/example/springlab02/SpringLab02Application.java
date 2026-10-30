package org.example.springlab02;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"org.example.springlab02", "kz.iitu.springlab"})
@EnableJpaRepositories(basePackages = {"kz.iitu.springlab", "org.example.springlab02"})
@EntityScan(basePackages = {"kz.iitu.springlab", "org.example.springlab02"})
@ConfigurationPropertiesScan
public class SpringLab02Application {
    public static void main(String[] args) {
        SpringApplication.run(SpringLab02Application.class, args);
    }
}