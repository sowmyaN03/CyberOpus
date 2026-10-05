package com.cyberopus.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cyberOpusOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CyberOpus API")
                        .description("IBM AI Specialist Cloud FullStack Capstone – CyberOpus E-Bookstore Backend")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("CyberOpus Capstone")
                                .email("admin@cyberopus.com")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token in the format: Bearer <token>")));
    }
}
