package com.buddharoad.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths; // 🚨🚨🚨 Paths 임포트 추가 🚨🚨🚨

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebMvcConfig.class); // 로거 인스턴스 생성

    @Value("${com.buddharoad.upload.path}")
    private String uploadPhysicalPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Windows 경로 구분자 '\'를 '/'로 통일 (URI 형식에 맞춤)
        String normalizedPath = uploadPhysicalPath.replace("\\", "/");

        // 경로 끝에 '/'가 없으면 추가 (ResourceHandlerRegistry가 폴더로 인식하도록)
        if (!normalizedPath.endsWith("/")) {
            normalizedPath += "/";
        }

        // 🚨🚨🚨 Paths.get().toUri().toString()을 사용하여 URI를 생성 🚨🚨🚨
        // 이 방법이 가장 강력하고 OS 독립적인 file URI 생성 방법임
        String resourceLocation = Paths.get(normalizedPath).toUri().toString();
        log.info("🚀 Configured static resource handler for /uploaded/** to serve from: {}", resourceLocation);

        registry.addResourceHandler("/uploaded/**")
                .addResourceLocations(resourceLocation);
    }
}
