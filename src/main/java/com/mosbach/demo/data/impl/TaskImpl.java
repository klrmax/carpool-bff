package com.mosbach.demo.data.impl;

import com.mosbach.demo.data.api.Task;

public class TaskImpl implements Task {

    String name;
    int priority;
    String email;

    public TaskImpl(String name, int priority, String email) {
        this.name = name;
        this.priority = priority;
        this.email = email;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getEmail() {
        return email;
    }
}
