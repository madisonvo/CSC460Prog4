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
        if (createTables(dbConn)) {
            System.out.println("Tables created successfully.");
            // importMemberData(dbConn, "Member.csv");
            importGameData(dbConn, "Game.csv");
            // importGameplayData(dbConn, "Gameplay.csv");
            // importPrizeData(dbConn, "Prize.csv");
            // importFoodCouponData(dbConn, "FoodCoupon.csv");
            System.out.println("Data imported successfully.");
        } else {
            System.out.println("Tables already exist. Skipping table creation and data import.");
        }
        String[] tableNames = {"Member", "Game", "Gameplay", "Prize", "FoodCoupon", "MembershipTier", "Transaction"};
        printTableAttributes(dbConn, tableNames);
       
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
            System.err.println("Could not insert into database tables.");
            e.printStackTrace();
            // System.exit(-1);
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
            // System.exit(-1);
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
            // System.exit(-1);
        }
    }

    private static void importPrizeData(Connection dbConn, String file) {
        String tableName = "Prize";

        System.out.println(tableName);

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
            // System.exit(-1);
        }
    }

    private static void importFoodCouponData(Connection dbConn, String file) {
        String tableName = "FoodCoupon";

        System.out.println(tableName);

        try {
            String insert = "INSERT INTO " + tableName + " VALUES (?, ?, ?)";
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
            // System.exit(-1);
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
}