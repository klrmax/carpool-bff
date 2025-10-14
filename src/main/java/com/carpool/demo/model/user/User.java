package com.carpool.demo.model.user;

public class User {
    private int userid;
    private String name;
    private String mobileNumber;
    private String email;
    private String password;
    private String token;
    private long tokenExpiration;

 public User(){}

    public String getName() {return name;}
    public String getMobileNumber() {return mobileNumber;}
    public String getEmail() {return email;}
    public String getPassword() {return password;}
    public String getToken() {return token;}
    public long getTokenExpiration() {return tokenExpiration;}

    public void setName(String name) {this.name = name;}
    public void setMobileNumber(String mobileNumber) {this.mobileNumber = mobileNumber;}
    public void setEmail(String email) {this.email = email;}
    public void setPassword(String password) {this.password = password;}
    public void setToken(String token) {this.token = token;}
    public void setTokenExpiration(long tokenExpiration) {this.tokenExpiration = tokenExpiration;}


}
