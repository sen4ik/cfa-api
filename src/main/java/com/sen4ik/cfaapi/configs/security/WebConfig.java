package com.sen4ik.cfaapi.configs.security;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//@Configuration
//@EnableWebMvc
//@Slf4j
// I used this WebConfig to overcome following issue: "Cross-Origin Request Blocked: The Same Origin Policy disallows reading the
// remote resource at http://11.11.11.11:8084/api/v1/file/category/0. (Reason: CORS header
// ‘Access-Control-Allow-Origin’ missing)."
// The problem is that when I use below way of overcoming above issue, swagger ui is not working.
// I was able to overcome the cors issue by running nginx with reverse proxy. See cfa-api-nginx.conf.
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
                .addMapping("/**")
                .allowedOrigins("http://localhost:3000"); // This is the URL of react app we want to allow accessing the api
    }
}
