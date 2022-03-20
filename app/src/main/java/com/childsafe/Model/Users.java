package com.childsafe.Model;

import java.util.List;

public class Users {
    private String name, mode, email, mobile;
    private List<String> connections;

    Users() {
    }

    public Users(String name, String mode, String email, String mobile, List<String> connections) {
        this.name = name;
        this.mode = mode;
        this.email = email;
        this.mobile = mobile;
        this.connections = connections;
    }

    public Users(String name, String email, String mobile) {
        this.name = name;
        this.email = email;
        this.mobile = mobile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public List<String> getConnections() {
        return connections;
    }

    public void setConnections(List<String> connections) {
        this.connections = connections;
    }

    @Override
    public String toString() {
        return "Users{" +
                "name='" + name + '\'' +
                ", mode='" + mode + '\'' +
                ", email='" + email + '\'' +
                ", mobile='" + mobile + '\'' +
                ", connections=" + connections +
                '}';
    }
}
