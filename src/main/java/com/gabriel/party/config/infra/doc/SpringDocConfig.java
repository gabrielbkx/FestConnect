package com.gabriel.party.config.infra.doc;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API para um marketplace de serviços de festas")
                        .version("v1")
                        .description("Documentação da API")
                        .contact(new Contact().name("Gabriel").email("22998715889")));
    }
}
