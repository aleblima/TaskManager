package service;

import exception.InvalidUserException;
import model.User;

import java.util.ArrayList;

public class UserService {
    private ArrayList<User> users = new ArrayList<>();
    private static int id = 0;

    public void criarUsuario(String nome){
        id++;
        users.add(new User(nome, id));
        System.out.println("Usuário criado: "+ nome +", id: "+ id);
    }

    public User getUserById(int id) throws InvalidUserException {
        for (User user : users){
            if (user.getId() == id){
                return user;
            }
        }
        throw new InvalidUserException("Usuário não encontrado");
    }
}
