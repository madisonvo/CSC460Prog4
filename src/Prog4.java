import java.sql.*;
import java.util.*;
import java.io.*;

public class Prog4 {
    public static void main(String[] args) {
        Connection dbConn = null;
        // if there are a correct amount of command line arguments
        if (args.length == 2) {
            dbConn = getConnection(args);

        // if there are a wrong amount of command line arguments
        } else {
            System.err.println("Usage: java JDBC <username> <password>");
            System.exit(-1);
        }

        answerQueries(dbConn);
    }

    /*
     * Function: getConnection
     * Parameters: String[] args - The array of Strings containing the command line arguments.
     * Returns: dbConn - The JDBC database connection.
     * 
     * Pre-condition: The args parameter contains the username in index 0 and the password in index 1.
     * 
     * Post-condition: TODO
     * 
     * Purpose: 
     */
    private static Connection getConnection(String[] args) {
        final String oracleURL = "jdbc:oracle:thin:@aloe.cs.arizona.edu:1521:oracle"; /* lectura -> aloe access spell */
        String username = args[0]; /* username to access database from command line argument */
        String password = args[1]; /* password to access database from command line argument */

        // try catch to load Oracle JDBC driver
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            System.out.println("Successful loading of Oracle JDBC driver.\n");
        
        // catch ClassNotFoundException
        } catch (ClassNotFoundException e) {
            System.err.println("Error loading Oracle JDBC driver.");
            System.exit(-1);
        }

        Connection dbConn = null; /* initializing database connection */

        // try catch for logging in/connecting to database
        try {
            dbConn = DriverManager.getConnection(oracleURL, username, password); /* database connection using username and password */
            System.out.println("Successful JDBC connection.\n");

        // catch SQLException
        } catch (SQLException e) {
            System.err.println("Could not open JDBC connection.");
            System.exit(-1);
        }

        return dbConn;
    }

    private static void answerQueries(Connection dbConn) {
        Scanner scanner = new Scanner(System.in);

        // continually prompt user for input on queries
        while (true) {
            // printing out menu of queries to choose from
            System.out.println("\nQueries:");
            System.out.println("(a) List all games in the arcade and the names of the members who" + 
                                "have the current high scores");
            System.out.println("(b) Give the names and membership information of all members who" + 
                                "have spent at least $100 on tokens in the past month");
            System.out.println("(c) For a given member, list all arcade rewards that they can purchase" +  
                                "with their tickets");
            // TODO: Create a non-trivial query of our own design
            // TODO: Must be constructed using at least on piece of information gathered from user
            System.out.println("(d) N/A"); 
            System.out.println("(e) Exit\n");

            // prompting input from user
            System.out.println("Enter your query of choice (a, b, c, d, or e)");
            String query = scanner.nextLine();

            switch (query.toLowerCase()) {
                // call queryA() for query a
                case "a":
                    queryA(dbConn);
                    break;

                // call queryB() for query b
                case "b":
                    queryB(dbConn);
                    break;

                // call queryC() for query c
                case "c":
                    queryC(dbConn);
                    break;

                // call queryD() for query d
                case "d":
                    queryD(dbConn);
                    break;

                // exit if user chose e
                case "e":
                    scanner.close();
                    System.out.println("\n***Exiting***");
                    System.exit(0);

                // invalid input
                default:
                    System.out.println("\nPlease choose a valid query (a, b, c, d, or e)");
            }
        }
    }

    // TODO: Implement logic to answer query a
    private static void queryA(Connection dbConn) {}

    // TODO: Implement logic to answer query b
    private static void queryB(Connection dbConn) {}

    // TODO: Implement logic to answer query c
    private static void queryC(Connection dbConn) {}

    // TODO: Implement logic to answer query d
    private static void queryD(Connection dbConn) {}
}
