package org.example.springlab02.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Configuration
public class AppConfig {

    @Bean
    public DateTimeFormatter dateTimeFormatter(@Value("${app.date-pattern}") String pattern) {
        return DateTimeFormatter.ofPattern(pattern, Locale.ROOT);
    }
}