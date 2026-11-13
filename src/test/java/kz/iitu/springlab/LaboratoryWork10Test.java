package kz.iitu.springlab;

import kz.iitu.springlab.catalog.BookService;
import kz.iitu.springlab.security.AppUser;
import kz.iitu.springlab.security.AppUserRepository;
import kz.iitu.springlab.security.AuthAuditLog;
import kz.iitu.springlab.security.AuthAuditLogRepository;
import org.example.springlab02.SpringLab02Application;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = SpringLab02Application.class)
class LaboratoryWork10Test {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private AuthAuditLogRepository authAuditLogRepository;

    @Autowired
    private BookService bookService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("Task 2: BCrypt produces unique salted hashes of length 60 and verifies against stored DB users")
    void testBcryptHashingAndDatabaseUsers() {
        String h1 = passwordEncoder.encode("user123");
        String h2 = passwordEncoder.encode("user123");
        assertThat(h1).isNotEqualTo(h2);
        assertThat(h1).hasSize(60).startsWith("$2a$10$");
        assertThat(passwordEncoder.matches("user123", h1)).isTrue();
        assertThat(passwordEncoder.matches("user123", h2)).isTrue();

        AppUser dbUser = appUserRepository.findByUsername("user").orElseThrow();
        AppUser dbAdmin = appUserRepository.findByUsername("admin").orElseThrow();
        assertThat(passwordEncoder.matches("user123", dbUser.getPasswordHash())).isTrue();
        assertThat(passwordEncoder.matches("admin123", dbAdmin.getPasswordHash())).isTrue();
        assertThat(dbAdmin.getRoles()).containsExactlyInAnyOrder("USER", "ADMIN");
    }

    @Test
    @DisplayName("Task 3.4: Access matrix for Anonymous, USER, and ADMIN")
    void testAccessMatrix() throws Exception {
        // Anonymous
        mockMvc.perform(get("/")).andExpect(status().isOk());
        mockMvc.perform(get("/books")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        mockMvc.perform(get("/admin")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        mockMvc.perform(get("/nowhere")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        // Logged-in USER
        mockMvc.perform(get("/books").with(user("user").roles("USER")))
                .andExpect(status().isOk());
        mockMvc.perform(post("/books").with(user("user").roles("USER")).with(csrf())
                        .param("title", "Test Book").param("author", "Author").param("year", "2020"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/admin").with(user("user").roles("USER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/nowhere").with(user("user").roles("USER")))
                .andExpect(status().isForbidden());

        // Logged-in ADMIN
        mockMvc.perform(get("/books").with(user("admin").roles("USER", "ADMIN")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin").with(user("admin").roles("USER", "ADMIN")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/nowhere").with(user("admin").roles("USER", "ADMIN")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Task 4: CSRF protection rejects POST without token (403) and accepts POST with token")
    void testCsrfProtection() throws Exception {
        // Without CSRF token -> 403 Forbidden
        mockMvc.perform(post("/books")
                        .with(user("admin").roles("USER", "ADMIN"))
                        .param("title", "Forged Book")
                        .param("author", "Attacker")
                        .param("year", "2020"))
                .andExpect(status().isForbidden());

        // With valid CSRF token -> 302 Redirect after PRG
        mockMvc.perform(post("/books")
                        .with(user("admin").roles("USER", "ADMIN"))
                        .with(csrf())
                        .param("title", "Genuine Secured Book")
                        .param("author", "Jane Doe")
                        .param("year", "2022")
                        .param("genre", "FICTION"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));
    }

    @Test
    @DisplayName("Task 3.2: @PreAuthorize denies delete() for ROLE_USER")
    @WithMockUser(username = "user", roles = "USER")
    void testMethodSecurityDeniesUser() {
        assertThatThrownBy(() -> bookService.delete(1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Task 3.2: @PreAuthorize allows delete() for ROLE_ADMIN")
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    void testMethodSecurityAllowsAdmin() {
        // Should not throw AccessDeniedException
        bookService.delete(999L);
    }

    @Test
    @DisplayName("Variant 10: AuthenticationSuccessEvent and FailureEvent write audit rows")
    void testAuthenticationAuditEvents() throws Exception {
        authAuditLogRepository.deleteAll();

        // 1. Failure login
        mockMvc.perform(formLogin("/login").user("user").password("wrongpass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));

        // 2. Success login
        mockMvc.perform(formLogin("/login").user("admin").password("admin123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        List<AuthAuditLog> logs = authAuditLogRepository.findAll();
        assertThat(logs).hasSize(2);
        assertThat(logs.get(0).getEventType()).isEqualTo("LOGIN_FAILURE");
        assertThat(logs.get(0).getUsername()).isEqualTo("user");
        assertThat(logs.get(1).getEventType()).isEqualTo("LOGIN_SUCCESS");
        assertThat(logs.get(1).getUsername()).isEqualTo("admin");
    }
}
