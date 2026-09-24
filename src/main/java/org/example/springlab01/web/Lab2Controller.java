package org.example.springlab01.web;

import org.example.springlab01.lifecycle.LifecycleDemo;
import org.example.springlab01.notify.NotificationService;
import org.example.springlab01.scope.TicketOffice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lab2")
public class Lab2Controller {

    private final NotificationService notifications;
    private final LifecycleDemo lifecycle;
    private final TicketOffice ticketOffice;

    public Lab2Controller(NotificationService notifications,
                          LifecycleDemo lifecycle,
                          TicketOffice ticketOffice) {
        this.notifications = notifications;
        this.lifecycle = lifecycle;
        this.ticketOffice = ticketOffice;
    }

    @GetMapping("/notify")
    public Map<String, Object> notify(@RequestParam(defaultValue = "Hello") String text) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("primary", notifications.viaPrimary(text));
        result.put("console", notifications.viaConsole(text));
        result.put("all", notifications.viaAll(text));
        result.put("beanNames", notifications.names());
        return result;
    }

    @GetMapping("/lifecycle")
    public List<String> lifecycle() {
        return lifecycle.events();
    }

    @GetMapping("/scopes")
    public Map<String, Object> scopes() {
        return ticketOffice.demo();
    }

    @GetMapping("/custom")
    public Map<String, Object> custom(@RequestParam(defaultValue = "Hello") String text) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("channel", "reversed");
        result.put("input", text);
        result.put("output", notifications.viaName("reversed", text));
        return result;
    }
}