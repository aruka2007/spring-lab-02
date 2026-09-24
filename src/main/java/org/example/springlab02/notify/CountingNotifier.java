package org.example.springlab02.notify;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component("counting")
@Order(3)
public class CountingNotifier implements Notifier {

    private static final Logger log = LoggerFactory.getLogger(CountingNotifier.class);

    @PostConstruct
    void init() {
        log.info("COUNTING >> initialized");
    }

    @Override
    public String send(String message) {
        int words = message.isBlank() ? 0 : message.trim().split("\\s+").length;
        int chars = message.length();
        String result = message + " [words=" + words + ", chars=" + chars + "]";
        log.info("COUNTING >> {} -> {}", message, result);
        return result;
    }

    @Override
    public String channel() {
        return "counting";
    }
}