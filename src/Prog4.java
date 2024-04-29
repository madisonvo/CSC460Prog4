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
        } else {
            System.out.println("Tables already exist. Skipping table creation and data import.");
        }

        promptUpdate(dbConn);
        
        answerQueries(dbConn);
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

        return dbConn;
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
        // SQL statements to create tables
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
        
        String createGameTable = "CREATE TABLE Game (" +
        "GameID INT PRIMARY KEY, " +
        "Name VARCHAR(100), " +
        "TokenCost INT, " +
        "Tickets INT" +
        ")";

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

        String createPrizeTable = "CREATE TABLE Prize (" +
        "PrizeID INT PRIMARY KEY, " +
        "Name VARCHAR(100), " +
        "TicketCost INT" +
        ")";

        String createFoodCouponTable = "CREATE TABLE FoodCoupon (" +
        "FoodCouponID INT PRIMARY KEY, " +
        "MemberID INT, " +
        "RedeemedFood VARCHAR(100), " +
        "Used NUMBER(1), " +
        "FOREIGN KEY (MemberID) REFERENCES Member(MemberID)" +
        ")";

        String createMembershipTierTable = "CREATE TABLE MembershipTier (" +
        "MembershipTierID INT PRIMARY KEY, " +
        "Name VARCHAR(100), " +
        "TotalSpendingReq DECIMAL(10, 2), " +
        "DiscountPercentage DECIMAL(5, 2), " +
        "FreeTickets INT" +
        ")";

        String createTransactionTable = "CREATE TABLE Transaction (" +
        "TransactionID INT PRIMARY KEY, " +
        "Type VARCHAR(50), " +
        "Amount DECIMAL(10, 2), " +
        "\"Date\" DATE" +
        ")";

        String[] tableNames = {"Member", "Game", "Gameplay", "Prize", "FoodCoupon", "MembershipTier", "Transaction"};
        String[] createTableQueries = {createMemberTable, createGameTable, createGameplayTable, createPrizeTable, createFoodCouponTable, createMembershipTierTable, createTransactionTable};

        try (Statement statement = dbConn.createStatement()) {
            // Check if tables already exist
            for (int i = 0; i < tableNames.length; i++) {
                if (tableExists(dbConn, tableNames[i])) {
                    System.out.println("Table " + tableNames[i] + " already exists.");
                    return false;
                } else {
                    statement.execute(createTableQueries[i]);
                    System.out.println("Table " + tableNames[i] + " created successfully.");
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
        String tableName = "Member";

        System.out.println(tableName);

        try {
            String insert = "INSERT INTO " + tableName + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'), ?)";
            PreparedStatement statement = dbConn.prepareStatement(insert);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                for (int i = 0; i < data.length; i ++) {
                    System.out.println(data[i]);
                }
                if (!rowExists(dbConn, tableName, data[0])) {
                    statement.setInt(1, Integer.valueOf(data[0]));
                    System.out.println(1 + " successful");
                    statement.setString(2, data[1]);
                    System.out.println(2 + " successful");
                    statement.setString(3, data[2]);
                    System.out.println(3 + " successful");
                    statement.setString(4, data[3]);
                    System.out.println(4 + " successful");
                    statement.setString(5, data[4]);
                    System.out.println(5 + " successful");
                    statement.setInt(6, Integer.valueOf(data[5]));
                    System.out.println(6 + " successful");
                    statement.setDouble(7, Double.valueOf(data[6]));
                    System.out.println(7 + " successful");
                    statement.setString(8, data[7]);
                    System.out.println(8 + " successful");
                    statement.setInt(9, Integer.valueOf(data[8]));
                    System.out.println(9 + " successful");
                    statement.setDate(10, java.sql.Date.valueOf(data[9]));
                    System.out.println(10 + " successful");
                    statement.setInt(11, Integer.valueOf(data[10]));
                    System.out.println(11 + " successful");

                    statement.executeUpdate();
                } else {
                    System.out.println("Skipping duplicate row");
                }
            }

            System.out.println("Successful import of " + file + " into table " + tableName + ".");
            reader.close();
            statement.close();
        } catch (Exception e) {
            //System.err.println("Could not insert into database tables.");
            //e.printStackTrace();
            // System.exit(-1);
        }
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
        String tableName = "Game";

        System.out.println(tableName);

        try {
            String insert = "INSERT INTO " + tableName + " VALUES (?, ?, ?, ?)";
            PreparedStatement statement = dbConn.prepareStatement(insert);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (!rowExists(dbConn, tableName, data[0])) {
                    statement.setInt(1, Integer.valueOf(data[0]));
                    statement.setString(2, data[1]);
                    statement.setInt(3, Integer.valueOf(data[2]));
                    statement.setInt(4, Integer.valueOf(data[3]));

                    statement.executeUpdate();
                } else {
                    System.out.println("Skipping duplicate row");
                }
            }

            System.out.println("Successful import of " + file + " into table " + tableName + ".");
            reader.close();
            statement.close();
        } catch (Exception e) {
            System.err.println("Could not insert into database tables.");
            e.printStackTrace();
        }
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
        String tableName = "Gameplay";

        System.out.println(tableName);

        try {
            String insert = "INSERT INTO " + tableName + " VALUES (?, ?, ?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'))";
            PreparedStatement statement = dbConn.prepareStatement(insert);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (!rowExists(dbConn, tableName, data[0])) {
                    statement.setInt(1, Integer.valueOf(data[0]));
                    System.out.println(1 + " successful");
                    statement.setInt(2, Integer.valueOf(data[1]));
                    System.out.println(2 + " successful");
                    statement.setInt(3, Integer.valueOf(data[2]));
                    System.out.println(3 + " successful");
                    statement.setInt(4, Integer.valueOf(data[3]));
                    System.out.println(4 + " successful");
                    statement.setInt(5, Integer.valueOf(data[4]));
                    System.out.println(5 + " successful");
                    statement.setString(6, data[5]);

                    statement.executeUpdate();
                } else {
                    System.out.println("Skipping duplicate row");
                }
            }

            System.out.println("Successful import of " + file + " into table " + tableName + ".");
            reader.close();
            statement.close();
        } catch (Exception e) {
            System.err.println("Could not insert into database tables.");
            e.printStackTrace();
        }
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
        String tableName = "Prize";

        System.out.println("\nInserting into " + tableName);

        try {
            String insert = "INSERT INTO " + tableName + " VALUES (?, ?, ?)";
            PreparedStatement statement = dbConn.prepareStatement(insert);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (!rowExists(dbConn, tableName, data[0])) {
                    statement.setInt(1, Integer.valueOf(data[0]));
                    statement.setString(2, data[1]);
                    statement.setInt(3, Integer.valueOf(data[2]));

                    statement.executeUpdate();
                } else {
                    System.out.println("Skipping duplicate row");
                }
            }

            System.out.println("Successful import of " + file + " into table " + tableName + ".");
            reader.close();
            statement.close();
        } catch (Exception e) {
            System.err.println("Could not insert into database tables.");
            e.printStackTrace();
        }
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
        String tableName = "FoodCoupon";

        System.out.println("\nInserting into " + tableName);

        try {
            String insert = "INSERT INTO " + tableName + " VALUES (?, ?, ?, ?)";
            PreparedStatement statement = dbConn.prepareStatement(insert);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (!rowExists(dbConn, tableName, data[0])) {
                    statement.setInt(1, Integer.valueOf(data[0]));
                    statement.setInt(2, Integer.valueOf(data[1]));
                    statement.setString(3, data[2]);
                    statement.setBoolean(4, Boolean.valueOf(data[3]));

                    statement.executeUpdate();
                } else {
                    System.out.println("Skipping duplicate row");
                }
            }

            System.out.println("Successful import of " + file + " into table " + tableName + ".");
            reader.close();
            statement.close();
        } catch (Exception e) {
            System.err.println("Could not insert into database tables.");
            e.printStackTrace();
        }
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
        String tableName = "MembershipTier";

        System.out.println("\nInserting into " + tableName);

        try {
            String insert = "INSERT INTO " + tableName + " VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = dbConn.prepareStatement(insert);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (!rowExists(dbConn, tableName, data[0])) {
                    statement.setInt(1, Integer.valueOf(data[0]));
                    statement.setString(2, data[1]);
                    statement.setDouble(3, Double.valueOf(data[2]));
                    statement.setDouble(4, Double.valueOf(data[3]));
                    statement.setInt(5, Integer.valueOf(data[4]));

                    statement.executeUpdate();
                } else {
                    System.out.println("Skipping duplicate row");
                }
            }

            System.out.println("Successful import of " + file + " into table " + tableName + ".");
            reader.close();
            statement.close();
        } catch (Exception e) {
            System.err.println("Could not insert into database tables.");
            e.printStackTrace();
        }
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
        String tableName = "Transaction";

        System.out.println("\nInserting into " + tableName);

        try {
            String insert = "INSERT INTO " + tableName + " VALUES (?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'))";
            PreparedStatement statement = dbConn.prepareStatement(insert);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (!rowExists(dbConn, tableName, data[0])) {
                    statement.setInt(1, Integer.valueOf(data[0]));
                    statement.setString(2, data[1]);
                    statement.setDouble(3, Double.valueOf(data[2]));
                    statement.setDate(4, java.sql.Date.valueOf(data[3]));

                    statement.executeUpdate();
                } else {
                    System.out.println("Skipping duplicate row");
                }
            }

            System.out.println("Successful import of " + file + " into table " + tableName + ".");
            reader.close();
            statement.close();
        } catch (Exception e) {
            System.err.println("Could not insert into database tables.");
            e.printStackTrace();
        }
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
        System.out.println(id);
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
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nWould you like to update the tables? (y/n)");
            String answer = scanner.nextLine();

            switch (answer.toLowerCase()) {
                case "y":
                    update(dbConn);
                    break;

                case "n":
                    //scanner.close();
                    //break;
                    return;

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
        Scanner scanner = new Scanner(System.in);
        System.out.println("Which table would you like to update? (Member, Game, Prize)");
        String answer = scanner.nextLine();

        switch (answer.toLowerCase()) {
            case "member":
                updateMember(dbConn);
                break;

            case "game":
                updateGame(dbConn);
                break;

            case "prize":
                updatePrize(dbConn);
                break;

            case "default":
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
        Scanner scanner = new Scanner(System.in);
        System.out.println("How would you like to update the Member table? (Add/Update/Delete)");
        String answer = scanner.nextLine();

        switch (answer.toLowerCase()) {
            case "add":
                addMember(dbConn);
                break;

            case "update":
                editMember(dbConn);
                break;

            case "delete":
                deleteMember(dbConn);
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
        int newMemberID = getLastMemberID(dbConn) + 1;
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the new member's first name:");
        String fName = scanner.nextLine();

        System.out.println("Enter the new member's last name:");
        String lName = scanner.nextLine();

        System.out.println("Enter the new member's phone number:");
        String phoneNum = scanner.nextLine();

        System.out.println("Enter the new member's address:");
        String address = scanner.nextLine();

        String insert = "INSERT INTO Member VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'), ?)";
        try {
            PreparedStatement statement = dbConn.prepareStatement(insert);

            if (!rowExists(dbConn, "Member", String.valueOf(newMemberID))) {
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


                statement.executeUpdate();
                System.out.println("Successfully added new member " + fName + " " + lName);
            } else {
                System.out.println("Skipping duplicate row");
            }
        } catch (SQLException e) {
            System.err.println("Could not add new member.");
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
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the ID of the member you want to update:");
        int memberId = scanner.nextInt();

        if (!memberExists(dbConn, memberId)) {
            System.out.println("Member with ID " + memberId + " does not exist.");
            return;
        }

        System.out.println("What would you like to update for this member? (Phone number/Address/Both)");
        String toChange = scanner.nextLine();

        String newPhoneNumber = null;
        String newAddress = null;

        switch (toChange.toLowerCase()) {
            case "phone number":
                System.out.println("Enter the new phone number (###-###-####:");
                newPhoneNumber = scanner.nextLine();
                break;

            case "address":
                System.out.println("Enter the new address:");
                newAddress = scanner.nextLine();
                break;

            case "both":
                System.out.println("Enter the new phone number (###-###-####:");
                newPhoneNumber = scanner.nextLine();
                System.out.println("Enter the new address:");
                newAddress = scanner.nextLine();
                break;

            default:
                System.out.println("Please choose a valid option (Phone number/Address/Both)");
        }

        String update = "UPDATE Member SET ";
        if (newPhoneNumber != null) {
            update += "TelephoneNum = '" + newPhoneNumber + "'";
        }

        if (newAddress != null) {
            if (newPhoneNumber != null) {
                update += ", ";
            }

            update += "Address = '" + newAddress + "'";
        }

        update += "WHERE MemberID = ?";

        try {
            PreparedStatement statement = dbConn.prepareStatement(update);
            statement.setInt(1, memberId);
            int updatedRow = statement.executeUpdate();
            System.out.println(updatedRow + " row(s) updated successfully.");
        } catch (SQLException e) {
            System.out.println("Could not update row(s).");
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
        String sql = "SELECT COUNT(*) FROM Member WHERE MemberID = ?";
        try {
            PreparedStatement statement = dbConn.prepareStatement(sql);
            statement.setInt(1, memberId);
            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        } catch (SQLException e) {

        }
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
    private static void deleteMember(Connection dbConn) {
        Scanner scanner = new Scanner(System.in);
    
        System.out.println("Enter the Member ID of the member you want to delete:");
        int memberId = scanner.nextInt();
        scanner.nextLine(); // Consume newline character
    
        try {
            PreparedStatement preparedStatement = dbConn.prepareStatement("DELETE FROM Member WHERE MemberID = ?");
            preparedStatement.setInt(1, memberId);
    
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Member with ID " + memberId + " deleted successfully.");
            } else {
                System.out.println("No member found with ID " + memberId + ".");
            }
        } catch (SQLException e) {
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

        System.out.println("How would you like to update the Game table? (Add/Delete)");
        String answer = scanner.nextLine();
    
        switch (answer.toLowerCase()) {
            case "add":
                addGame(dbConn);
                break;
    
            case "delete":
                deleteGame(dbConn);
                break;
    
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

        System.out.println("Enter the ID of the game you want to delete:");
        int gameID = scanner.nextInt();
        scanner.nextLine(); // Consume newline character

        try (Statement statement = dbConn.createStatement()) {
            String query = String.format("DELETE FROM Game WHERE GameID = %d", gameID);
            int rowsAffected = statement.executeUpdate(query);
            if (rowsAffected > 0) {
                System.out.println("Game deleted successfully.");
            } else {
                System.out.println("Failed to delete game. Make sure the game ID is correct.");
            }
        } catch (SQLException e) {
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
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the name of the new game:");
        String name = scanner.nextLine();

        System.out.println("Enter the token cost of the new game:");
        int tokenCost = scanner.nextInt();
        scanner.nextLine(); // Consume newline character

        System.out.println("Enter the number of tickets earned by playing the new game:");
        int tickets = scanner.nextInt();
        scanner.nextLine(); // Consume newline character

        try (Statement statement = dbConn.createStatement()) {
            String query = String.format("INSERT INTO Game (Name, TokenCost, Tickets) VALUES ('%s', %d, %d)", name, tokenCost, tickets);
            int rowsAffected = statement.executeUpdate(query);
            if (rowsAffected > 0) {
                System.out.println("New game added successfully.");
            } else {
                System.out.println("Failed to add new game.");
            }
        } catch (SQLException e) {
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
        Scanner scanner = new Scanner(System.in);
        System.out.println("How would you like to update the Prize table? (Add/Delete)");
        String answer = scanner.nextLine();
    
        switch (answer.toLowerCase()) {
            case "add":
                addPrize(dbConn);
                break;
    
            case "delete":
                deletePrize(dbConn);
                break;
    
            default:
                System.out.println("\nPlease choose a valid action (AddDelete)");
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
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the ID of the prize you want to delete:");
        int prizeID = scanner.nextInt();

        try (Statement statement = dbConn.createStatement()) {
            String deleteQuery = "DELETE FROM Prize WHERE PrizeID = " + prizeID;
            int rowsAffected = statement.executeUpdate(deleteQuery);
            if (rowsAffected > 0) {
                System.out.println("Prize with ID " + prizeID + " deleted successfully.");
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
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the ID of the new prize:");
        int newPrizeID = scanner.nextInt();
    
        System.out.println("Enter the name of the new prize:");
        String newName = scanner.nextLine();
    
        System.out.println("Enter the ticket cost for the new prize:");
        int newTicketCost = scanner.nextInt();
    
        try (PreparedStatement preparedStatement = dbConn.prepareStatement("INSERT INTO Prize (PrizeID, Name, TicketCost) VALUES (?, ?, ?)")) {
            preparedStatement.setInt(1, newPrizeID);
            preparedStatement.setString(2, newName);
            preparedStatement.setInt(3, newTicketCost);
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
            Statement statement = dbConn.createStatement();
            ResultSet tables = statement.executeQuery("SELECT MAX(MemberID) AS LastMemberID FROM Member");

            if (tables.next()) {
                int maxMemberID = tables.getInt("LastMemberID");
                tables.close();
                statement.close();
                return maxMemberID;
            } else {
                tables.close();
                statement.close();
                return 0;
            }
        } catch (SQLException e) {
            System.err.println("Couldn't get last member ID.");
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
                    System.out.println("\nPlease choose a valid query (a, b, c, d, or e)");
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
        try(Statement statement = dbConn.createStatement()){
            String query = "SELECT Game.Name, Member.Fname, Member.Lname FROM Game, Member," + 
            " Gameplay WHERE Game.GameID = Gameplay.GameID AND Member.MemberID = Gameplay.MemberID " +
            "AND Gameplay.Score = (SELECT MAX(Score) FROM Gameplay WHERE Gameplay.GameID = Game.GameID)";
            ResultSet rs = statement.executeQuery(query);

            System.out.println("\nGames in arcade and current high scores:");
            System.out.println("--------------------------------------------");
            while(rs.next()){
                System.out.println("Game: " + rs.getString("Name") + " High Score: " + rs.getString("Fname") + " " + rs.getString("Lname"));
            }
            System.out.println("--------------------------------------------");
        }catch (SQLException e) {
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
            String query = "SELECT * FROM Member WHERE TotalSpending >= 100 AND LastVisitDate >= (SELECT SYSDATE - 30 FROM DUAL)";

            ResultSet resultSet = statement.executeQuery(query);

            System.out.println("\nNames and membership information of all members who have spent at least $100 on tokens in the past month:");
            System.out.println("------------------------------------------------------------");
            while (resultSet.next()) {
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
        }catch (SQLException e) {
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
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter member ID to view available rewards:");
        int memberId = scanner.nextInt();

        try {
            Statement statement = dbConn.createStatement();

            String query = "SELECT Prize.Name, Prize.TicketCost FROM Prize WHERE Prize.TicketCost <= " + 
                            "(SELECT Member.TotalTickets FROM Member WHERE Member.MemberID = " + memberId + ")";

            ResultSet tables = statement.executeQuery(query);
            System.out.println("\nAvailable rewards for member ID " + memberId + ":");
            System.out.println("-----------------------------------------------");
            while (tables.next()) {
                String prizeName = tables.getString("Name");
                int ticketCost = tables.getInt("TicketCost");

                System.out.println("Prize: " + prizeName + ", Ticket cost: " + ticketCost);
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
        System.out.println("Enter Game ID:");
        int gameId = scanner.nextInt();

        try {
            Statement statement = dbConn.createStatement();

            String query = "SELECT MemberID, SUM(TicketsEarned) AS TotalTicketsEarned FROM Gameplay WHERE GameID = " + gameId + " GROUP BY MemberID";

            ResultSet resultSet = statement.executeQuery(query);

            System.out.println("\nTotal tickets earned by each member for Game ID " + gameId + ":");
            System.out.println("-----------------------------------------------");
            while (resultSet.next()) {
                int memberId = resultSet.getInt("MemberID");
                int totalTicketsEarned = resultSet.getInt("TotalTicketsEarned");

                System.out.println("Member ID: " + memberId + ", Total Tickets Earned: " + totalTicketsEarned);
            }
            System.out.println("-----------------------------------------------");
        } catch (SQLException e) {
            System.err.println("Error in executing query d.");
            e.printStackTrace();
        }

        //scanner.close();
    }

    /*---------------------------------------------------------------------
    |  Method printTableAttributes(connection, tablesToPrint)
    |
    |  Purpose:  Retrieves and prints the attributes (columns) of specified
    |            tables in the database.
    |
    |  Pre-condition:  The JDBC database connection is valid and points to
    |                  the target database.
    |
    |  Post-condition: Prints the attributes (columns) of the specified
    |                  tables if they exist in the database.
    |
    |  Parameters:
    |      dbConn -- Connection object representing the database connection.
    |      tablesToPrint -- Array of table names for which attributes need
    |                       to be printed.
    |
    |  Returns:  None.
    *-------------------------------------------------------------------*/
    private static void printTableAttributes(Connection dbConn, String[] tablesToPrint) {
        try {
            DatabaseMetaData metaData = dbConn.getMetaData();
    
            // Iterate through specified tables
            for (String tableName : tablesToPrint) {
                ResultSet tables = metaData.getTables(null, null, tableName, null);
    
                // Check if table exists
                if (!tables.next()) {
                    System.out.println("\nTable: " + tableName);
    
                    // Get columns for the table
                    ResultSet columns = metaData.getColumns(null, null, tableName, null);
    
                    // Iterate through columns
                    while (columns.next()) {
                        String columnName = columns.getString("COLUMN_NAME");
                        String dataType = columns.getString("TYPE_NAME");
                        int columnSize = columns.getInt("COLUMN_SIZE");
                        System.out.println("  " + columnName + " " + dataType + "(" + columnSize + ")");
                    }
                    columns.close(); // Close columns ResultSet
                } else {
                    System.out.println("Table " + tableName + " does not exist.");
                }
                tables.close(); // Close tables ResultSet
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

            // Drop foreign key constraints
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

            // Drop tables
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