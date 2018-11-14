package com.example.app1;


public class Product {
    private	int	id;
    private	String name, hashID, username, password;
    private boolean loggedIn;

    public Product(String name, String hashID) {
        this.name = name;
        this.hashID = hashID;
    }

    public Product(int id, String name, String hashID) {
        this.id = id;
        this.name = name;
        this.hashID = hashID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHashID() {
        return hashID;
    }

    public void setHashID(String hashID) {
        this.hashID = hashID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getIfLoggedIn() {
        return loggedIn;
    }

    public void setIfLoggedIn(Boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

}
