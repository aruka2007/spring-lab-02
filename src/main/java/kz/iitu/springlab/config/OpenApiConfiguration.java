package kz.iitu.springlab.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Library Catalogue REST API")
                        .version("1.0.0")
                        .description("Laboratory Work No. 7: REST API with DTOs, mappers, RFC 9457 ProblemDetail, and Swagger UI")
                        .contact(new Contact()
                                .name("Erkin Aruzhan (2405-IS, Variant 10)")
                                .email("aruka@iitu.edu.kz")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development Server")
                ));
    }
}
