package kz.iitu.springlab.web;

import kz.iitu.springlab.security.AppUserRepository;
import kz.iitu.springlab.security.AuthAuditLogRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AppUserRepository userRepository;
    private final AuthAuditLogRepository authAuditLogRepository;

    public AdminController(AppUserRepository userRepository, AuthAuditLogRepository authAuditLogRepository) {
        this.userRepository = userRepository;
        this.authAuditLogRepository = authAuditLogRepository;
    }

    @GetMapping
    public String adminPage(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("authLogs", authAuditLogRepository.findAllByOrderByCreatedAtDesc());
        return "admin/users";
    }
}
