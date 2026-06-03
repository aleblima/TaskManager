package org.example.service;

import org.example.model.User;
import java.util.List;

public interface UserServiceInterface {
    void createUser(User user);
    User getUserById(int id);
    User updateUser(User user);
    void deleteUser(int id);
    List<User> listAllUsers();
}
