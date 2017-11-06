package com.financeTracker.model.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.springframework.stereotype.Component;


@Component
public class DBManager {
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/finance_tracker";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root"; 
    private Connection connection;

    private DBManager() {
        try {
            Class.forName(DBManager.JDBC_DRIVER);
            this.connection = DriverManager.getConnection(DBManager.DB_URL, DBManager.USERNAME, DBManager.PASSWORD);
        } catch (ClassNotFoundException e) {
            System.out.println("Unable to load database driver: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Unable to connect to database: " + e.getMessage());
        }
    }
    
    public Connection getConnection() {
        return this.connection;	
    }

    public void closeConnection() {
        try {
            this.connection.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}