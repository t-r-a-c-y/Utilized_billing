package com.utility.billing.config;

import com.utility.billing.entity.User;
import com.utility.billing.entity.enums.Role;
import com.utility.billing.entity.enums.UserStatus;
import com.utility.billing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a default ROLE_ADMIN account on first boot so the secured API can be
 * accessed immediately. Disable with app.seed.enabled=false.
 *
 * Default credentials:  admin@utility.rw / Admin123!
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdmin("admin@utility.rw", "Admin123!", "System Administrator", Role.ROLE_ADMIN, "+250788000001");
        seedAdmin("operator@utility.rw", "Operator123!", "Field Operator", Role.ROLE_OPERATOR, "+250788000002");
        seedAdmin("finance@utility.rw", "Finance123!", "Finance Officer", Role.ROLE_FINANCE, "+250788000003");
    }

    private void seedAdmin(String email, String rawPassword, String name, Role role, String phone) {
        if (userRepository.existsByEmail(email)) {
            return;
        }
        userRepository.save(User.builder()
                .fullNames(name)
                .email(email)
                .phoneNumber(phone)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .status(UserStatus.ACTIVE)
                .build());
        log.info("Seeded {} account: {}", role, email);
    }
}
