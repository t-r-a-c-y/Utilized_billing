package com.utility.billing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Entry point for the National Utility Company billing backend.
 */
@SpringBootApplication
@EnableJpaAuditing
public class UtilityBillingApplication {

    public static void main(String[] args) {
        SpringApplication.run(UtilityBillingApplication.class, args);
    }
}
