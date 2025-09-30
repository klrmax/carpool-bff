package com.mosbach.demo;

import com.mosbach.demo.data.api.TaskManager;
import com.mosbach.demo.data.api.UserManager;
import com.mosbach.demo.data.impl.*;
import com.mosbach.demo.model.alexa.AlexaRO;
import com.mosbach.demo.model.alexa.OutputSpeechRO;
import com.mosbach.demo.model.alexa.ResponseRO;
import com.mosbach.demo.model.user.Token;
import com.mosbach.demo.model.user.TokenAnswer;
import com.mosbach.demo.model.user.User;
import com.mosbach.demo.model.task.*;
import com.mosbach.demo.model.user.UserWithName;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api")
public class MappingController {

    TaskManager taskManager =
            PropertyFileTaskManagerImpl.getPropertyFileTaskManagerImpl("src/main/resources/tasks.properties");
            // PostgresTaskManagerImpl.getPostgresTaskManagerImpl();

    UserManager userManager =
            PropertyFileUserManagerImpl.getPropertyFileUserManagerImpl("src/main/resources/users.properties");
            // PostgresUserManagerImpl.getPostgresUserManagerImpl();

    @PostMapping(
            path = "/login",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    @ResponseStatus(HttpStatus.OK)
    public TokenAnswer loginUser(@RequestBody User user) {

        Logger myLogger = Logger.getLogger("UserLoggingOn");
        myLogger.info("Received a POST request on login with email " + user.getEmail()); //wichtig fuer debuggen

        String token = userManager.logUserOn(user.getEmail(), user.getPassword());
        myLogger.info("Token generated " + token);

        // TODO
        // Fehlerfall behandeln

        return
                new TokenAnswer(token,"200"); //token und valid time min 
    }


    @DeleteMapping(
            path = "/login",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    @ResponseStatus(HttpStatus.OK)
    public com.mosbach.demo.model.user.MessageAnswer logOffUser(@RequestBody Token token) {

        Logger myLogger = Logger.getLogger("UserLoggingOff");
        myLogger.info("Received a DELETE request on login with token " + token.getToken());

        boolean couldLogoffUser =
                userManager.logUserOff(userManager.getUserEmailFromToken(token.getToken()));

        myLogger.info("User logged off " + couldLogoffUser);

        // TODO
        // Fehlerfall behandeln

        return
                new com.mosbach.demo.model.user.MessageAnswer("User logged out.");
    }


    @PostMapping(
            path = "/user",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    @ResponseStatus(HttpStatus.OK)
    public com.mosbach.demo.model.user.MessageAnswer createUser(@RequestBody UserWithName userWithName) {

        Logger myLogger = Logger.getLogger("UserCreate");
        myLogger.info("Received a POST request on user with email " + userWithName.getEmail());

        boolean couldCreateUser = userManager
                        .createUser(
                            new UserImpl(
                                    userWithName.getName(),
                                    userWithName.getEmail(),
                                    userWithName.getPassword(),
                                    "OFF"
                            )
                        );
        myLogger.info("User created " + couldCreateUser);

        // TODO
        // Fehlerfall behandeln

        return
                new com.mosbach.demo.model.user.MessageAnswer("User created.");
    }


    @PostMapping(
            path = "/task",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    @ResponseStatus(HttpStatus.OK)
    public com.mosbach.demo.model.user.MessageAnswer loginUser(@RequestBody TokenTask tokenTask) {

        Logger myLogger = Logger.getLogger("AddTask");
        myLogger.info("Received a POST request on task with token " + tokenTask.getToken());

        String email = userManager.getUserEmailFromToken(tokenTask.getToken());
        myLogger.info("Found the following email for this token " + email);
        if (email.equals("NOT-FOUND"))
            return
                    new com.mosbach.demo.model.user.MessageAnswer("No user found or not logged on.");;
        boolean couldCreateTask = taskManager
                .addTask(
                        new TaskImpl(
                                tokenTask.getTask().getName(),
                                tokenTask.getTask().getPriority(),
                                email
                        )
                );

        myLogger.info("Task created " + couldCreateTask);

        // TODO
        // Fehlerfall behandeln

        return
                new com.mosbach.demo.model.user.MessageAnswer("Task created.");
    }

    @GetMapping("/task")
    public TaskList getTasks(@RequestParam(value = "token", defaultValue = "123") String token) {

        Logger myLogger = Logger.getLogger("TaskLogger");
        myLogger.info("Received a GET request on task with token " + token);

        String email = userManager.getUserEmailFromToken(token);
        List<com.mosbach.demo.data.api.Task> tasks = taskManager.getAllTasksPerEmail(email);
        List<Task> result = new ArrayList<>();
        for (com.mosbach.demo.data.api.Task t : tasks)
            result.add(new Task(t.getName(), t.getPriority()));

        // TODO
        // Fehlerfall behandeln

        return
                new TaskList(result);
    }

    @GetMapping("/task/createtables")
    @ResponseStatus(HttpStatus.OK)
    public String createTask() {
        PostgresTaskManagerImpl.getPostgresTaskManagerImpl().createTaskTable();
        PostgresUserManagerImpl.getPostgresUserManagerImpl().createUserTable();
        return "Database Tables created";
    }






    @PostMapping(
            path = "/alexa",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    @ResponseStatus(HttpStatus.OK)
    public AlexaRO createTask(@RequestBody AlexaRO alexaRO) {

        Logger.getLogger("MappingController").log(Level.INFO,"MappingController POST /alexa ");
        String outText = "";

        if (alexaRO.getRequest().getType().equalsIgnoreCase("LaunchRequest"))
            outText += "Welcome to the Mosbach Task Organizer. ";

        if (alexaRO.getRequest().getType().equalsIgnoreCase("IntentRequest")
                &&
                (alexaRO.getRequest().getIntent().getName().equalsIgnoreCase("TaskReadIntent"))
        ) {
            List<com.mosbach.demo.data.api.Task> tasks = taskManager.getAllTasksPerEmail("mh@test.com");
            if (!tasks.isEmpty()) {
                outText += "You have to do the following tasks. ";
                int i = 1;
                for (com.mosbach.demo.data.api.Task t : tasks) {
                    outText += "Task Number " + i + " with Name " + t.getName()
                        + " and priority " + t.getPriority() + " . ";
                    i++;
                }
            }
            else outText += "This is your lucky day. You have no tasks to do. ";
        }
        return
                prepareResponse(alexaRO, outText, true);
    }


    @PostMapping(
            path = "/alexa",
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    public AlexaRO getTasks(@RequestBody AlexaRO alexaRO) {

        String outText = "";


        return alexaRO;
    }

    private AlexaRO prepareResponse(AlexaRO alexaRO, String outText, boolean shouldEndSession) {

        alexaRO.setRequest(null);
        alexaRO.setSession(null);
        alexaRO.setContext(null);
        OutputSpeechRO outputSpeechRO = new OutputSpeechRO();
        outputSpeechRO.setType("PlainText");
        outputSpeechRO.setText(outText);
        ResponseRO response = new ResponseRO(outputSpeechRO, shouldEndSession);
        alexaRO.setResponse(response);
        return alexaRO;
    }

}
