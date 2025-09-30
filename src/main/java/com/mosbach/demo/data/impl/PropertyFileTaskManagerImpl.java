package com.mosbach.demo.data.impl;

import com.mosbach.demo.data.api.Task;
import com.mosbach.demo.data.api.TaskManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PropertyFileTaskManagerImpl implements TaskManager {

    String fileName;
    List<Task> tasks = new ArrayList<>();

    static PropertyFileTaskManagerImpl propertyFileTaskManager = null;

    private PropertyFileTaskManagerImpl(String fileName) {
        this.fileName = fileName;
    }

    static public PropertyFileTaskManagerImpl getPropertyFileTaskManagerImpl(String fileName) {
        if (propertyFileTaskManager == null)
            propertyFileTaskManager = new PropertyFileTaskManagerImpl(fileName);
        return propertyFileTaskManager;
    }

    void loadProperties() {
        List<Task> temp = new ArrayList<>();
        tasks = new ArrayList<>();
        Properties properties = new Properties();
        try {
            properties.load(Files.newInputStream(Paths.get(fileName)));
            int i = 1;
            while (properties.containsKey("Task." + i + ".email")) {
                temp.add(new TaskImpl(
                        properties.getProperty("Task." + i + ".name"),
                        Integer.parseInt(properties.getProperty("Task." + i + ".priority")),
                        properties.getProperty("Task." + i + ".email")
                ));
                i++;
            }
            tasks = temp;
        } catch (IOException e) {
            throw new RuntimeException(e);
            // TODO Add Logger
        }
    }
    void storeProperties() {
        Properties properties = new Properties();
        int i = 1;
        for (Task t : tasks) {
            properties.setProperty("Task." + i +".email", t.getEmail());
            properties.setProperty("Task." + i +".priority", "" + t.getPriority());
            properties.setProperty("Task." + i +".name", t.getName());
            i++;
        }
        try {
            properties.store(Files.newOutputStream(Paths.get(fileName)), "Writing all tasks to properties file");
        }
        catch (IOException e) {
            throw new RuntimeException(e);
            // TODO Add Logger
        }
    }


    @Override
    public List<Task> getAllTasksPerEmail(String email) {
        loadProperties();
        List<Task> tasksForEmail = new ArrayList<>();
        for (Task t : tasks)
            if (t.getEmail().equals(email))
                tasksForEmail.add(t);
        return tasksForEmail;
    }

    @Override
    public boolean addTask(Task task) {
        loadProperties();
        tasks.add(task);
        storeProperties();
        return true;
    }
}
