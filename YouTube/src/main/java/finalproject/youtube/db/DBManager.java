package finalproject.youtube.db;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

@Component
public class DBManager {

    private String DB_HOSTNAME;
    private int DB_PORT;
    private String DB_NAME;
    private String DB_USERNAME;
    private String DB_PASSWORD;

    private static Connection connection;

    public DBManager() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Sorry, driver not found. Please check your dependencies!");
        }
        setCredentials();
        this.connection = createConnection();
    }

    private void setCredentials() {
        File configFile = new File("src\\main\\resources\\static\\dbconfig.txt");

        try (FileInputStream fis = new FileInputStream(configFile);
             Scanner sc = new Scanner(fis)
        ) {
            String line = sc.nextLine();
            String[] credentials = line.split(",");
            DB_HOSTNAME = credentials[0];
            DB_PORT = Integer.parseInt(credentials[1]);
            DB_NAME = credentials[2];
            DB_USERNAME = credentials[3];
            DB_PASSWORD = credentials[4];

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            System.out.println("Error while reading from file.");
        }
    }

    private Connection createConnection() {
        try {
            return DriverManager.getConnection("jdbc:mysql://" + DB_HOSTNAME + ":" + DB_PORT + "/" + DB_NAME, DB_USERNAME, DB_PASSWORD);
        } catch (SQLException e) {
            System.out.println("Error connecting to database. Check your credentials!");
            return null;
        }
    }

    public Connection getConnection() throws SQLException {
        if (connection.isClosed()) {
            connection = createConnection();
        }
        return connection;
    }
}


