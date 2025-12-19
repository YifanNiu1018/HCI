package com.cooking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173", "http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 静态资源映射 - 系统内置图片
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
        
        // 用户上传图片映射
        String uploadDir = System.getProperty("user.dir") + File.separator + "uploads";
        File uploadFolder = new File(uploadDir);
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + File.separator);
    }
}

