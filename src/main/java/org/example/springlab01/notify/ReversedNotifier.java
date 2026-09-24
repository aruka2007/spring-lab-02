package org.example.springlab01.notify;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component("reversed")
@Order(3)
public class ReversedNotifier implements Notifier {

    private static final Logger log = LoggerFactory.getLogger(ReversedNotifier.class);

    @PostConstruct
    void init() {
        log.info("REVERSED >> initialized");
    }

    @Override
    public String send(String message) {
        String result = new StringBuilder(message).reverse().toString();
        log.info("REVERSED >> {} -> {}", message, result);
        return result;
    }

    @Override
    public String channel() {
        return "reversed";
    }
}