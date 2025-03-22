package com.example.user_product_api.config;

import com.example.user_product_api.entity.Role;
import com.example.user_product_api.entity.User;
import com.example.user_product_api.repository.RoleRepository;
import com.example.user_product_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Only run if the data doesn't exist
        if (roleRepository.count() == 0) {
            seedRoles();
        }

        if (userRepository.count() == 0) {
            seedUsers();
        }
    }

    private void seedRoles() {
        Role userRole = new Role();
        userRole.setName("ROLE_USER");
        roleRepository.save(userRole);

        Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");
        roleRepository.save(adminRole);

        System.out.println("Roles seeded successfully");
    }

    private void seedUsers() {
        // Create admin user
        Optional<Role> adminRoleOptional = roleRepository.findByName("ROLE_ADMIN");
        if (adminRoleOptional.isPresent()) {
            Role adminRole = adminRoleOptional.get();

            User admin = new User();
            admin.setName("Administrator");
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRoles(Collections.singleton(adminRole));
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());

            userRepository.save(admin);
            System.out.println("Admin user seeded successfully");
        }

        // Create regular user
        Optional<Role> userRoleOptional = roleRepository.findByName("ROLE_USER");
        if (userRoleOptional.isPresent()) {
            Role userRole = userRoleOptional.get();

            User user = new User();
            user.setName("Regular User");
            user.setUsername("user");
            user.setEmail("user@example.com");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRoles(Collections.singleton(userRole));
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            userRepository.save(user);
            System.out.println("Regular user seeded successfully");
        }
    }
}
