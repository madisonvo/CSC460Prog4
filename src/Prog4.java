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

        // drop(dbConn);

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

    /*
     * Function: getConnection
     * Parameters: String[] args - The array of Strings containing the command line arguments.
     * Returns: dbConn - The JDBC database connection.
     * 
     * Pre-condition: The args parameter contains the username in index 0 and the password in index 1.
     * 
     * Post-condition: TODO
     * 
     * Purpose: TODO
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
            e.printStackTrace();
            return false;
        }
    }

        /*
     * Function: tableExists
     * Parameters: Connection dbConn - The JDBC database connection.
     *             String tableName - The name of the table we want to check if it exists.
     * Returns: true - table doesn't exist.
     *          false - table exists.
     * 
     * Pre-condition: The JDBC database connection is valid.
     * 
     * Post-condition: True or false will be returned.
     * 
     * Purpose: This function is responsible for checking to see if the given tableName exists within
     *          the given JDBC database connection, returning true if it doesn't, false otherwise.
     */
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
                if (!rowExists(dbConn, tableName, data[0])) {
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
            System.exit(-1);
        }
    }

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
                    statement.setInt(2, Integer.valueOf(data[1]));
                    statement.setInt(3, Integer.valueOf(data[2]));
                    statement.setInt(4, Integer.valueOf(data[3]));
                    statement.setInt(5, Integer.valueOf(data[4]));
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

    /*
     * Function: rowExists
     * Parameters: Connection dbConn - The JDBC database connection.
     *             String tableName - The name of the table we want to check if a given row exists in.
     *             String facilityId - The facility id we want to check exists in the given table name.
     * Returns: true if the row exists
     *          false if the row doesn't exist
     * 
     * Pre-condition: The JDBC database connection, table, and facility id are valid.
     * 
     * Post-condition: Returns true or false.
     * 
     * Purpose: This function is responsible for checking to see if a row (given a facility id) exists
     *          within the given table name in the given JDBC database connection.
     */
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

    private static void promptUpdate(Connection dbConn) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nWould you like to update the tables? (y/n)");
            String answer = scanner.nextLine();

            switch (answer.toLowerCase()) {
                case "y":
                    update(dbConn);
                    break;

                case "n":
                    return;

                default:
                    System.out.println("\nPlease choose a valid answer (y/n)");
            }
        }
    }

    private static void update(Connection dbConn) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Which table would you like to update? (Member, Game, Prize)");
        String answer = scanner.nextLine();

        switch (answer.toLowerCase()) {
            case "member":
                updateMember(dbConn);
                break;

            case "game":
                break;

            case "prize":
                break;

            case "default":
                System.out.println("\nPlease choose a valid table (Member/Game/Prize)");
        }
    }

    private static void updateMember(Connection dbConn) {
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
                break;
        }
    }

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

    private static void editMember(Connection dbConn) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the ID of the member you want to update:");
        String memberId = scanner.nextLine();


    }

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
            System.out.println("(d) Get the total number of tickets for a given gameID"); 
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

            System.out.println("\n--------------------------------------------");
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
            String query = "SELECT * FROM Member WHERE TotalSpending >= 100 AND LastVisitDate >= SYSDATE - 30";

            ResultSet resultSet = statement.executeQuery(query);

            System.out.println("Results: ");
            System.out.println("------------------------------------------------------------");
            while (resultSet.next()) {
                int memberID = resultSet.getInt("MemberID");
                String firstName = resultSet.getString("Fname");
                String lastName = resultSet.getString("Lname");
                String telephoneNum = resultSet.getString("TelephoneNum");
                String address = resultSet.getString("Address");
                int gameTokens = resultSet.getInt("GameTokens");
                double totalSpending = resultSet.getDouble("TotalSpending");
                int membershipTier = resultSet.getInt("MembershipTier");
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

    // TODO: Implement logic to answer query c
    private static void queryC(Connection dbConn) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter member ID to view available rewards:");
        int memberId = scanner.nextInt();

        try {
            Statement statement = dbConn.createStatement();

            String query = "SELECT Prize.Name, Prize.TicketCost WHERE Prize.TicketCost <= " + 
                            "(SELECT SUM(Gameplay.TicketsEarned) WHERE Gameplay.MemberID = " + memberId + ")";

            ResultSet tables = statement.executeQuery(query);
            System.out.println("Available rewards for member ID " + memberId + ":");
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

        scanner.close();
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

            System.out.println("Total tickets earned by each member for Game ID " + gameId + ":");
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

        scanner.close();
    }

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