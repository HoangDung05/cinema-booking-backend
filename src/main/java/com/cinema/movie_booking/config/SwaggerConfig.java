package com.cinema.movie_booking.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Tài liệu API Hệ thống Rạp Phim",
                version = "1.0",
                description = "Danh sách API dành cho nhóm Frontend thao tác và tích hợp."
        ),
        // Áp dụng ổ khóa bảo mật cho toàn bộ API
        security = @SecurityRequirement(name = "Bearer Authentication") 
)
@SecurityScheme(
        name = "Bearer Authentication", // Tên của ổ khóa
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class SwaggerConfig {
}