package com.valence.config;

import com.valence.model.User;
import com.valence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private static final String SEED_EMAIL = "admin@valence.dev";
    private static final String SEED_PASSWORD = "changeme123";
    private static final String SEED_DISPLAY_NAME = "Admin";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail(SEED_EMAIL)) {
            log.debug("Default user '{}' already exists — skipping", SEED_EMAIL);
            return;
        }

        User user = new User();
        user.setEmail(SEED_EMAIL);
        user.setPasswordHash(passwordEncoder.encode(SEED_PASSWORD));
        user.setDisplayName(SEED_DISPLAY_NAME);
        userRepository.save(user);
        log.info("Default user created: {}", SEED_EMAIL);
    }
}
