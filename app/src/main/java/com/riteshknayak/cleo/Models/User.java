package com.riteshknayak.cleo.Models;

public class User {
    private String name, uid, email, pass;
    private int credits;
    private Boolean newUser, premiumUser;

    public User(String name, String uid, String email, String pass, int credits, Boolean newUser, Boolean premiumUser) {
        this.name = name;
        this.uid = uid;
        this.email = email;
        this.pass = pass;
        this.credits = credits;
        this.newUser = newUser;
        this.premiumUser = premiumUser;
    }

    public Boolean getPremium() {
        return premiumUser;
    }

    public void setPremium(Boolean premiumUser) {
        this.premiumUser = premiumUser;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public Boolean getNewUser() {
        return newUser;
    }

    public void setNewUser(Boolean newUser) {
        this.newUser = newUser;
    }
}
