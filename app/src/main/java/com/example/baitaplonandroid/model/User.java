package com.example.baitaplonandroid.model;

public class User {
    private String name,email,password,id,phone,photo;
    private boolean requestSent;
    private String Role;

    public User() {
    }

    public User(String name, String email, String password, String id, String phone, String photo, boolean requestSent,String Role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.id = id;
        this.phone = phone;
        this.photo = photo;
        this.requestSent = requestSent;
        this.Role = Role;
    }

    public User(String id, String name, String photo) {
        this.name = name;
        this.id = id;
        this.photo = photo;
    }

    public User(String name, String photo, String id, boolean requestSent) {
        this.name = name;
        this.id = id;
        this.photo = photo;
        this.requestSent = requestSent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public boolean isRequestSent() {
        return requestSent;
    }

    public void setRequestSent(boolean requestSent) {
        this.requestSent = requestSent;
    }

    public String getRole() {
        return Role;
    }

    public void setRole(String role) {
        Role = role;
    }
}
