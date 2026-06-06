package org.example.model;

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

    public String getNome() { return name; }

    public void setNome(String name) { this.name = name; }

    public void setId(int id) { this.id = id; }
}
