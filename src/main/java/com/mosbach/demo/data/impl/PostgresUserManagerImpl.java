package com.mosbach.demo.data.impl;

import com.mosbach.demo.data.api.Task;
import com.mosbach.demo.data.api.TaskManager;
import com.mosbach.demo.data.api.User;
import com.mosbach.demo.data.api.UserManager;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PostgresUserManagerImpl implements UserManager {

    String databaseURL = "jdbc:postgresql://cee3ebbhveeoab.cluster-czrs8kj4isg7.us-east-1.rds.amazonaws.com:5432/d6juffo78h12bv";
    String username = "udh9d9snr2ffb4";
    String password = "p3eb32789be02f537b2e0bc5f95aa66b27e345fc435534dae7933cd36073d598d";
    BasicDataSource basicDataSource;

    // Singleton
    static PostgresUserManagerImpl postgresUserManager = null;
    private PostgresUserManagerImpl() {
        basicDataSource = new BasicDataSource();
        basicDataSource.setUrl(databaseURL);
        basicDataSource.setUsername(username);
        basicDataSource.setPassword(password);
    }
    public static PostgresUserManagerImpl getPostgresUserManagerImpl() {
        if (postgresUserManager == null)
            postgresUserManager = new PostgresUserManagerImpl();
        return postgresUserManager;
    }


    @Override
    public boolean createUser(User user) {

        final Logger createUserLogger = Logger.getLogger("CreateUserLogger");
        createUserLogger.log(Level.INFO,"Start creating user " + user.getName());
        Statement stmt = null;
        Connection connection = null;

        try {
            connection = basicDataSource.getConnection();
            stmt = connection.createStatement();
            String udapteSQL = "INSERT into users (name, email, password, token) VALUES (" +
                    "'" + user.getName() + "', '" + user.getEmail() + "', '" + user.getPassword() + "', '" + user.getToken() + "')";
            stmt.executeUpdate(udapteSQL);

            stmt.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        try {
            stmt.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public String logUserOn(String email, String password) {
        final Logger logOnUserLogger = Logger.getLogger("logOnUserLogger");
        logOnUserLogger.log(Level.INFO,"Start logging on user " + email);
        Statement stmt = null;
        Connection connection = null;

        // TODO Everybody gets the same token, it only works if we have only 1 user!!!
        String newToken = "123";
        try {
            connection = basicDataSource.getConnection();
            stmt = connection.createStatement();
            String udapteSQL = "UPDATE users SET token  = " +
                    "'" + newToken + "' " +
                    "WHERE email = '" + email + "' AND password = '" + password + "'";
            stmt.executeUpdate(udapteSQL);

            stmt.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return "OFF";
        }
        try {
            stmt.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newToken;
    }

    @Override
    public boolean logUserOff(String email) {
        final Logger logOffUserLogger = Logger.getLogger("logOffUserLogger");
        logOffUserLogger.log(Level.INFO,"Start logging on user " + email);
        Statement stmt = null;
        Connection connection = null;

        try {
            connection = basicDataSource.getConnection();
            stmt = connection.createStatement();
            String udapteSQL = "UPDATE users SET token  = " +
                    "'OFF' " +
                    "WHERE email = '" + email + "'";
            stmt.executeUpdate(udapteSQL);

            stmt.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        try {
            stmt.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public String getUserEmailFromToken(String token) {

        final Logger readEmailLogger = Logger.getLogger("ReadEmailLogger");
        readEmailLogger.log(Level.INFO,"Start reading users from DB. ");

        String foundEmail = "NOT-FOUND";
        Statement stmt = null;
        Connection connection = null;

        try {
            connection = basicDataSource.getConnection();
            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM users");

            while (rs.next()) {
                User u = new UserImpl(
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("token")
                );
                if (u.getToken().equals(token))
                    foundEmail = u.getEmail();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            stmt.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return foundEmail;

    }



    public void createUserTable() {
        // Be carefull: It deletes data if table already exists.
        Statement stmt = null;
        Connection connection = null;
        try {
            connection = basicDataSource.getConnection();
            stmt = connection.createStatement();

            stmt.executeUpdate("DROP TABLE IF EXISTS users");
            stmt.executeUpdate("CREATE TABLE users (" +
                    "id SERIAL PRIMARY KEY, " +
                    "name varchar(100) NOT NULL, " +
                    "email varchar(100) NOT NULL, " +
                    "password varchar(100) NOT NULL, " +
                    "token varchar(100) NOT NULL)");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            stmt.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


}
