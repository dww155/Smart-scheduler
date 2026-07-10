package com.dww.chat_app.configuration;

import com.dww.chat_app.entity.Role;
import com.dww.chat_app.entity.User;
import com.dww.chat_app.repository.RoleRepository;
import com.dww.chat_app.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

import static com.dww.chat_app.constant.UserConstant.ADMIN_PASSWORD;
import static com.dww.chat_app.constant.UserConstant.ADMIN_USERNAME;
import static com.dww.chat_app.constant.UserConstant.ROLE_ADMIN;
import static com.dww.chat_app.constant.UserConstant.ROLE_ADMIN_DESCRIPTION;
import static com.dww.chat_app.constant.UserConstant.ROLE_USER;
import static com.dww.chat_app.constant.UserConstant.ROLE_USER_DESCRIPTION;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApplicationInitConfig implements ApplicationRunner {
    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Starting system...");
        Role userRole = createRoleIfNotExists(ROLE_USER, ROLE_USER_DESCRIPTION);
        Role adminRole = createRoleIfNotExists(ROLE_ADMIN, ROLE_ADMIN_DESCRIPTION);

        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(userRole);
        adminRoles.add(adminRole);

        if (!userRepository.existsByUsername(ADMIN_USERNAME)) {
            User admin = User.builder()
                    .username(ADMIN_USERNAME)
                    .roles(adminRoles)
                    .password(passwordEncoder.encode(ADMIN_PASSWORD))
                    .build();

            userRepository.save(admin);

            log.info("Admin created with password: {}", admin.getPassword());

        }

        log.info("System started successfully");
    }

    private Role createRoleIfNotExists(String name, String description) {
        return roleRepository.findById(name)
                .orElseGet(() -> {
                    Role role = Role.builder()
                            .name(name)
                            .description(description)
                            .build();

                    log.info("Created initial role: {}", name);
                    return roleRepository.save(role);
                });
    }
}
