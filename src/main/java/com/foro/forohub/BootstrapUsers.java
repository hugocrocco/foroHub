package com.foro.forohub;

import com.foro.forohub.domain.user.Role;
import com.foro.forohub.domain.user.User;
import com.foro.forohub.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class BootstrapUsers implements CommandLineRunner {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        if (users.count() == 0) {
            User admin = User.builder()
                    .email("admin@mail.com")
                    .username("admin")
                    .password(encoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build();

            User user = User.builder()
                    .email("user@mail.com")
                    .username("user")
                    .password(encoder.encode("user123"))
                    .role(Role.USER)
                    .build();

            users.save(admin);
            users.save(user);
        }
    }
}