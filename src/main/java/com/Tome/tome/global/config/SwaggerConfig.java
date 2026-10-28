package com.Tome.tome.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    private static final String SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tome API")
                        .description("Tome 서버 API 명세서")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SCHEME_NAME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    // /login, /logout은 Spring Security 필터가 처리해서 springdoc이 자동으로 찾지 못하므로 직접 문서에 추가
    @Bean
    public OpenApiCustomizer securityEndpointCustomizer() {
        return openApi -> {
            openApi.path("/login", new PathItem().post(new Operation()
                    .tags(List.of("Auth"))
                    .summary("이메일 로그인")
                    .description("Spring Security 폼 로그인입니다. 성공하면 accessToken을 JSON으로, refresh_token을 쿠키(14일, HttpOnly)로 내려줍니다. "
                            + "Authorization 헤더 없이 호출하므로 /api/csrf로 받은 토큰을 X-XSRF-TOKEN 헤더에 넣어야 합니다.")
                    .security(List.of())
                    .requestBody(new RequestBody().required(true).content(new Content()
                            .addMediaType("application/x-www-form-urlencoded", new MediaType()
                                    .schema(new ObjectSchema()
                                            .addProperty("email", new StringSchema().example("test@test.com"))
                                            .addProperty("password", new StringSchema().example("12345678"))
                                            .required(List.of("email", "password"))))))
                    .responses(new ApiResponses()
                            .addApiResponse("200", new ApiResponse()
                                    .description("로그인 성공: {\"accessToken\": \"...\", \"firstLogin\": true}"))
                            .addApiResponse("401", new ApiResponse()
                                    .description("이메일 또는 비밀번호 오류: {\"message\": \"이메일 또는 비밀번호가 올바르지 않습니다\"}"))
                            .addApiResponse("403", new ApiResponse().description("CSRF 토큰 누락")))));

            openApi.path("/logout", new PathItem().post(new Operation()
                    .tags(List.of("Auth"))
                    .summary("로그아웃")
                    .description("DB의 리프레시 토큰과 refresh_token 쿠키를 삭제합니다. Bearer 토큰 없이 호출하면 X-XSRF-TOKEN 헤더가 필요합니다.")
                    .responses(new ApiResponses()
                            .addApiResponse("200", new ApiResponse().description("{\"message\": \"로그아웃되었습니다\"}")))));
        };
    }
}
