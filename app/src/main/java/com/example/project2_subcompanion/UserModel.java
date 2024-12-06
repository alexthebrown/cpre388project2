package com.example.project2_subcompanion;

public class UserModel {
    private String text;

    private String id, email, name, userClass;

    public UserModel(String id,String email, String name, String userClass) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.userClass = userClass;
    }

    public String getEmail() {
        return email;
    }
    public String getName() {
        return name;
    }
    public String getUserClass() {
        return userClass;
    }
    public String getId() { return id;}
}
