package com.mosbach.demo.data.api;

public interface Task {

    void setName(String name);
    String getName();
    void setPriority(int priority);
    int getPriority();
    void setEmail(String email);
    String getEmail();
}
