package com.utility.billing.security;

import com.utility.billing.entity.Customer;
import com.utility.billing.entity.User;
import com.utility.billing.exception.BusinessRuleException;
import com.utility.billing.exception.ResourceNotFoundException;
import com.utility.billing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves the Customer profile linked to the currently authenticated user
 * (by their JWT email). Used by the customer self-service endpoints so a
 * customer can only ever see their own data.
 */
@Component
@RequiredArgsConstructor
public class CurrentCustomerResolver {

    private final UserRepository userRepository;

    public Customer resolve(String email) {
        String normalized = email == null ? null : email.trim().toLowerCase();
        User user = userRepository.findByEmail(normalized)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        Customer customer = user.getCustomer();
        if (customer == null) {
            throw new BusinessRuleException(
                    "This account is not linked to a customer profile.");
        }
        return customer;
    }
}
