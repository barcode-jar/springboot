package com.example.barcodesample.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class AdditionalResourceWebConfiguration implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        String IMG_PATH = "src/main/resources/images/";
        //String PDF_PATH = "src/main/resources/pdf/";

        Path app_folder = Paths.get("/app/src/main/resources");
        
        if (Files.exists(app_folder) && Files.isDirectory(app_folder)) {
            // Docker コンテナ上 (とりあえず)
            IMG_PATH = "/app/" + IMG_PATH;
            //PDF_PATH = "/app/" + PDF_PATH;
        }

        registry.addResourceHandler("/images/**").addResourceLocations("file:" + IMG_PATH);
        //registry.addResourceHandler("/pdf/**").addResourceLocations("file:" + PDF_PATH);
    }

}