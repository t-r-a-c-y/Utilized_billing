package com.utility.billing.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI configuration. Registers a global Bearer-JWT scheme so the
 * "Authorize" button in Swagger UI sends the token on every secured request.
 */
@Configuration
public class OpenApiConfig {

    private static final String SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI utilityBillingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Utility Billing System API")
                        .version("1.0.0")
                        .description("""
                                Backend for a national utility company managing water (postpaid) and
                                electricity (prepaid -> postpaid) services. Covers users, customers,
                                meters, readings, tariffs, billing, payments, taxes, penalties and
                                notifications.

                                **How to authenticate (two-step OTP login):**
                                1. `POST /api/v1/auth/login` with email + password — an OTP is emailed
                                   (and logged to the console when app.otp.log-to-console=true).
                                2. `POST /api/v1/auth/verify-otp` with the OTP to receive your JWT `token`.
                                3. Click **Authorize** and paste the token (no need to type "Bearer ").

                                New accounts must be verified at `/auth/verify-account`; passwords are
                                reset via `/auth/forgot-password` + `/auth/reset-password`.
                                """)
                        .contact(new Contact().name("Utility Co. Engineering").email("dev@utility.rw"))
                        .license(new License().name("Academic Use")))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME_NAME))
                .components(new Components().addSecuritySchemes(SCHEME_NAME,
                        new SecurityScheme()
                                .name(SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
