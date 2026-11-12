package com.maquigest.platform.shared.infrastructure.documentation.openapi.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/** API documentation and the JWT bearer authentication scheme. */
@Configuration
public class OpenApiConfiguration {
    @Value("${spring.application.name}")
    String applicationName;

    @Value("${documentation.application.description}")
    String applicationDescription;

    @Value("${documentation.application.version}")
    String applicationVersion;

    @Bean
    public OpenAPI maquigestPlatformOpenApi() {
        final String securitySchemeName = "bearerAuth";
        var openApi = new OpenAPI();
        openApi.info(new Info()
                .title(applicationName)
                .description(applicationDescription)
                .version(applicationVersion));
        openApi.servers(List.of(new Server()
                .url("http://localhost:8080")
                .description("Local Development Environment")));
        // Define the scheme globally without requiring bearer for public sign-up/sign-in.
        // Protected operations declare @SecurityRequirement(name = "bearerAuth").
        openApi.components(new Components().addSecuritySchemes(securitySchemeName,
                new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT bearer token for protected MaquiGest API endpoints")));
        return openApi;
    }
}
