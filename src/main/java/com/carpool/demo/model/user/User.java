package com.carpool.demo.model.user;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userid;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique = true)
    private String mobileNumber;
    // private String email;
    @Column(nullable = false)
    private String password;
    private String token;
    private long tokenExpiration;

 public User(){}

    public int  getUserid() {
        return userid;
    }
    public String getName() {return name;}
    public String getMobileNumber() {return mobileNumber;}
    // public String getEmail() {return email;}
    public String getPassword() {return password;}
    public String getToken() {return token;}
    public long getTokenExpiration() {return tokenExpiration;}


    public void setUserid(int userid) {}
    public void setName(String name) {this.name = name;}
    public void setMobileNumber(String mobileNumber) {this.mobileNumber = mobileNumber;}
    // public void setEmail(String email) {this.email = email;}
    public void setPassword(String password) {this.password = password;}
    public void setToken(String token) {this.token = token;}
    public void setTokenExpiration(long tokenExpiration) {this.tokenExpiration = tokenExpiration;}


}
