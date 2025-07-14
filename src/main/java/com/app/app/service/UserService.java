package com.app.app.service;

import com.app.app.model.Role;
import com.app.app.model.User;
import com.app.app.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User saveOrUpdateUser(String email, String name, String pictureUrl) {
        return userRepository.findById(email).map(user -> {
            user.setName(name);
            user.setPictureUrl(pictureUrl);
            return userRepository.save(user);
        }).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setPictureUrl(pictureUrl);
            newUser.setRole(Role.ROLE_USER); // use Enum
            return userRepository.save(newUser);
        });
    }
}

