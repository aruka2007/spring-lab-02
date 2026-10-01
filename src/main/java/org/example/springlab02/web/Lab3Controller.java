package org.example.springlab02.web;

import org.example.springlab02.config.AppProperties;
import org.example.springlab02.config.EnvironmentBanner;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/lab3")
public class Lab3Controller {

    private final AppProperties props;
    private final EnvironmentBanner banner;
    private final Environment environment;

    public Lab3Controller(AppProperties props, EnvironmentBanner banner,
                          Environment environment) {
        this.props = props;
        this.banner = banner;
        this.environment = environment;
    }

    @GetMapping("/config")
    public Map<String, Object> config() {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("owner", props.owner());
        res.put("group", props.group());
        res.put("mailFrom", props.mail().from());
        res.put("mailRetryCount", props.mail().retryCount());
        res.put("mailTimeout", props.mail().timeout().toString());
        res.put("mailEnabled", props.mail().enabled());
        res.put("notificationChannels", props.notification().channels());
        res.put("notificationQuietHours", props.notification().quietHours());
        String port = environment.getProperty("server.port");
        res.put("serverPort", port != null ? port : "8080");
        res.put("activeProfiles", Arrays.asList(environment.getActiveProfiles()));
        res.put("banner", banner.describe());
        return res;
    }
}
