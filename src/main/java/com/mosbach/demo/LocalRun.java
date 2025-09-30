package com.mosbach.demo;

import com.mosbach.demo.data.api.TaskManager;
import com.mosbach.demo.data.api.UserManager;
import com.mosbach.demo.data.impl.PropertyFileTaskManagerImpl;
import com.mosbach.demo.data.impl.PropertyFileUserManagerImpl;
import com.mosbach.demo.data.impl.TaskImpl;
import com.mosbach.demo.data.impl.UserImpl;

public class LocalRun {

    public static void main(String[] args) {
        UserManager userManager = PropertyFileUserManagerImpl.getPropertyFileUserManagerImpl("src/main/resources/users.properties");
        userManager.createUser(new UserImpl(
                "She She",
                "she@she.com",
                "123",
                "OFF"
        ));
        userManager.logUserOn("she@she.com", "123");
        TaskManager taskManager = PropertyFileTaskManagerImpl.getPropertyFileTaskManagerImpl("src/main/resources/tasks.properties");
        taskManager.addTask(new TaskImpl("Learn Mathe", 1, "you@you.com"));

    }


}
