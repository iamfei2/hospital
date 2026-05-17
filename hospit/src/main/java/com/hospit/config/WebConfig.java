package com.hospit.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Autowired
    private ApiKeyInterceptor apiKeyInterceptor;
    
    // 注册拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/user/login",
                    "/user/register",
                    "/user/list",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/swagger-ui/index.html",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**",
                    "/error",
                    "/ctExamination/download/**",
                    "/mriExamination/download/**",
                    "/enteroscopyExamination/download/**",
                    "/pathologyExamination/download/**",
                    "/labResult/download/**",
                    "/import/**",
                    "/isolationForest/**",
                    "/open/api/**"
                );

        registry.addInterceptor(apiKeyInterceptor)
                .addPathPatterns("/open/api/**");
    }
}
