package ru.spbu.office.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.spbu.office.model.User;
import ru.spbu.office.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    public boolean exists(String login) {
        return users.existsByLogin(login);
    }

    public User register(String login, String rawPassword) {
        User u = new User();
        u.setLogin(login);
        u.setPasswordHash(encoder.encode(rawPassword));
        return users.save(u);
    }

    public User getByLogin(String login) {
        return users.findByLogin(login).orElseThrow();
    }
}
