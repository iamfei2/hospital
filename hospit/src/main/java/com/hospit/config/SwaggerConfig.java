package com.hospit.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    // 配置Swagger OpenAPI信息
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("医院检查数据管理平台 API")
                        .description("基于Spring Boot的医院检查数据管理平台接口文档")
                        .version("1.0")
                        .contact(new Contact()
                                .name("龙涛")
                                .email("")
                                .url("")));
    }
}
