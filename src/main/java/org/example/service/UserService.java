package org.example.service;

import org.example.model.User;
import java.util.ArrayList;

public interface UserService {
    void createUser(User user);
    User getUserById(int id);
    User getUserByName(String name);
    User updateUser(User user);
    void deleteUser(int id);
    ArrayList<User> listAllUsers();
}
