import java.sql.*;
import java.util.*;
import java.io.*;

/**
* Author: Tariq Wilson & Madison Vo
* Purpose: This Program implements a two-tier client-server architecture.
*          It runs the Oracle DBMS on aloe.cs.arizona.edu. It creates and populates
*          tables as well as has a client user interface that handles the functionality
*          required by the client. Our application supports record insertion,
*          deletion, updates, and queries.
* Course: CSC 460
* Instructor: Lester McCann
* TA: Ahmad Musa, Jake Bode, Priyansh Nayak
* Assignment: Prog4.java
*
*/

/*---------------------------------------------------------------------
|  Class Prog4
|
|  Purpose:  This class represents a program for managing data related to
|            an event management system. It provides functionalities for
|            creating tables, importing data from CSV files into the database,
|            updating tables, and executing custom queries. Users can interact
|            with the program via a menu system.
|
|  Pre-condition:  The Oracle JDBC driver is available. The database
|                  connection parameters (URL, username, password) are
|                  correctly configured. CSV files containing data are
|                  available in the local directory.
|
|  Post-condition: The program provides options for users to perform
|                  various data management tasks. Database tables may be
|                  created if they do not exist, and data may be imported
|                  from CSV files into the database. Users can update tables
|                  and execute custom queries.
|
|  Constructors:   None.
|
|  Methods:        - main(String[] args): The main method of the program
|                    that establishes a connection to the database, creates
|                    tables if necessary, imports data, displays a menu, and
|                    allows users to choose from different functionalities.
|                 - getConnection(String[] args): Method to establish a database
|                    connection using the provided username and password.
|                 - createTables(Connection dbConn): Method to create tables in
|                    the database to store data for each entity if they do not
|                    already exist.
|                 - importMemberData(Connection dbConn, String file): Method to
|                    import member data from a CSV file into the Member table
|                    in the database.
|                 - importGameData(Connection dbConn, String file): Method to import
|                    game data from a CSV file into the Game table in the database.
|                 - importGameplayData(Connection dbConn, String file): Method to
|                    import gameplay data from a CSV file into the Gameplay table
|                    in the database.
|                 - importPrizeData(Connection dbConn, String file): Method to import
|                    prize data from a CSV file into the Prize table in the database.
|                 - importFoodCouponData(Connection dbConn, String file): Method to
|                    import food coupon data from a CSV file into the FoodCoupon table
|                    in the database.
|                 - importMembershipTierData(Connection dbConn, String file): Method
|                    to import membership tier data from a CSV file into the MembershipTier
|                    table in the database.
|                 - importTransactionData(Connection dbConn, String file): Method to import
|                    transaction data from a CSV file into the Transaction table in the database.
|                 - tableExists(Connection dbConn, String tableName): Method to check if
|                    a table exists in the database.
|                 - rowExists(Connection dbConn, String tableName, String id): Method to check
|                    if a row with the given ID exists in the specified table.
|                 - promptUpdate(Connection dbConn): Method to prompt the user if they want to
|                    update the tables and execute the update method accordingly.
|                 - update(Connection dbConn): Method to prompt the user for the table they want
|                    to update and call the corresponding update method.
|                 - updateMember(Connection dbConn): Method to prompt the user for the type of
|                    update they want to perform on the Member table and call the corresponding
|                    method (addMember, editMember, deleteMember).
|                 - addMember(Connection dbConn): Method to add a new member to the Member table
|                    in the database.
|                 - editMember(Connection dbConn): Method to edit an existing member in the Member
|                    table in the database.
|                 - deleteMember(Connection dbConn): Method to delete a member from the Member table
|                    in the database.
|                 - addGame(Connection dbConn): Method to add a new game to the Game table
|                    in the database.
|                 - deleteGame(Connection dbConn): Method to delete a game from the Game table
|                    in the database.
|                 - addPrize(Connection dbConn): Method to add a new prize to the Prize table
|                    in the database.
|                 - deletePrize(Connection dbConn): Method to delete a prize from the Prize table
|                    in the database.
|
|  Constants:      None.
|
|  Returns:        None.
*-------------------------------------------------------------------*/
public class Prog4 {

    public static void main(String[] args) throws SQLException {
        Connection dbConn = null;
        // if there are a correct amount of command line arguments
        if (args.length == 2) {
            dbConn = getConnection(args);

        // if there are a wrong amount of command line arguments
        } else {
            System.err.println("Usage: java JDBC <username> <password>");
            System.exit(-1);
        }

        // drop(dbConn);
        
        // creating tables
        if (createTables(dbConn)) {
            System.out.println("Tables created successfully.");
            importMemberData(dbConn, "Member.csv");
            importGameData(dbConn, "Game.csv");
            importGameplayData(dbConn, "Gameplay.csv");
            importPrizeData(dbConn, "Prize.csv");
            importFoodCouponData(dbConn, "FoodCoupon.csv");
            importMembershipTierData(dbConn, "MembershipTier.csv");
            importTransactionData(dbConn, "Transaction.csv");
            System.out.println("Data imported successfully.");

        // tables already exist
        } else {
            System.out.println("Tables already exist. Skipping table creation and data import.");
        }

        promptUpdate(dbConn); // prompt user to update tables
        
        answerQueries(dbConn); // prompt user to get answer to queries
    }

    /*---------------------------------------------------------------------
    |  Function: getConnection
    |
    |  Purpose:  Establishes a connection to the database using the provided
    |            username and password. If successful, it returns the
    |            Connection object representing the database connection.
    |
    |  Pre-condition:  The Oracle JDBC driver is available. The database
    |                  connection parameters (URL, username, password) are
    |                  correctly configured.
    |
    |  Post-condition: A valid database connection is established.
    |
    |  Parameters:
    |      args - An array of Strings containing the username and password
    |             for database authentication.
    |
    |  Returns:  Connection - A Connection object representing the database
    |                        connection if successful, null otherwise.
    *-------------------------------------------------------------------*/
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

        return dbConn; // return JDBC connection
    }

    /*---------------------------------------------------------------------
    |  Method createTables(connection)
    |
    |  Purpose:  Creates tables in the database to store data for each entity
    |            if they do not already exist. The method executes SQL statements
    |            to create tables for the Member, Game, Gameplay, Prize, FoodCoupon,
    |            and MembershipTier entities.
    |
    |  Pre-condition:  Connection to the database is established.
    |
    |  Post-condition: Tables are created in the database if they do not exist.
    |
    |  Parameters:
    |      connection -- Connection object representing the database connection.
    |
    |  Returns:  boolean -- True if tables are created successfully, false otherwise.
    *-------------------------------------------------------------------*/
    public static boolean createTables(Connection dbConn) {
        // SQL statements to create table for member
        String createMemberTable = "CREATE TABLE Member (" +
        "MemberID INT PRIMARY KEY, " +
        "Fname VARCHAR(50), " +
        "Lname VARCHAR(50), " +
        "TelephoneNum VARCHAR(20), " +
        "Address VARCHAR(255), " +
        "GameTokens INT, " +
        "TotalSpending DECIMAL(10, 2), " +
        "MembershipTier VARCHAR(50), " +
        "VisitCount INT, " +
        "LastVisitDate DATE, " +
        "TotalTickets INT" +
        ")";
        // SQL statements to create table for game
        String createGameTable = "CREATE TABLE Game (" +
        "GameID INT PRIMARY KEY, " +
        "Name VARCHAR(100), " +
        "TokenCost INT, " +
        "Tickets INT" +
        ")";
        // SQL statements to create table for gameplay
        String createGameplayTable = "CREATE TABLE Gameplay (" +
        "GameplayID INT PRIMARY KEY, " +
        "MemberID INT, " +
        "GameID INT, " +
        "Score INT, " +
        "TicketsEarned INT, " +
        "\"Date\" DATE, " +
        "FOREIGN KEY (MemberID) REFERENCES Member(MemberID), " +
        "FOREIGN KEY (GameID) REFERENCES Game(GameID)" +
        ")";
        // SQL statements to create table for prize
        String createPrizeTable = "CREATE TABLE Prize (" +
        "PrizeID INT PRIMARY KEY, " +
        "Name VARCHAR(100), " +
        "TicketCost INT" +
        ")";
        // SQL statements to create table for foodcoupon
        String createFoodCouponTable = "CREATE TABLE FoodCoupon (" +
        "FoodCouponID INT PRIMARY KEY, " +
        "MemberID INT, " +
        "RedeemedFood VARCHAR(100), " +
        "Used NUMBER(1), " +
        "FOREIGN KEY (MemberID) REFERENCES Member(MemberID)" +
        ")";
        // SQL statements to create table for membershiptier
        String createMembershipTierTable = "CREATE TABLE MembershipTier (" +
        "MembershipTierID INT PRIMARY KEY, " +
        "Name VARCHAR(100), " +
        "TotalSpendingReq DECIMAL(10, 2), " +
        "DiscountPercentage DECIMAL(5, 2), " +
        "FreeTickets INT" +
        ")";
        // SQL statements to create table for transaction
        String createTransactionTable = "CREATE TABLE Transaction (" +
        "TransactionID INT PRIMARY KEY, " +
        "Type VARCHAR(50), " +
        "Amount DECIMAL(10, 2), " +
        "\"Date\" DATE" +
        ")";

        //list of the names of tables to be used later for creation processing
        String[] tableNames = {"Member", "Game", "Gameplay", "Prize", "FoodCoupon", "MembershipTier", "Transaction"};
        String[] createTableQueries = {createMemberTable, createGameTable, createGameplayTable, createPrizeTable, createFoodCouponTable, createMembershipTierTable, createTransactionTable};
        
        // here the table creation statements are ran
        try (Statement statement = dbConn.createStatement()) {
            // Check if tables already exist
            for (int i = 0; i < tableNames.length; i++) {
                if (tableExists(dbConn, tableNames[i])) {
                    System.out.println("Table " + tableNames[i] + " already exists.");
                    return false; // if the table exists 
                } else {
                    // if the table does not already exist it is created as well as granted public privilege.
                    statement.execute(createTableQueries[i]);
                    System.out.println("Table " + tableNames[i] + " created successfully.");
                    String grantStatement = "GRANT SELECT ON " + tableNames[i] + " TO PUBLIC";
                    statement.execute(grantStatement);
                    System.out.println("SELECT privileges granted on " + tableNames[i] + " to PUBLIC.");
                }
            }
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /*---------------------------------------------------------------------
    |  Method tableExists(connection, tableName)
    |
    |  Purpose:  Checks if a given table exists in the database.
    |
    |  Pre-condition:  The JDBC database connection is valid.
    |
    |  Post-condition: Returns true if the table exists, false otherwise.
    |
    |  Parameters:
    |      dbConn -- Connection object representing the database connection.
    |      tableName -- Name of the table to check existence.
    |
    |  Returns:  boolean -- True if the table exists, false otherwise.
    *-------------------------------------------------------------------*/
    private static boolean tableExists(Connection dbConn, String tableName) {
        // try catch for checking to see if given table name exists
        try {
            DatabaseMetaData metadata = dbConn.getMetaData(); /* grabbing data from JDBC database connection */
            ResultSet tables = metadata.getTables(null, null, tableName, null); /* grabbing given table name */

            // true if not found, false otherwise
            return tables.next();

        // catch SQLException
        } catch (SQLException e) {
            System.err.println("Could not check to see if table already exists.");
            return false;
        }
    }

    /*---------------------------------------------------------------------
    |  Method importMemberData(connection, file)
    |
    |  Purpose:  Imports member data from a CSV file into the Member table
    |            in the database.
    |
    |  Pre-condition:  The JDBC database connection is valid and points to
    |                  the target database.
    |
    |  Post-condition: Member data is inserted into the Member table if it
    |                  does not already exist.
    |
    |  Parameters:
    |      dbConn -- Connection object representing the database connection.
    |      file -- Name of the CSV file containing member data.
    |
    |  Throws:
    |      SQLException -- If an SQL exception occurs during database operations.
    |
    |  Returns:  None.
    *-------------------------------------------------------------------*/
    private static void importMemberData(Connection dbConn, String file) {
        String tableName = "Member"; // initialize string for table name

        try {
            String insert = "INSERT INTO " + tableName + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'), ?)"; // insert statement for member table
            PreparedStatement statement = dbConn.prepareStatement(insert); // prepare insert statement
            BufferedReader reader = new BufferedReader(new FileReader(file)); // initialize reader for csv file
            String line = null; // initialize line variable
            // iterate through member.csv file
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(","); // split data
                // if the memberID doesn't yet exist within the table
                if (!rowExists(dbConn, tableName, data[0])) {
                    // set all attributes of the member table
                    statement.setInt(1, Integer.valueOf(data[0]));
                    statement.setString(2, data[1]);
                    statement.setString(3, data[2]);
                    statement.setString(4, data[3]);
                    statement.setString(5, data[4]);
                    statement.setInt(6, Integer.valueOf(data[5]));
                    statement.setDouble(7, Double.valueOf(data[6]));
                    statement.setString(8, data[7]);
                    statement.setInt(9, Integer.valueOf(data[8]));
                    statement.setDate(10, java.sql.Date.valueOf(data[9]));
                    statement.setInt(11, Integer.valueOf(data[10]));

                    // execute update
                    statement.executeUpdate();
                // memberID already exists in table
                } else {
                    System.out.println("Skipping duplicate row");
                }
            }

            System.out.println("Successful import of " + file + " into table " + tableName + ".");
            reader.close(); // closer reader
            statement.close(); // close statement
        } catch (Exception e) {}
    }

    /*---------------------------------------------------------------------
    |  Method importGameData(connection, file)
    |
    |  Purpose:  Imports game data from a CSV file into the Game table
    |            in the database.
    |
    |  Pre-condition:  The JDBC database connection is valid and points to
    |                  the target database.
    |
    |  Post-condition: Game data is inserted into the Game table if it
    |                  does not already exist.
    |
    |  Parameters:
    |      dbConn -- Connection object representing the database connection.
    |      file -- Name of the CSV file containing game data.
    |
    |  Throws:
    |      SQLException -- If an SQL exception occurs during database operations.
    |
    |  Returns:  None.
    *-------------------------------------------------------------------*/
    private static void importGameData(Connection dbConn, String file) {
        String tableName = "Game"; // initialize string for table name
        try {
            String insert = "INSERT INTO " + tableName + " VALUES (?, ?, ?, ?)"; // insert statement for game table
            PreparedStatement statement = dbConn.prepareStatement(insert); // prepare insert statement 
            BufferedReader reader = new BufferedReader(new FileReader(file)); // initialize reader for csv file
            String line = null; // initialize line variable
            // iterate through each line of csv file
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(","); // split data
                // if gameID doesn't yet exist in the table
                if (!rowExists(dbConn, tableName, data[0])) {
                    // set all attributes of game
                    statement.setInt(1, Integer.valueOf(data[0]));
                    statement.setString(2, data[1]);
                    statement.setInt(3, Integer.valueOf(data[2]));
                    statement.setInt(4, Integer.valueOf(data[3]));

                    // execute update
                    statement.executeUpdate();
                // gameID already exists in table
                } else {
                    System.out.println("Skipping duplicate row");
                }
            }

            System.out.println("Successful import of " + file + " into table " + tableName + ".");
            reader.close(); // close reader
            statement.close(); // closer statement
        } catch (Exception e) {}
    }

    /*---------------------------------------------------------------------
    |  Method importGameplayData(connection, file)
    |
    |  Purpose:  Imports gameplay data from a CSV file into the Gameplay table
    |            in the database.
    |
    |  Pre-condition:  The JDBC database connection is valid and points to
    |                  the target database.
    |
    |  Post-condition: Gameplay data is inserted into the Gameplay table if it
    |                  does not already exist.
    |
    |  Parameters:
    |      dbConn -- Connection object representing the database connection.
    |      file -- Name of the CSV file containing gameplay data.
    |
    |  Throws:
    |      SQLException -- If an SQL exception occurs during database operations.
    |
    |  Returns:  None.
    *-------------------------------------------------------------------*/
    private static void importGameplayData(Connection dbConn, String file) {
        String tableName = "Gameplay"; // initialize string for table name
        
        try {
            String insert = "INSERT INTO " + tableName + " VALUES (?, ?, ?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'))"; // insert statement for gameplay table
            PreparedStatement statement = dbConn.prepareStatement(insert); // prepare insert statement
            BufferedReader reader = new BufferedReader(new FileReader(file)); // intialize reader for csv file
            String line = null; // initialize line variable
            // iterate through every line in csv file
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(","); // split data
                // if gameplayID doesn't exist within the table
                if (!rowExists(dbConn, tableName, data[0])) {
                    // set all attributes for gameplay
                    statement.setInt(1, Integer.valueOf(data[0]));
                    statement.setInt(2, Integer.valueOf(data[1]));
                    statement.setInt(3, Integer.valueOf(data[2]));
                    statement.setInt(4, Integer.valueOf(data[3]));
                    statement.setInt(5, Integer.valueOf(data[4]));
                    statement.setString(6, data[5]);

                    // execute update
                    statement.executeUpdate();
                // gameplayID already exists within the table
                } else {
                    System.out.println("Skipping duplicate row");
                }
            }

            System.out.println("Successful import of " + file + " into table " + tableName + ".");
            reader.close(); // close reader
            statement.close(); // close statement
        } catch (Exception e) {}
    }

    /*---------------------------------------------------------------------
    |  Method importPrizeData(connection, file)
    |
    |  Purpose:  Imports prize data from a CSV file into the Prize table
    |            in the database.
    |
    |  Pre-condition:  The JDBC database connection is valid and points to
    |                  the target database.
    |
    |  Post-condition: Prize data is inserted into the Prize table if it
    |                  does not already exist.
    |
    |  Parameters:
    |      dbConn -- Connection object representing the database connection.
    |      file -- Name of the CSV file containing prize data.
    |
    |  Throws:
    |      SQLException -- If an SQL exception occurs during database operations.
    |
    |  Returns:  None.
    *-------------------------------------------------------------------*/
    private static void importPrizeData(Connection dbConn, String file) {
        String tableName = "Prize"; // initialize string for prize table

        try {
            String insert = "INSERT INTO " + tableName + " VALUES (?, ?, ?)"; // insert statement
            PreparedStatement statement = dbConn.prepareStatement(insert); // prepare insert statement
            BufferedReader reader = new BufferedReader(new FileReader(file)); // initialize reader for csv file
            String line = null; // initialize line variable
            // iterate through each line of csv file
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(","); // split data
                // if prizeID doesn't exist within the table
                if (!rowExists(dbConn, tableName, data[0])) {
                    // set all attributes of prize
                    statement.setInt(1, Integer.valueOf(data[0]));
                    statement.setString(2, data[1]);
                    statement.setInt(3, Integer.valueOf(data[2]));

                    // execute update
                    statement.executeUpdate();
                // prizeID already exists within the table
                } else {
                    System.out.println("Skipping duplicate row");
                }
            }

            System.out.println("Successful import of " + file + " into table " + tableName + ".");
            reader.close(); // close reader
            statement.close(); // close statement
        } catch (Exception e) {}
    }
    
    /*---------------------------------------------------------------------
    |  Method importFoodCouponData(connection, file)
    |
    |  Purpose:  Imports food coupon data from a CSV file into the FoodCoupon table
    |            in the database.
    |
    |  Pre-condition:  The JDBC database connection is valid and points to
    |                  the target database.
    |
    |  Post-condition: Food coupon data is inserted into the FoodCoupon table if it
    |                  does not already exist.
    |
    |  Parameters:
    |      dbConn -- Connection object representing the database connection.
    |      file -- Name of the CSV file containing food coupon data.
    |
    |  Throws:
    |      SQLException -- If an SQL exception occurs during database operations.
    |
    |  Returns:  None.
    *-------------------------------------------------------------------*/
    private static void importFoodCouponData(Connection dbConn, String file) {
        String tableName = "FoodCoupon"; // initialize string for table name

        try {
            String insert = "INSERT INTO " + tableName + " VALUES (?, ?, ?, ?)"; // insert statement for foodCoupon table
            PreparedStatement statement = dbConn.prepareStatement(insert); // prepare insert statement
            BufferedReader reader = new BufferedReader(new FileReader(file)); // initialize reader for csv file
            String line = null; // initialize line variable
            // iterate through each line of csv file
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(","); // split data
                // if foodCouponID doesn't exist within the table
                if (!rowExists(dbConn, tableName, data[0])) {
                    // set all attributes for foodCoupon
                    statement.setInt(1, Integer.valueOf(data[0]));
                    statement.setInt(2, Integer.valueOf(data[1]));
                    statement.setString(3, data[2]);
                    statement.setBoolean(4, Boolean.valueOf(data[3]));

                    // execute update
                    statement.executeUpdate();
                // foodCouponID already exists within table
                } else {
                    System.out.println("Skipping duplicate row");
                }
            }

            System.out.println("Successful import of " + file + " into table " + tableName + ".");
            reader.close(); // close reader
            statement.close(); // close statement
        } catch (Exception e) {}
    }

    /*---------------------------------------------------------------------
    |  Method importMembershipTierData(connection, file)
    |
    |  Purpose:  Imports membership tier data from a CSV file into the MembershipTier table
    |            in the database.
    |
    |  Pre-condition:  The JDBC database connection is valid and points to
    |                  the target database.
    |
    |  Post-condition: Membership tier data is inserted into the MembershipTier table if it
    |                  does not already exist.
    |
    |  Parameters:
    |      dbConn -- Connection object representing the database connection.
    |      file -- Name of the CSV file containing membership tier data.
    |
    |  Throws:
    |      SQLException -- If an SQL exception occurs during database operations.
    |
    |  Returns:  None.
    *-------------------------------------------------------------------*/
    private static void importMembershipTierData(Connection dbConn, String file) {
        String tableName = "MembershipTier"; // initialize string for table name

        try {
            String insert = "INSERT INTO " + tableName + " VALUES (?, ?, ?, ?, ?)"; // insert statement for membershipTier table
            PreparedStatement statement = dbConn.prepareStatement(insert); // prepare statement for insert statement
            BufferedReader reader = new BufferedReader(new FileReader(file)); // initialize reader for csv file
            String line = null; // initialize line variable
            // iterate through every line in csv file
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(","); // split data
                // if membershipTierID doesn't exist within table
                if (!rowExists(dbConn, tableName, data[0])) {
                    // set all attributes for membershipTier
                    statement.setInt(1, Integer.valueOf(data[0]));
                    statement.setString(2, data[1]);
                    statement.setDouble(3, Double.valueOf(data[2]));
                    statement.setDouble(4, Double.valueOf(data[3]));
                    statement.setInt(5, Integer.valueOf(data[4]));

                    // execute update
                    statement.executeUpdate();
                // membershiptierID already exists within table
                } else {
                    System.out.println("Skipping duplicate row");
                }
            }

            System.out.println("Successful import of " + file + " into table " + tableName + ".");
            reader.close(); // close reader 
            statement.close(); // closer statement
        } catch (Exception e) {}
    }

    /*---------------------------------------------------------------------
    |  Method importTransactionData(connection, file)
    |
    |  Purpose:  Imports transaction data from a CSV file into the Transaction table
    |            in the database.
    |
    |  Pre-condition:  The JDBC database connection is valid and points to
    |                  the target database.
    |
    |  Post-condition: Transaction data is inserted into the Transaction table if it
    |                  does not already exist.
    |
    |  Parameters:
    |      dbConn -- Connection object representing the database connection.
    |      file -- Name of the CSV file containing transaction data.
    |
    |  Throws:
    |      SQLException -- If an SQL exception occurs during database operations.
    |
    |  Returns:  None.
    *-------------------------------------------------------------------*/
    private static void importTransactionData(Connection dbConn, String file) {
        String tableName = "Transaction"; // initializing string for table name

        try {
            String insert = "INSERT INTO " + tableName + " VALUES (?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'))"; // insert statement for transaction table
            PreparedStatement statement = dbConn.prepareStatement(insert); // prepare insert statement
            BufferedReader reader = new BufferedReader(new FileReader(file)); // initialize reader for csv file
            String line = null; // initialize line variable
            // iterate through each line of csv file
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(","); // split data
                // if transactionID doesn't exist within the table
                if (!rowExists(dbConn, tableName, data[0])) {
                    // set all attributes for transaction
                    statement.setInt(1, Integer.valueOf(data[0]));
                    statement.setString(2, data[1]);
                    statement.setDouble(3, Double.valueOf(data[2]));
                    statement.setDate(4, java.sql.Date.valueOf(data[3]));

                    // execute update
                    statement.executeUpdate();
                // transactionID already exists within table
                } else {
                    System.out.println("Skipping duplicate row");
                }
            }

            System.out.println("Successful import of " + file + " into table " + tableName + ".");
            reader.close(); // close reader
            statement.close(); // close statement
        } catch (Exception e) {}
    }

    /*---------------------------------------------------------------------
    |  Method rowExists(connection, tableName, id)
    |
    |  Purpose:  Checks whether a row with the given ID exists in the specified
    |            table of the database.
    |
    |  Pre-condition:  The JDBC database connection is valid and points to
    |                  the target database.
    |
    |  Post-condition: Returns true if the row exists; otherwise, false.
    |
    |  Parameters:
    |      dbConn -- Connection object representing the database connection.
    |      tableName -- Name of the table to search for the row.
    |      id -- The ID of the row to check for existence.
    |
    |  Throws:
    |      SQLException -- If an SQL exception occurs during database operations.
    |
    |  Returns:  boolean -- True if the row exists; otherwise, false.
    *-------------------------------------------------------------------*/
    private static boolean rowExists(Connection dbConn, String tableName, String id) throws SQLException {
        String query = "SELECT COUNT(*) FROM " + tableName + " WHERE " + tableName + "ID = ?"; /* query to check for given id */
        // try catch for checking for row in table
        try (PreparedStatement statement = dbConn.prepareStatement(query)) {
            // setting facility id to facility attribute index
            statement.setInt(1, Integer.valueOf(id));

            // try catch for checking if the row exists
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                // true if exists, false otherwise
                return resultSet.getInt(1) > 0;
            }
        }
    }

    /*---------------------------------------------------------------------
    |  Method promptUpdate(connection)
    |
    |  Purpose:  Prompts the user to decide whether to update the database tables.
    |
    |  Pre-condition:  The JDBC database connection is valid and points to
    |                  the target database.
    |
    |  Post-condition: Allows the user to choose whether to update the tables.
    |
    |  Parameters:
    |      dbConn -- Connection object representing the database connection.
    |
    |  Throws:
    |      SQLException -- If an SQL exception occurs during database operations.
    |
    |  Returns:  None.
    *-------------------------------------------------------------------*/
    private static void promptUpdate(Connection dbConn) throws SQLException {
        Scanner scanner = new Scanner(System.in); // initialize new scanner for user input

        // keep prompting user for input until they enter 'n'
        while (true) {
            System.out.println("\nWould you like to update the tables? (y/n)");
            String answer = scanner.nextLine(); // get user input

            switch (answer.toLowerCase()) {
                // update tables
                case "y":
                    update(dbConn);
                    break;

                // don't update tables
                case "n":
                    return;

                // invalid input
                default:
                    System.out.println("\nPlease choose a valid answer (y/n)");
            }
        }
    }

    /*---------------------------------------------------------------------
    |  Method update(connection)
    |
    |  Purpose:  Allows the user to choose which table to update (Member, Game, or Prize)
    |            and calls the respective update method.
    |
    |  Pre-condition:  The JDBC database connection is valid and points to
    |                  the target database.
    |
    |  Post-condition: Updates the chosen table in the database.
    |
    |  Parameters:
    |      dbConn -- Connection object representing the database connection.
    |
    |  Throws:
    |      SQLException -- If an SQL exception occurs during database operations.
    |
    |  Returns:  None.
    *-------------------------------------------------------------------*/
    private static void update(Connection dbConn) throws SQLException {
        Scanner scanner = new Scanner(System.in); // initialize new scanner for user input
        System.out.println("Which table would you like to update? (Member/Game/Prize)");
        String answer = scanner.nextLine(); // get user input

        switch (answer.toLowerCase()) {
            // update member table
            case "member":
                updateMember(dbConn);
                break;

            // update game table
            case "game":
                updateGame(dbConn);
                break;

            // update prize table
            case "prize":
                updatePrize(dbConn);
                break;

            // invalid input
            default:
                System.out.println("\nPlease choose a valid table (Member/Game/Prize)");
        }
    }

    /*---------------------------------------------------------------------
    |  Method updateMember(connection)
    |
    |  Purpose:  Allows the user to choose how to update the Member table
    |            (Add, Update, or Delete) and calls the respective method.
    |
    |  Pre-condition:  The JDBC database connection is valid and points to
    |                  the target database.
    |
    |  Post-condition: Updates the Member table according to the user's choice.
    |
    |  Parameters:
    |      dbConn -- Connection object representing the database connection.
    |
    |  Throws:
    |      SQLException -- If an SQL exception occurs during database operations.
    |
    |  Returns:  None.
    *-------------------------------------------------------------------*/
    private static void updateMember(Connection dbConn) throws SQLException {
        Scanner scanner = new Scanner(System.in); // initialize new scanner for user input
        System.out.println("How would you like to update the Member table? (Add/Update/Delete)"); 
        String answer = scanner.nextLine(); // ger user input

        switch (answer.toLowerCase()) {
            // add member
            case "add":
                addMember(dbConn);
                break;

            // edit member
            case "update":
                editMember(dbConn);
                break;

            // delete member
            case "delete":
                deleteMember(dbConn);
                break;

            // invalid input
            default:
                System.out.println("\nPlease choose a valid option (Add/Update/Delete)");
                break;
        }
    }

    /*---------------------------------------------------------------------
    |  Method addMember(connection)
    |
    |  Purpose:  Adds a new member to the Member table in the database.
    |
    |  Pre-condition:  The JDBC database connection is valid and points to
    |                  the target database.
    |
    |  Post-condition: Inserts a new row into the Member table with the
    |                  provided member details.
    |
    |  Parameters:
    |      dbConn -- Connection object representing the database connection.
    |
    |  Throws:
    |      SQLException -- If an SQL exception occurs during database operations.
    |
    |  Returns:  None.
    *-------------------------------------------------------------------*/
    private static void addMember(Connection dbConn) {
        int newMemberID = getLastMemberID(dbConn) + 1; // get new member id to add
        Scanner scanner = new Scanner(System.in); // initialize scanner for user input

        System.out.println("Enter the new member's first name:");
        String fName = scanner.nextLine(); // ger first name

        System.out.println("Enter the new member's last name:");
        String lName = scanner.nextLine(); // get last name

        System.out.println("Enter the new member's phone number:");
        String phoneNum = scanner.nextLine(); // get phone number

        System.out.println("Enter the new member's address:");
        String address = scanner.nextLine(); // get address

        String insert = "INSERT INTO Member VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'), ?)"; // insert statement for member table
        try {
            PreparedStatement statement = dbConn.prepareStatement(insert); // prepare insert statement

            // memberID doesn't exist within table
            if (!rowExists(dbConn, "Member", String.valueOf(newMemberID))) {
                // set all attributes of member
                statement.setInt(1, newMemberID);
                statement.setString(2, fName);
                statement.setString(3, lName);
                statement.setString(4, phoneNum);
                statement.setString(5, address);
                statement.setInt(6, 0);
                statement.setDouble(7, 0.00);
                statement.setString(8, " ");
                statement.setInt(9, 1);
                statement.setDate(10, java.sql.Date.valueOf("2024-04-29"));
                statement.setInt(11, 0);

                // execute update
                statement.executeUpdate();
                System.out.println("\nSuccessfully added new member " + fName + " " + lName);
            // memberID already exists within table
            } else {
                System.out.println("\nSkipping duplicate row");
            }
        } catch (SQLException e) {
            System.err.println("\nCould not add new member.");
            e.printStackTrace();
        }
    }

    /*---------------------------------------------------------------------
    |  Method editMember(connection)
    |
    |  Purpose:  Allows the user to edit the details of an existing member
    |            in the Member table of the database.
    |
    |  Pre-condition:  The JDBC database connection is valid and points to
    |                  the target database.
    |
    |  Post-condition: Updates the details of the specified member in the
    |                  Member table according to the user's input.
    |
    |  Parameters:
    |      dbConn -- Connection object representing the database connection.
    |
    |  Throws:
    |      SQLException -- If an SQL exception occurs during database operations.
    |
    |  Returns:  None.
    *-------------------------------------------------------------------*/
    private static void editMember(Connection dbConn) throws SQLException {
        Scanner scanner = new Scanner(System.in); // initialize new scanner for user input

        System.out.println("Enter the ID of the member you want to update:");
        int memberId = scanner.nextInt(); // get memberID
        scanner.nextLine();

        // if member doesn't exist within table
        if (!memberExists(dbConn, memberId)) {
            System.out.println("Member with ID " + memberId + " does not exist.");
            return;
        }

        System.out.println("What would you like to update for this member? (Phone number/Address/Both)");
        String toChange = scanner.nextLine(); // get user input

        String newPhoneNumber = null; // initialize new phone number
        String newAddress = null; // initialize new address

        switch (toChange.toLowerCase()) {
            // changing phone number
            case "phone number":
                System.out.println("Enter the new phone number (###-###-####):");
                newPhoneNumber = scanner.nextLine(); // get new phone number
                break;

            // changing address
            case "address":
                System.out.println("Enter the new address:");
                newAddress = scanner.nextLine(); // get new address
                break;

            // changing both phone number and address
            case "both":
                System.out.println("Enter the new phone number (###-###-####):");
                newPhoneNumber = scanner.nextLine(); // get new phone number
                System.out.println("Enter the new address:");
                newAddress = scanner.nextLine(); // get new address
                break;

            // invalid input
            default:
                System.out.println("\nPlease choose a valid option (Phone number/Address/Both)");
        }

        String update = "UPDATE Member SET "; // initialize update statement
        // phone number needs to be updated
        if (newPhoneNumber != null) {
            update += "TelephoneNum = '" + newPhoneNumber + "'";
        }

        // address needs to be updates
        if (newAddress != null) {
            if (newPhoneNumber != null) {
                update += ", ";
            }

            update += "Address = '" + newAddress + "'";
        }

        // both phone number and address need to be updated
        if (newPhoneNumber != null || newAddress != null) {
            update += " WHERE MemberID = ?";
        // no updates to be made
        } else {
            // If no updates are being made, just return
            System.out.println("No updates to perform.");
            return;
        }

        try {
            PreparedStatement statement = dbConn.prepareStatement(update); // prepare update statement
            statement.setInt(1, memberId); // set memberID
            int updatedRow = statement.executeUpdate(); // execute update
            System.out.println("\n" + updatedRow + " row(s) updated successfully.");
        } catch (SQLException e) {
            System.out.println("\nCould not update row(s).");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /*---------------------------------------------------------------------
    |  Method memberExists(connection, memberId)
    |
    |  Purpose:  Checks whether a member with the given ID exists in the
    |            Member table of the database.
    |
    |  Pre-condition:  The JDBC database connection is valid and points to
    |                  the target database.
    |
    |  Post-condition: Returns true if the member exists; otherwise, false.
    |
    |  Parameters:
    |      dbConn -- Connection object representing the database connection.
    |      memberId -- The ID of the member to check for existence.
    |
    |  Throws:
    |      SQLException -- If an SQL exception occurs during database operations.
    |
    |  Returns:  boolean -- True if the member exists; otherwise, false.
    *-------------------------------------------------------------------*/
    private static boolean memberExists(Connection dbConn, int memberId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Member WHERE MemberID = ?"; // initialize select statement
        try {
            PreparedStatement statement = dbConn.prepareStatement(sql); // prepare select statement
            statement.setInt(1, memberId); // set memberID
            try (var resultSet = statement.executeQuery()) {
                // member exists
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        } catch (SQLException e) {}

        return false;
    }

    /*---------------------------------------------------------------------
    |  Method deleteMember(connection)
    |
    |  Purpose:  Deletes a member from the Member table in the database
    |            based on the provided Member ID.
    |
    |  Pre-condition:  Connection to the database is established.
    |
    |  Post-condition: The specified member is deleted from the Member table
    |                  if it exists.
    |
    |  Parameters:
    |      connection -- Connection object representing the database connection.
    |
    |  Returns:  None.
    *-------------------------------------------------------------------*/
    private static void deleteMember(Connection dbConn) throws SQLException {
        Scanner scanner = new Scanner(System.in); // initiialize scanner for user input
    
        System.out.println("Enter the Member ID of the member you want to delete:");
        int memberId = scanner.nextInt(); // get memberID
        scanner.nextLine(); // consume newline character

        // member doesn't exist within database
        if (!memberExists(dbConn, memberId)) {
            System.out.println("\nThere is no member in the database with the MemberID " + memberId);
            return;
        }
    
        try {
            PreparedStatement ticketCheckStatement = dbConn.prepareStatement("SELECT TotalTickets FROM Member WHERE MemberID = ?"); // prepare select statement
            ticketCheckStatement.setInt(1, memberId); // set memberID
            ResultSet tickets = ticketCheckStatement.executeQuery(); // execute query
            tickets.next();
            int ticketCount = tickets.getInt(1); // get ticket count

            // tickets available
            if (ticketCount > 0) {
                System.out.println("\nThere are still " + ticketCount + " remaining tickets. Exchange them for a prize? (y/n/e)");
                String exchangeChoice = scanner.nextLine().trim(); // get exchange choice

                // user wants to exchange tickets
                if (exchangeChoice.equalsIgnoreCase("y")) {
                    exchangeTickets(dbConn, memberId, ticketCount);
                // user doesn't want to exchange tickers
                } else if (exchangeChoice.equalsIgnoreCase("n")) {
                    System.out.println("\nTickets will not be exchanged for a prize.");
                // user wants to exit
                } else if (exchangeChoice.equalsIgnoreCase("e")) {
                    return;
                // invalid input
                } else {
                    System.out.println("Invalid choice. Please enter 'y' for yes or 'n' for no or 'e' to exit.");
                }
            }

            redeemCoupon(dbConn, memberId); // redeeming food coupons

            PreparedStatement gameplayCheckStatement = dbConn.prepareStatement(
                "SELECT COUNT(*) FROM Gameplay WHERE MemberID = ?"); // prepare select statement
            gameplayCheckStatement.setInt(1, memberId); // set memberID
            ResultSet childResultSet = gameplayCheckStatement.executeQuery(); // get rows
            childResultSet.next();
            int gameplayCount = childResultSet.getInt(1); // get count of rows

            // if there were rows returned
            if (gameplayCount > 0) {
                System.out.println("This member has associated records in Gameplay table. Deleting Gameplay records.");
                // delete associated records in the Gameplay table
                PreparedStatement deleteGameplayStatement = dbConn.prepareStatement(
                        "DELETE FROM Gameplay WHERE MemberID = ?");
                deleteGameplayStatement.setInt(1, memberId);
                deleteGameplayStatement.executeUpdate();
            }

            PreparedStatement foodCouponCheckStatement = dbConn.prepareStatement(
                "SELECT COUNT(*) FROM FoodCoupon WHERE MemberID = ?"); // prepare select statement
            foodCouponCheckStatement.setInt(1, memberId); // set memberID
            ResultSet foodCouponResultSet = foodCouponCheckStatement.executeQuery(); // get rows
            foodCouponResultSet.next();
            int foodCouponCount = foodCouponResultSet.getInt(1); // get count of rows

            // if there were rows returned
            if (foodCouponCount > 0) {
                System.out.println("This member has associated records in FoodCoupon table. Deleting FoodCoupon records.");
                // delete associated records in the FoodCoupon table
                PreparedStatement deleteFoodCouponStatement = dbConn.prepareStatement(
                        "DELETE FROM FoodCoupon WHERE MemberID = ?");
                deleteFoodCouponStatement.setInt(1, memberId);
                deleteFoodCouponStatement.executeUpdate();
            }

            PreparedStatement preparedStatement = dbConn.prepareStatement("DELETE FROM Member WHERE MemberID = ?"); // prepare select statement
            preparedStatement.setInt(1, memberId); // set memberID
            int rowsAffected = preparedStatement.executeUpdate(); // get rows

            // if there were rows returned
            if (rowsAffected > 0) {
                System.out.println("\nMember with ID " + memberId + " deleted successfully along with associated records.");
            } else {
                System.out.println("\nNo member found with ID " + memberId + ".");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
     * Function: exchangeTickets()
     * Arugments: Connection dbConn - The JDBC data connection.
     *            int memberId - The ID of the member we want to delete.
     *            int ticketCOunt - The number of tickets the member has
     * Returns: None.
     * 
     * Pre-Condition: The JDBC database connection is valid.
     * 
     * Post-condition: None.
     * 
     * Puprose: This function is responsible for prompting the user to exchange tickets for prize(s).
     */
    private static void exchangeTickets(Connection dbConn, int memberId, int ticketCount) {
        Scanner scanner = new Scanner(System.in); // intitialize new scanner
        int currTickets = ticketCount; // get current number of tickets

        try {
            // while member still has tickets
            while (currTickets > 0) {
                System.out.println("There are still " + currTickets + " tickets left.");
                Statement statement = dbConn.createStatement(); // creating new statement

                String query = "SELECT Prize.Name, Prize.TicketCost FROM Prize WHERE Prize.TicketCost <= " + currTickets; // select query for prizes

                ResultSet tables = statement.executeQuery(query); // get rows from query
                List<String> availablePrizes = new ArrayList<>(); // initialize list for available prizes
                Map<String, Integer> prizeCost = new HashMap<>(); // initialize map for prize cost of each prize
                System.out.println("\nAvailable prizes: ");
                System.out.println("-----------------------------------------------");
                // iterate through valid rows
                while (tables.next()) {
                    String prizeName = tables.getString("Name"); // get prize name
                    int ticketCost = tables.getInt("TicketCost"); // get prize cost

                    System.out.println("Prize: " + prizeName + ", Ticket cost: " + ticketCost);
                    availablePrizes.add(prizeName.toLowerCase()); // add prize to available prizes
                    prizeCost.put(prizeName.toLowerCase(), ticketCost); // add prize and prize cost to prizeCost
                }

                System.out.println("-----------------------------------------------");

                // not enough tickets to redeem any prizes
                if (availablePrizes.isEmpty()) {
                    System.out.println("You don't have enough tickets to redeem any prizes.");
                    break;
                } else {
                    System.out.println("\nWhich prize would you like to redeem?");
                    String prize = scanner.nextLine().trim(); // get prize choice from user

                    // if the prize choice is a valid choice
                    if (availablePrizes.contains(prize.toLowerCase())) {
                        int cost = prizeCost.get(prize.toLowerCase()); // get cost of prize
                        System.out.println("\nYou redeemed: " + prize);
                        currTickets -= cost; // decrement currTickets by cost of prize
                    // exit
                    } else if (prize.toLowerCase() == "e") {
                        return;
                    // invalid choice
                    } else {
                        System.out.println("Invalid prize selection. Please choose from the available prizes.");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in redeeming prize.");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /*
     * Function: redeemCoupon()
     * Arguments: Connection dbConn - The JDBC database connection.
     *            int memberId - The ID of the member we want to delete.
     * Returns: None.
     * 
     * Pre-condition: The JDBC database connection valid and the member exists.
     * 
     * Post-condition: None.
     * 
     * Purpose: This function is responsible for prompting the user to redeem their food coupons.
     */
    private static void redeemCoupon(Connection dbConn, int memberId) {
        Scanner scanner = new Scanner(System.in); // set new scanner

        try {
            while (true) {
                // Retrieve unredeemed food coupons for the member
                PreparedStatement selectStatement = dbConn.prepareStatement(
                        "SELECT FoodCouponID, RedeemedFood FROM FoodCoupon WHERE MemberID = ? AND Used != 1"); // get all unused food coupon of the member
                selectStatement.setInt(1, memberId); // set memberId
                ResultSet resultSet = selectStatement.executeQuery(); // execute query
    
                // go through all rows returned from query
                if (resultSet.next()) {
                    int foodCouponID = resultSet.getInt("FoodCouponID"); // get foodCouponID
                    String redeemedFood = resultSet.getString("RedeemedFood"); // get redeemed food
    
                    System.out.println("There is an unredeemed food coupon for " + redeemedFood + ". Redeem it? (y/n/e)");
                    String redeemChoice = scanner.nextLine().trim(); // get users redeem choice
    
                    // user wants to redeem food coupon
                    if (redeemChoice.equalsIgnoreCase("y")) {
                        // update the food coupon to mark it as used
                        PreparedStatement updateStatement = dbConn.prepareStatement(
                                "UPDATE FoodCoupon SET Used = 1 WHERE FoodCouponID = ?");
                        updateStatement.setInt(1, foodCouponID);
                        updateStatement.executeUpdate();
    
                        System.out.println("\nYou redeemed a food coupon for " + redeemedFood + ".");
                    // user doesn't want to redeem food coupon
                    } else if (redeemChoice.equalsIgnoreCase("n")) {
                        // update food coupon to mark as used anyways for while loop execution
                        PreparedStatement updateStatement = dbConn.prepareStatement(
                                "UPDATE FoodCoupon SET Used = 1 WHERE FoodCouponID = ?");
                        updateStatement.setInt(1, foodCouponID);
                        updateStatement.executeUpdate();
                        System.out.println("\nFood coupon for " + redeemedFood + " will not be redeemed.");
                    // exiy
                    } else if (redeemChoice.equalsIgnoreCase("e")) {
                        break;
                    // invalid choice
                    } else {
                        System.out.println("Invalid choice. Please enter 'y' for yes or 'n' for no or 'e' to exit.");
                    }
                // no unredeemed food coupons
                } else {
                    System.out.println("\nNo unredeemed food coupons found for this member.");
                    break;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error redeeming food coupons.");
            e.printStackTrace();
        }
    }

    /*---------------------------------------------------------------------
    |  Method updateGame(connection)
    |
    |  Purpose:  Allows the user to choose how to update the Game table
    |            (Add or Delete) and calls the respective method.
    |
    |  Pre-condition:  The JDBC database connection is valid and points to
    |                  the target database.
    |
    |  Post-condition: Updates the Game table according to the user's choice.
    |
    |  Parameters:
    |      dbConn -- Connection object representing the database connection.
    |
    |  Returns:  None.
    *-------------------------------------------------------------------*/
    private static void updateGame(Connection dbConn) {
        Scanner scanner = new Scanner(System.in);
    
        // Prompt user for the action they want to perform
        System.out.println("How would you like to update the Game table? (Add/Delete)");
        String answer = scanner.nextLine();
    
        // Convert the user's input to lowercase for case-insensitive comparison
        switch (answer.toLowerCase()) {
            // If user wants to add a game
            case "add":
                // Call the method to add a game
                addGame(dbConn);
                break;
    
            // If user wants to delete a game
            case "delete":
                // Call the method to delete a game
                deleteGame(dbConn);
                break;
    
            // If user input is not recognized
            default:
                System.out.println("\nPlease choose a valid action (Add/Delete)");
        }
    }

    /*---------------------------------------------------------------------
    |  Method deleteGame(Connection dbConn)
    |
    |  Purpose:  Allows the user to delete an existing game entry from the
    |            Game table in the database. The method prompts the user to
    |            input the game ID, executes an SQL DELETE statement, and
    |            prints a success message if the operation is successful.
    |
    |  Pre-condition:  Connection to the database is established.
    |
    |  Post-condition: The specified game entry is deleted from the Game
    |                  table in the database if the operation is successful.
    |
    |  Parameters:
    |      dbConn -- Connection object representing the database connection.
    |
    |  Returns:  None.
    *-------------------------------------------------------------------*/
    private static void deleteGame(Connection dbConn) {
        Scanner scanner = new Scanner(System.in);

        // Prompt the user to enter the ID of the game to delete
        System.out.println("Enter the ID of the game you want to delete:");
        int gameID = scanner.nextInt();
        scanner.nextLine(); 

        try (Statement statement = dbConn.createStatement()) {
            // Delete gameplay records associated with the game
            String deleteGameplayQuery = "DELETE FROM Gameplay WHERE GameID = " + gameID;
            int gameplayDeleted = statement.executeUpdate(deleteGameplayQuery);

            // Delete the game entry itself
            String deleteGameQuery = "DELETE FROM Game WHERE GameID = " + gameID;
            int gameDeleted = statement.executeUpdate(deleteGameQuery);

            // Check if the game was deleted successfully
            if (gameDeleted > 0) {
                // Print success message along with the number of related gameplay records deleted
                System.out.println("Game with ID " + gameID + " deleted successfully.");
                System.out.println("Related gameplay records deleted: " + gameplayDeleted);
            } else {
                // Print message if no game was found with the provided ID
                System.out.println("No game found with ID " + gameID);
            }
        } catch (SQLException e) {
            // Print any SQL exceptions that occur during deletion
            e.printStackTrace();
        }
    }

    /*---------------------------------------------------------------------
    |  Method addGame(Connection dbConn)
    |
    |  Purpose:  Allows the user to add a new game entry to the Game table
    |            in the database. The method prompts the user to input the
    |            name, token cost, and tickets earned for the new game,
    |            executes an SQL INSERT statement, and prints a success
    |            message if the operation is successful.
    |
    |  Pre-condition:  Connection to the database is established.
    |
    |  Post-condition: A new game entry is added to the Game table in the
    |                  database if the operation is successful.
    |
    |  Parameters:
    |      dbConn -- Connection object representing the database connection.
    |
    |  Returns:  None.
    *-------------------------------------------------------------------*/
    private static void addGame(Connection dbConn) {
        // Get the ID for the new game by incrementing the last game ID
        int newGameID = getLastGameID(dbConn) + 1;
        Scanner scanner = new Scanner(System.in);
    
        // Prompt the user to enter the name of the new game
        System.out.println("Enter the name of the new game:");
        String name = scanner.nextLine();
    
        // Prompt the user to enter the token cost of the new game
        System.out.println("Enter the token cost of the new game:");
        int tokenCost = scanner.nextInt();
        scanner.nextLine(); // Consume newline character
    
        // Prompt the user to enter the number of tickets earned by playing the new game
        System.out.println("Enter the number of tickets earned by playing the new game:");
        int tickets = scanner.nextInt();
        scanner.nextLine(); // Consume newline character
    
        try (Statement statement = dbConn.createStatement()) {
            // Construct the SQL INSERT query using the provided information
            String query = String.format("INSERT INTO Game (GameID, Name, TokenCost, Tickets) VALUES (%d, '%s', %d, %d)", newGameID, name, tokenCost, tickets);
            // Execute the SQL INSERT statement
            int rowsAffected = statement.executeUpdate(query);
            // Check if the insertion was successful and print appropriate message
            if (rowsAffected > 0) {
                System.out.println("New game added successfully.");
            } else {
                System.out.println("Failed to add new game.");
            }
        } catch (SQLException e) {
            // Print any SQL exceptions that occur during insertion
            e.printStackTrace();
        }
    }

    /*---------------------------------------------------------------------
    |  Method updatePrize(connection)
    |
    |  Purpose:  Provides options to add, update, or delete prize information
    |            in the Prize table of the database.
    |
    |  Pre-condition:  Connection to the database is established.
    |
    |  Post-condition: Prize information is added, updated, or deleted in the
    |                  Prize table based on user input.
    |
    |  Parameters:
    |      connection -- Connection object representing the database connection.
    |
    |  Returns:  None.
    *-------------------------------------------------------------------*/
    private static void updatePrize(Connection dbConn) {
        // Create a scanner object to read user input
        Scanner scanner = new Scanner(System.in);
        
        // Prompt the user to specify the action they want to perform on the Prize table
        System.out.println("How would you like to update the Prize table? (Add/Delete)");
        String answer = scanner.nextLine();
    
        // Switch statement to handle different actions based on user input
        switch (answer.toLowerCase()) {
            // If the user chooses to add a prize
            case "add":
                // Call the addPrize method to add a new prize
                addPrize(dbConn);
                break;
    
            // If the user chooses to delete a prize
            case "delete":
                // Call the deletePrize method to delete an existing prize
                deletePrize(dbConn);
                break;
    
            // If the user inputs an invalid action
            default:
                // Prompt the user to choose a valid action
                System.out.println("\nPlease choose a valid action (Add/Delete)");
        }
    }

    /*---------------------------------------------------------------------
    |  Method deletePrize(Connection dbConn)
    |
    |  Purpose:  Allows the user to delete an existing prize entry from the
    |            Prize table in the database. The method prompts the user to
    |            input the prize ID, executes an SQL DELETE statement, and
    |            prints a success message if the operation is successful.
    |
    |  Pre-condition:  Connection to the database is established.
    |
    |  Post-condition: The specified prize entry is deleted from the Prize
    |                  table in the database if the operation is successful.
    |
    |  Parameters:
    |      dbConn -- Connection object representing the database connection.
    |
    |  Returns:  None.
    *-------------------------------------------------------------------*/
    private static void deletePrize(Connection dbConn) {
        // Create a scanner object to read user input
        Scanner scanner = new Scanner(System.in);
        
        // Prompt the user to enter the ID of the prize they want to delete
        System.out.println("Enter the ID of the prize you want to delete:");
        int prizeID = scanner.nextInt();
    
        // Prompt the user to enter the ID of the member who wants to redeem the prize
        System.out.println("Enter the ID of the member who wants to redeem this prize:");
        int memberID = scanner.nextInt();
    
        try (Statement statement = dbConn.createStatement()) {
            // Check if the prize exists in the Prize table
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Prize WHERE PrizeID = " + prizeID);
            if (resultSet.next()) {
                // Get the ticket cost of the prize
                int ticketCost = resultSet.getInt("TicketCost");
    
                // Check if the member exists and has enough tickets to redeem the prize
                ResultSet memberResultSet = statement.executeQuery("SELECT TotalTickets FROM Member WHERE MemberID = " + memberID);
                if (memberResultSet.next()) {
                    int totalTickets = memberResultSet.getInt("TotalTickets");
                    // If the member has enough tickets to redeem the prize
                    if (totalTickets >= ticketCost) {
                        // Update the member's total tickets after redeeming the prize
                        int newTotalTickets = totalTickets - ticketCost;
                        String updateQuery = "UPDATE Member SET TotalTickets = " + newTotalTickets + " WHERE MemberID = " + memberID;
                        statement.executeUpdate(updateQuery);
    
                        // Delete the prize from the Prize table
                        String deleteQuery = "DELETE FROM Prize WHERE PrizeID = " + prizeID;
                        int rowsAffected = statement.executeUpdate(deleteQuery);
                        if (rowsAffected > 0) {
                            System.out.println("Prize with ID " + prizeID + " deleted successfully.");
                        } else {
                            System.out.println("No prize found with ID " + prizeID);
                        }
                    } else {
                        System.out.println("Member with ID " + memberID + " does not have enough tickets to redeem this prize.");
                    }
                } else {
                    System.out.println("Member with ID " + memberID + " does not exist.");
                }
            } else {
                System.out.println("No prize found with ID " + prizeID);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*---------------------------------------------------------------------
    |  Method addPrize(Connection dbConn)
    |
    |  Purpose:  Allows the user to add a new prize entry to the Prize table
    |            in the database. The method prompts the user to input the
    |            name and ticket cost for the new prize, executes an SQL INSERT
    |            statement, and prints a success message if the operation is
    |            successful.
    |
    |  Pre-condition:  Connection to the database is established.
    |
    |  Post-condition: A new prize entry is added to the Prize table in the
    |                  database if the operation is successful.
    |
    |  Parameters:
    |      dbConn -- Connection object representing the database connection.
    |
    |  Returns:  None.
    *-------------------------------------------------------------------*/
    private static void addPrize(Connection dbConn) {
        // Get the ID for the new prize by incrementing the last prize ID in the database
        int newPrizeID = getLastPrizeID(dbConn) + 1;
        
        // Create a scanner object to read user input
        Scanner scanner = new Scanner(System.in);
        
        // Prompt the user to enter the name of the new prize
        System.out.println("Enter the name of the new prize:");
        String newName = scanner.nextLine();
        
        // Prompt the user to enter the ticket cost for the new prize
        System.out.println("Enter the ticket cost for the new prize:");
        int newTicketCost = scanner.nextInt();
        
        try (PreparedStatement preparedStatement = dbConn.prepareStatement("INSERT INTO Prize (PrizeID, Name, TicketCost) VALUES (?, ?, ?)")) {
            // Set the parameters for the prepared statement
            preparedStatement.setInt(1, newPrizeID);
            preparedStatement.setString(2, newName);
            preparedStatement.setInt(3, newTicketCost);
            
            // Execute the prepared statement and get the number of rows affected
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("New prize added successfully.");
            } else {
                System.out.println("Failed to add new prize.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*---------------------------------------------------------------------
    |  Method getLastMemberID(connection)
    |
    |  Purpose:  Retrieves the last MemberID from the Member table in the
    |            database.
    |
    |  Pre-condition:  The JDBC database connection is valid and points to
    |                  the target database.
    |
    |  Post-condition: Returns the last MemberID from the Member table.
    |
    |  Parameters:
    |      dbConn -- Connection object representing the database connection.
    |
    |  Returns:  int -- The last MemberID from the Member table.
    *-------------------------------------------------------------------*/
    private static int getLastMemberID(Connection dbConn) {
        try {
            Statement statement = dbConn.createStatement(); // create statement
            ResultSet tables = statement.executeQuery("SELECT MAX(MemberID) AS LastMemberID FROM Member"); // execute select statement

            // if row was returned
            if (tables.next()) {
                int maxMemberID = tables.getInt("LastMemberID"); // getting last memberID
                tables.close(); // close tables
                statement.close(); // close statement
                return maxMemberID; // return last ID
            // no row returned
            } else {
                tables.close(); // close tables
                statement.close(); // close statement
                return 0; // return default ID
            }
        } catch (SQLException e) {
            System.err.println("Couldn't get last member ID.");
            e.printStackTrace();
            return 0;
        }
    }

    /*---------------------------------------------------------------------
    |  Method getLastGameID(Connection dbConn)
    |
    |  Purpose:  Retrieves the maximum GameID from the Game table in the
    |            database, allowing the insertion of a new game with an
    |            incremented ID. Returns the last GameID value if it exists
    |            or 0 if no game records are found in the database.
    |
    |  Pre-condition:  Connection to the database is established.
    |
    |  Post-condition: Returns the last GameID value from the Game table
    |                  or 0 if no records are found.
    |
    |  Parameters:
    |      dbConn -- Connection object representing the database connection.
    |
    |  Returns:  The last GameID value from the Game table or 0 if no records
    |            are found.
    *-------------------------------------------------------------------*/
    private static int getLastGameID(Connection dbConn) {
        try {
            // Create a statement object to execute SQL queries
            Statement statement = dbConn.createStatement();
            
            // Execute a query to get the maximum GameID from the Game table
            ResultSet resultSet = statement.executeQuery("SELECT MAX(GameID) AS LastGameID FROM Game");
            
            // Check if the query returned any results
            if (resultSet.next()) {
                // Get the maximum GameID from the result set
                int maxGameID = resultSet.getInt("LastGameID");
                
                // Close the result set and statement to release resources
                resultSet.close();
                statement.close();
                
                // Return the maximum GameID
                return maxGameID;
            } else {
                // If no result is found, close the result set and statement and return 0
                resultSet.close();
                statement.close();
                return 0;
            }
        } catch (SQLException e) {
            // Print an error message if an SQL exception occurs
            System.err.println("Couldn't get last game ID.");
            e.printStackTrace();
            return 0;
        }
    }

    /*---------------------------------------------------------------------
    |  Method getLastPrizeID(Connection dbConn)
    |
    |  Purpose:  Retrieves the maximum PrizeID from the Prize table in the
    |            database, allowing the insertion of a new prize with an
    |            incremented ID. Returns the last PrizeID value if it exists
    |            or 0 if no prize records are found in the database.
    |
    |  Pre-condition:  Connection to the database is established.
    |
    |  Post-condition: Returns the last PrizeID value from the Prize table
    |                  or 0 if no records are found.
    |
    |  Parameters:
    |      dbConn -- Connection object representing the database connection.
    |
    |  Returns:  The last PrizeID value from the Prize table or 0 if no records
    |            are found.
    *-------------------------------------------------------------------*/
    private static int getLastPrizeID(Connection dbConn) {
        try {
            // Create a statement object to execute SQL queries
            Statement statement = dbConn.createStatement();
            
            // Execute a query to get the maximum PrizeID from the Prize table
            ResultSet resultSet = statement.executeQuery("SELECT MAX(PrizeID) AS LastPrizeID FROM Prize");
            
            // Check if the query returned any results
            if (resultSet.next()) {
                // Get the maximum PrizeID from the result set
                int maxPrizeID = resultSet.getInt("LastPrizeID");
                
                // Close the result set and statement to release resources
                resultSet.close();
                statement.close();
                
                // Return the maximum PrizeID
                return maxPrizeID;
            } else {
                // If no result is found, close the result set and statement and return 0
                resultSet.close();
                statement.close();
                return 0;
            }
        } catch (SQLException e) {
            // Print an error message if an SQL exception occurs
            System.err.println("Couldn't get last prize ID.");
            e.printStackTrace();
            return 0;
        }
    }

    /*---------------------------------------------------------------------
    |  Method answerQueries(connection)
    |
    |  Purpose:  Continually prompts the user for input on queries and
    |            executes the corresponding query method based on the user's
    |            choice.
    |
    |  Pre-condition:  The JDBC database connection is valid and points to
    |                  the target database.
    |
    |  Post-condition: Executes the specified query method or exits the 
    |                  program based on the user's choice.
    |
    |  Parameters:
    |      dbConn -- Connection object representing the database connection.
    |
    |  Returns:  None.
    *-------------------------------------------------------------------*/
    private static void answerQueries(Connection dbConn) {
        Scanner scanner = new Scanner(System.in);

        // continually prompt user for input on queries
        while (true) {
            // printing out menu of queries to choose from
            System.out.println("\nQueries:");
            System.out.println("(a) List all games in the arcade and the names of the members who " + 
                                "have the current high scores");
            System.out.println("(b) Give the names and membership information of all members who " + 
                                "have spent at least $100 on tokens in the past month");
            System.out.println("(c) For a given member, list all arcade rewards that they can purchase " +  
                                "with their tickets");
            System.out.println("(d) Get total number of tickets for a given gameID"); 
            System.out.println("(u) Update"); 
            System.out.println("(e) Exit\n");

            // prompting input from user
            System.out.println("Enter your query of choice (a, b, c, d, u or e)");
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

                case "u":
                    try {
                        update(dbConn);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    break;
                // exit if user chose e
                case "e":
                    scanner.close();
                    System.out.println("\n***Exiting***");
                    System.exit(0);

                // invalid input
                default:
                    System.out.println("\nPlease choose a valid query (a, b, c, d, u or e)");
            }
        }
    }

    /*---------------------------------------------------------------------
    |  Method queryA(connection)
    |
    |  Purpose:  Retrieves all games in the arcade along with the names of
    |            members who have the current high scores for each game.
    |            The method constructs and executes an SQL query to fetch
    |            this information from the database and prints the results.
    |
    |  Pre-condition:  Connection to the database is established.
    |
    |  Post-condition: List of games and high scorers is displayed.
    |
    |  Parameters:
    |      connection -- Connection object representing the database connection.
    |
    |  Returns:  None.
    *-------------------------------------------------------------------*/
    private static void queryA(Connection dbConn) {
        try (Statement statement = dbConn.createStatement()) {
            // SQL query to select game name, member first name, member last name, and score from the database
            String query = "SELECT Game.Name, Member.Fname, Member.Lname, Gameplay.Score " +
                           "FROM Game, Member, Gameplay " +
                           "WHERE Game.GameID = Gameplay.GameID AND Member.MemberID = Gameplay.MemberID " +
                           "AND Gameplay.Score = (SELECT MAX(Score) FROM Gameplay WHERE Gameplay.GameID = Game.GameID)";
            
            // Execute the query and get the result set
            ResultSet rs = statement.executeQuery(query);
    
            // Print the header for the result
            System.out.println("\nGames in arcade and current high scores:");
            System.out.println("--------------------------------------------");
    
            // Iterate over the result set and print each row
            while (rs.next()) {
                // Print the game name, member first name, member last name, and score
                System.out.println("Game: " + rs.getString("Name") + " High Score: " + rs.getString("Fname") + " " +
                                   rs.getString("Lname") + " with a score of " + rs.getInt("Score"));
            }
            // Print the footer for the result
            System.out.println("--------------------------------------------");
        } catch (SQLException e) {
            // Print any SQL exceptions that occur
            e.printStackTrace();
        }
    }

    /*---------------------------------------------------------------------
    |  Method queryB(connection)
    |
    |  Purpose:  Retrieves the names and membership information of all members
    |            who have spent at least $100 on tokens in the past month.
    |            The method constructs and executes an SQL query to fetch
    |            this information from the database and prints the results.
    |
    |  Pre-condition:  Connection to the database is established.
    |
    |  Post-condition: List of qualifying members and their information is displayed.
    |
    |  Parameters:
    |      connection -- Connection object representing the database connection.
    |
    |  Returns:  None.
    *-------------------------------------------------------------------*/
    private static void queryB(Connection dbConn) {
        try (Statement statement = dbConn.createStatement()) {
            // SQL query to select member information of members who have spent at least $100 on tokens in the past month
            String query = "SELECT MemberID, Fname, Lname, TelephoneNum, Address, GameTokens, TotalSpending, " +
                           "MembershipTier, VisitCount, LastVisitDate, TotalTickets " +
                           "FROM Member " +
                           "WHERE TotalSpending >= 100.00";
    
            // Execute the query and get the result set
            ResultSet resultSet = statement.executeQuery(query);
    
            // Print the header for the result
            System.out.println("\nNames and membership information of all members who have spent at least $100 on tokens in the past month:");
            System.out.println("------------------------------------------------------------");
    
            // Iterate over the result set and print each member's information
            while (resultSet.next()) {
                // Retrieve member information from the result set
                int memberID = resultSet.getInt("MemberID");
                String firstName = resultSet.getString("Fname");
                String lastName = resultSet.getString("Lname");
                String telephoneNum = resultSet.getString("TelephoneNum");
                String address = resultSet.getString("Address");
                int gameTokens = resultSet.getInt("GameTokens");
                double totalSpending = resultSet.getDouble("TotalSpending");
                String membershipTier = resultSet.getString("MembershipTier");
                int visitCount = resultSet.getInt("VisitCount");
                String lastVisitDate = resultSet.getString("LastVisitDate");
                int totalTickets = resultSet.getInt("TotalTickets");
    
                // Print member information
                System.out.println("Member ID: " + memberID);
                System.out.println("Name: " + firstName + " " + lastName);
                System.out.println("Telephone Number: " + telephoneNum);
                System.out.println("Address: " + address);
                System.out.println("Game Tokens: " + gameTokens);
                System.out.println("Total Spending: $" + totalSpending);
                System.out.println("Membership Tier: " + membershipTier);
                System.out.println("Visit Count: " + visitCount);
                System.out.println("Last Visit Date: " + lastVisitDate);
                System.out.println("Total Tickets: " + totalTickets);
                System.out.println("--------------------------------------------");
            }
    
            // Print the footer for the result
            System.out.println("--------------------------------------------");
        } catch (SQLException e) {
            // Print any SQL exceptions that occur
            e.printStackTrace();
        }
    }

    /*---------------------------------------------------------------------
    |  Method queryC(connection)
    |
    |  Purpose:  Retrieves available rewards for a given member ID based
    |            on the total tickets earned by the member.
    |
    |  Pre-condition:  The JDBC database connection is valid and points to
    |                  the target database.
    |
    |  Post-condition: Displays available rewards for the specified member
    |                  ID along with their ticket costs.
    |
    |  Parameters:
    |      dbConn -- Connection object representing the database connection.
    |
    |  Returns:  None.
    *-------------------------------------------------------------------*/
    private static void queryC(Connection dbConn) {
        Scanner scanner = new Scanner(System.in); // initialize scanner for user input
        System.out.println("Enter member ID to view available rewards:");
        int memberId = scanner.nextInt(); // get memberID

        try {
            Statement statement = dbConn.createStatement(); // create statement

            String query = "SELECT Prize.Name, Prize.TicketCost FROM Prize WHERE Prize.TicketCost <= " + 
                            "(SELECT Member.TotalTickets FROM Member WHERE Member.MemberID = " + memberId + ")"; // select statement

            ResultSet tables = statement.executeQuery(query); /// execute select statement
            System.out.println("\nAvailable rewards for member ID " + memberId + ":");
            System.out.println("-----------------------------------------------");
            // iterate through every row returned
            while (tables.next()) {
                String prizeName = tables.getString("Name"); // get prize name
                int ticketCost = tables.getInt("TicketCost"); // ge tticket cost

                System.out.println("Prize: " + prizeName + ", Ticket cost: " + ticketCost); // print result
            }
            System.out.println("-----------------------------------------------");
        } catch (SQLException e) {
            System.err.println("Error in calculating query c.");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /*---------------------------------------------------------------------
    | Method queryD(connection)
    |
    | Purpose: Retrieves the total number of tickets earned by each member
    | for a specific game.
    | The method prompts the user to input the Game ID, then constructs
    | and executes an SQL query to fetch this information from the database
    | and prints the results.
    |
    | Pre-condition: Connection to the database is established.
    |
    | Post-condition: Total tickets earned by each member for the specified game
    | are displayed.
    |
    | Parameters:
    | connection -- Connection object representing the database connection.
    |
    | Returns: None.
    *-------------------------------------------------------------------*/
    private static void queryD(Connection dbConn) {
        Scanner scanner = new Scanner(System.in);
    
        // Prompt the user to enter the Game ID
        System.out.println("Enter Game ID:");
        int gameId = scanner.nextInt();
    
        try {
            // Create a statement for executing SQL queries
            Statement statement = dbConn.createStatement();
    
            // SQL query to select the total tickets earned by each member for the specified Game ID
            String query = "SELECT MemberID, SUM(TicketsEarned) AS TotalTicketsEarned FROM Gameplay WHERE GameID = " + gameId + " GROUP BY MemberID";
    
            // Execute the query and get the result set
            ResultSet resultSet = statement.executeQuery(query);
    
            // Print the header for the result
            System.out.println("\nTotal tickets earned by each member for Game ID " + gameId + ":");
            System.out.println("-----------------------------------------------");
    
            // Iterate over the result set and print each member's total tickets earned
            while (resultSet.next()) {
                int memberId = resultSet.getInt("MemberID");
                int totalTicketsEarned = resultSet.getInt("TotalTicketsEarned");
    
                System.out.println("Member ID: " + memberId + ", Total Tickets Earned: " + totalTicketsEarned);
            }
    
            // Print the footer for the result
            System.out.println("-----------------------------------------------");
        } catch (SQLException e) {
            // Print error message if there is an SQL exception
            System.err.println("Error in executing query d.");
            e.printStackTrace();
        }
    
        // Close the scanner
        //scanner.close();
    }

    /*---------------------------------------------------------------------
    |  Method drop(connection)
    |
    |  Purpose:  Drops all tables in the database along with their foreign
    |            key constraints.
    |
    |  Pre-condition:  The JDBC database connection is valid and points to
    |                  the target database.
    |
    |  Post-condition: Drops all tables in the database along with their
    |                  foreign key constraints.
    |
    |  Parameters:
    |      dbConn -- Connection object representing the database connection.
    |
    |  Returns:  None.
    *-------------------------------------------------------------------*/
    private static void drop(Connection dbConn) {
        String[] tables = {"Transaction", "FoodCoupon", "Gameplay", "Member", "Prize", "Game", "MembershipTier"};

        try {
            Statement stmt = dbConn.createStatement();

            // drop foreign key constraints
            for (String table : tables) {
                ResultSet rs = stmt.executeQuery(
                    "SELECT constraint_name " +
                    "FROM user_constraints " +
                    "WHERE table_name = '" + table + "' AND constraint_type = 'R'"
                );

                while (rs.next()) {
                    String constraintName = rs.getString("constraint_name");
                    stmt.execute("ALTER TABLE " + table + " DROP CONSTRAINT " + constraintName);
                    System.out.println("Dropped foreign key constraint " + constraintName + " from table " + table);
                }
            }

            // drop tables
            for (String table : tables) {
                stmt.execute("DROP TABLE " + table);
                System.out.println("Dropped table: " + table);
            }

            System.out.println("Successfully dropped all tables.");
        } catch (SQLException e) {
            System.err.println("Couldn't drop tables.");
            e.printStackTrace();
            System.exit(-1);
        }
    }

}