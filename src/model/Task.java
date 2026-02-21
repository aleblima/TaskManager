package model;

public class Task {
    private final User user;
    private String title;
    private int id;
    private boolean status;
    private final Category category;

    public Task(String title, int id, User user, Category category){
        this.title = title;
        this.id = id;
        this.user = user;
        this.category = category;
        status = false;
    }

    public int getId(){
        return id;
    }
    public String getTitle(){
        return title;
    }
    public String getCategory(){
        return category.getCategory();
    }
    public String getStatus(){
        return this.status ? "Concluída" : "Não concluída";
    }
}
