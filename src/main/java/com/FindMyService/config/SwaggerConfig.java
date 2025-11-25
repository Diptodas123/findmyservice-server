package com.FindMyService.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private final String email;

    public SwaggerConfig(@Value("${support.email}")
                         String email) {
        this.email = email;
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FindMyService API")
                        .version("1.0")
                        .description("API documentation for FindMyService application")
                        .contact(new Contact()
                                .name("FindMyService Support")
                                .email(email)));
    }
}
