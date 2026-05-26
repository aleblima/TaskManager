package org.example.service;

import org.example.dao.UserDAO;
import org.example.model.User;
import java.sql.SQLException;
import java.util.ArrayList;

public class UserServiceImpl implements UserService {
    private final UserDAO userDAO = new UserDAO();

    @Override
    public void createUser(User user) {
        try {
            userDAO.create(user);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar usuário: " + e.getMessage(), e);
        }
    }

    @Override
    public User getUserById(int id) {
        try {
            return userDAO.getById(id);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar usuário: " + e.getMessage(), e);
        }
    }

    @Override
    public User getUserByName(String name) {
        try {
            return userDAO.getByName(name);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar usuário por nome: " + e.getMessage(), e);
        }
    }

    @Override
    public User updateUser(User user) {
        try {
            userDAO.update(user);
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar usuário: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteUser(int id) {
        try {
            userDAO.delete(id);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar usuário: " + e.getMessage(), e);
        }
    }

    @Override
    public ArrayList<User> listAllUsers() {
        try {
            return userDAO.listAll();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar usuários: " + e.getMessage(), e);
        }
    }
}
