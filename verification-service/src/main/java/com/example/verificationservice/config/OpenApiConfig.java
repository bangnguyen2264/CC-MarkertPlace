package com.example.verificationservice.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${springdoc.server.url:http://localhost:8222}")
    private String serverUrl;

    @Value("${springdoc.server.description:API Gateway}")
    private String serverDescription;

    @Bean
    public OpenAPI customOpenAPI() {
        // Cấu hình server - API Gateway
        Server gatewayServer = new Server();
        gatewayServer.setUrl(serverUrl);
        gatewayServer.setDescription(serverDescription);

        // Cấu hình Security Scheme cho JWT
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("Bearer Authentication");

        return new OpenAPI()
                .servers(List.of(gatewayServer))
                .info(new Info()
                        .title("Verification Service API")
                        .version("1.0.0")
                        .description("API documentation for Verification Service - Verify Request Management")
                        .contact(new Contact()
                                .name("Your Team Name")
                                .email("team@example.com")
                                .url("https://yourcompany.com")
                        )
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")
                        )
                )
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", securityScheme)
                )
                .addSecurityItem(securityRequirement);
    }
}