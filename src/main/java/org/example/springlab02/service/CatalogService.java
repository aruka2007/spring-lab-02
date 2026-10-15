package org.example.springlab02.service;

import org.example.springlab02.audit.Audited;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;

@Service
public class CatalogService {

    @Autowired
    @Lazy
    private CatalogService self;

    public String findById(long id) {
        sleep(50); // simulating database call
        return "Item no. " + id;
    }

    @Audited(action = "CATALOG_LIST", logArguments = true)
    public List<String> findAll(int limit) {
        sleep(300); // deliberately slow method (> 200 ms to trigger TimingAspect warning)
        return IntStream.rangeClosed(1, limit)
                .mapToObj(i -> "Item no. " + i)
                .toList();
    }

    @Audited(action = "CATALOG_REMOVE")
    public String remove(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid identifier: " + id);
        }
        return "Removed item no. " + id;
    }

    public String removeTwice(long id) {
        // Self-invocation via this: bypasses Spring AOP proxy wrapper
        String first = remove(id);
        String second = remove(id + 1);
        return first + "; " + second;
    }

    public String removeTwiceFixed(long id) {
        // Fixed invocation via self-injected proxy bean
        String first = self.remove(id);
        String second = self.remove(id + 1);
        return first + "; " + second;
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
