package model;

public class User {
    private String name;
    private int id;

    public User(String name, int id){
        this.name = name;
        this.id = id;
    }

    public int getId(){
        return id;
    }
}
