package Project.manageDB;

import javax.naming.AuthenticationException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DbOperations extends UnicastRemoteObject implements DbOperationsInterface {
    
    private static String dbUrl;
    public DbOperations(String url)throws RemoteException {
        dbUrl = url;
    }
/*
    public static synchronized DbOperations getInstance(String url) {
        if (dbInstance == null) {
            dbInstance = new DbOperations(url);
        }
        return dbInstance;
    }
    public static synchronized DbOperations getInstance() {
        System.out.println("getInstance without URL called: "+dbInstance);
        if (dbInstance == null) {
            throw new IllegalStateException("getInstance with URL must be called first");
        }
        return dbInstance;
    }
    */


    public synchronized int insertNewUser(String name,int id, String email, String passwd){
        String dbAddress = "jdbc:sqlite:" + dbUrl;
        int insertState=-1;
        System.out.println(dbAddress);

        try(Connection conn = DriverManager.getConnection(dbAddress);
            Statement stmt = conn.createStatement()){

            //IT IS BETTER TO USE SOMETHING CALL "preparedStatement" but I barely know how that works
            String createEntryQuery = "INSERT OR REPLACE INTO Utilizador (IdNumber, Uname, Email, Password) VALUES " +
                    "('" + id + "', '" + name + "', '" + email + "', '" + passwd + "');";
            insertState = stmt.executeUpdate(createEntryQuery);
            System.out.println("INSERT OPERATION COMPLETED. <<JUST FOR DEBUG>>");

            if(insertState<1){
                System.out.println("Insertion failed");
            }


        } catch (SQLException e) {
            System.out.println("Exception reported:\r\n\t..." + e.getMessage());
        }
        return insertState;
    }

    public synchronized boolean authenticateUser(String email, String password) {
        String dbAddress = "jdbc:sqlite:" + dbUrl;

        try (Connection conn = DriverManager.getConnection(dbAddress)) {
            String selectUserQuery = "SELECT Email, Password FROM Utilizador WHERE Email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(selectUserQuery)) {
                pstmt.setString(1, email);
                ResultSet resultSet = pstmt.executeQuery();

                if (resultSet.next()) {
                    String storedPassword = resultSet.getString("Password");
                    // Compara el email y la contraseña proporcionados
                    if (password.equals(storedPassword)) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Exception reported:\r\n\t..." + e.getMessage());
        }

        throw new RuntimeException("Authentication failed for the user: " + email);
    }

    @Override
    public synchronized String[] getUserData(String email) {
        String dbAddress = "jdbc:sqlite:" + dbUrl;
        try (Connection conn = DriverManager.getConnection(dbAddress)) {
            String selectUserQuery = "SELECT IdNumber, Uname, Email FROM Utilizador WHERE Email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(selectUserQuery)) {
                pstmt.setString(1, email);
                ResultSet resultSet = pstmt.executeQuery();

                if (resultSet.next()) {
                    String idNumber = resultSet.getString("IdNumber");
                    String username = resultSet.getString("Uname");
                    String storedEmail = resultSet.getString("Email");

                    // Return user data as an array
                    return new String[]{username, idNumber, storedEmail};
                }
            }
        } catch (SQLException e) {
            System.out.println("Exception reported:\r\n\t..." + e.getMessage());
        }

        // Return null if no user data is found
        return null;
    }

    public synchronized List<Event> getAllEvents(){
        String dbAddress = "jdbc:sqlite:" + dbUrl;
        List<Event> events = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(dbAddress)) {
            String selectEventsQuery = "SELECT Events.*, COUNT(Assistants.idGuest) as AssistantsNumber " +
                    "FROM Events " +
                    "LEFT JOIN Assistants ON Events.id = Assistants.idEvent " +
                    "GROUP BY Events.id";
            try (PreparedStatement pstmt = conn.prepareStatement(selectEventsQuery)) {
                ResultSet resultSet = pstmt.executeQuery();

                while(resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    String location = resultSet.getString("location");
                    String data = resultSet.getString("data");
                    String startTime = resultSet.getString("starTime");
                    String endTime = resultSet.getString("endTime");
                    int numAssistants = resultSet.getInt("AssistantsNumber");
                    events.add(new Event(id, name, location, numAssistants,data, startTime, endTime));
                }
            }
        } catch (SQLException e) {
            System.out.println("Exception reported:\r\n\t..." + e.getMessage());
        }
        return events;
    }

    @Override
    public void joinAnEvent(String email, int eventID) throws RemoteException {
        String dbAddress = "jdbc:sqlite:" + dbUrl;

        try (Connection conn = DriverManager.getConnection(dbAddress)) {

            String selectUserIdQuery = "SELECT IdNumber FROM Utilizador WHERE Email = ?";
            try (PreparedStatement selectUserIdStmt = conn.prepareStatement(selectUserIdQuery)) {
                selectUserIdStmt.setString(1, email);
                ResultSet resultSet = selectUserIdStmt.executeQuery();

                if (resultSet.next()) {
                    int idGuest = resultSet.getInt("IdNumber");


                    String insertAssistantQuery = "INSERT INTO Assistants (idEvent, idGuest) VALUES (?, ?)";
                    try (PreparedStatement insertAttendanceStmt = conn.prepareStatement(insertAssistantQuery)) {
                        insertAttendanceStmt.setInt(1, eventID);
                        insertAttendanceStmt.setInt(2, idGuest);
                        insertAttendanceStmt.executeUpdate();

                        System.out.println("The user " + email + " has join to the event number " + eventID);
                    }
                } else {
                    System.out.println("The user with email " + email + " doesn´t exist.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Exception reported:\r\n\t..." + e.getMessage());
        }
    }


    @Override
    public void updateUserData(String name, int id, String email, String passwd, String oldEmail) throws RemoteException {
        String dbAddress = "jdbc:sqlite:" + dbUrl;

        try (Connection conn = DriverManager.getConnection(dbAddress)) {
            // Use a prepared statement to avoid SQL injection
            String updateUserQuery = "UPDATE Utilizador SET Uname=?, IdNumber=?, Email=?, Password=? WHERE Email=?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateUserQuery)) {
                pstmt.setString(1, name);
                pstmt.setInt(2, id);
                pstmt.setString(3, email);
                pstmt.setString(4, passwd);
                pstmt.setString(5, oldEmail);

                int rowsUpdated = pstmt.executeUpdate();
                System.out.println("UPDATE OPERATION COMPLETED. <<JUST FOR DEBUG>>");
                // Check if any rows were updated
                if (rowsUpdated > 0) {
                    System.out.println("User data updated successfully.");
                } else {
                    System.out.println("No user found with the given email. Update failed.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Exception reported:\r\n\t..." + e.getMessage());
        }

    }

    public synchronized void versionController(){
        String dbAddress = "jdbc:sqlite:" + dbUrl;

        try (Connection conn = DriverManager.getConnection(dbAddress)){
            String insertVersionQuery = "INSERT INTO your_table (column2) VALUES (strftime('%Y-%m-%d %H:%M:%S', 'now'))";
            try (PreparedStatement preparedStatement = conn.prepareStatement(insertVersionQuery, Statement.RETURN_GENERATED_KEYS)){
                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected > 0){
                    System.out.println("Rows inserted succefully.");

                    try (var generatedKeys = preparedStatement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int generatedId = generatedKeys.getInt(1);
                            System.out.println("Generated ID of backup:" + generatedId);
                        }
                    }
                } else {
                    System.out.println("No ID generated for the backup.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

}



