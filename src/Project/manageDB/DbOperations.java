package Project.manageDB;

import Project.client.exceptions.AuthenticationErrorException;
import Project.manageDB.data.Attendance;
import Project.manageDB.data.Event;
import Project.principalServer.PrincipalServerInterface;
import Project.principalServer.data.Heardbeat;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DbOperations extends UnicastRemoteObject implements DbOperationsInterface {

    String objUrl = "rmi://localhost:4444/p1";
    int rmiPort;
    String RMIname;
    PrincipalServerInterface  serverService;


    private static String dbUrl;
    public DbOperations(String url,int rmiPort,String RMIname)throws RemoteException {
        dbUrl = url;
    }


    public synchronized int insertNewUser(String name, int id, String email, String passwd) {
        String dbAddress = "jdbc:sqlite:" + dbUrl;
        int insertState = 0;

        try (Connection conn = DriverManager.getConnection(dbAddress)) {
            String createEntryQuery = "INSERT INTO Users (id, name, email, password, roleId) VALUES (?,?, ?, ?,?)";
            try (PreparedStatement pstmt = conn.prepareStatement(createEntryQuery)) {
                pstmt.setInt(1, id);
                pstmt.setString(2, name);
                pstmt.setString(3, email);
                pstmt.setString(4, passwd);
                pstmt.setInt(5, 2);

                insertState = pstmt.executeUpdate();
                if (insertState < 1) {
                    System.out.println("Insertion failed.");
                }
            }
            updateVersion();
        } catch (SQLException e) {
            System.out.println("Exception reported:\r\n\t..." + e.getMessage());
       }
        return insertState;
    }


    public synchronized boolean authenticateUser(String email, String password) throws AuthenticationErrorException {
        String dbAddress = "jdbc:sqlite:" + dbUrl;

        try (Connection conn = DriverManager.getConnection(dbAddress)) {
            String selectUserQuery = "SELECT email, Password FROM Users WHERE email = ?";
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
        return false;
    }



    @Override
    public synchronized String[] getUserData(String email) {
        String dbAddress = "jdbc:sqlite:" + dbUrl;
        try (Connection conn = DriverManager.getConnection(dbAddress)) {
            String selectUserQuery = "SELECT id,name, email FROM Users WHERE email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(selectUserQuery)) {
                pstmt.setString(1, email);
                ResultSet resultSet = pstmt.executeQuery();

                if (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String username = resultSet.getString("name");
                    String storedEmail = resultSet.getString("email");

                    return new String[]{String.valueOf(id),username,storedEmail};
                }
            }
        } catch (SQLException e) {
            System.out.println("Exception reported:\r\n\t..." + e.getMessage());
        }

        return null;
    }

    public synchronized List<Event> getAllEvents(){
        String dbAddress = "jdbc:sqlite:" + dbUrl;
        List<Event> events = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(dbAddress)) {
            String selectEventsQuery = "SELECT Events.*, COUNT(Attendance.idGuest) as AttendeesNumber " +
                    "FROM Events " +
                    "LEFT JOIN Attendance ON Events.id = Attendance.idEvent " +
                    "GROUP BY Events.id";
            try (PreparedStatement pstmt = conn.prepareStatement(selectEventsQuery)) {
                ResultSet resultSet = pstmt.executeQuery();

                while(resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    String location = resultSet.getString("location");
                    String date = resultSet.getString("date");
                    String startTime = resultSet.getString("startTime");
                    String endTime = resultSet.getString("endTime");
                    int numAttendees = resultSet.getInt("AttendeesNumber");
                    events.add(new Event(id, name, location, numAttendees,date, startTime, endTime));
                }
            }
        } catch (SQLException e) {
            System.out.println("Exception reported:\r\n\t..." + e.getMessage());
        }
        return events;
    }

    @Override
    public synchronized void joinAnEvent(String email, int eventID) throws RemoteException {
        String dbAddress = "jdbc:sqlite:" + dbUrl;
        if (isUserEnrolledInAnEvent(email, eventID)) {
            System.out.println("The user " + email + " is already enrolled in the event");
        } else {
                if (isUserEnrolledInActiveEvent(email)) {
                    System.out.println("The user " + email + " is already enrolled in an active event.");
                } else {
                    try (Connection conn = DriverManager.getConnection(dbAddress)) {
                        String insertAttendeeQuery = "INSERT INTO Attendance (idEvent, idGuest) VALUES (?, ?)";
                        try (PreparedStatement insertAttendanceStmt = conn.prepareStatement(insertAttendeeQuery)) {
                            insertAttendanceStmt.setInt(1, eventID);
                            insertAttendanceStmt.setString(2, email);
                            insertAttendanceStmt.executeUpdate();

                            System.out.println("The user " + email + " has joined the event number " + eventID);
                        }
                    }catch (SQLException e) {
                        System.out.println("Exception reported:\r\n\t..." + e.getMessage());
                    }
                }
                updateVersion();
        }
    }
    private boolean isUserEnrolledInActiveEvent(String email) {
        String dbAddress = "jdbc:sqlite:" + dbUrl;
        String checkEnrollmentQuery = "SELECT COUNT(*) FROM Attendance a "
                + "INNER JOIN Events e ON a.idEvent = e.id "
                + "WHERE a.idGuest = ? AND ? BETWEEN e.startTime AND e.endTime";

        try (Connection conn = DriverManager.getConnection(dbAddress);
             PreparedStatement checkEnrollmentStmt = conn.prepareStatement(checkEnrollmentQuery)) {

            checkEnrollmentStmt.setString(1, email);
            // Get current datetime
            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
            checkEnrollmentStmt.setTimestamp(2, currentTimestamp);

            try (ResultSet resultSet = checkEnrollmentStmt.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Exception reported:\r\n\t..." + e.getMessage());
        }
        return false;
    }
    private boolean isUserEnrolledInAnEvent(String email, int eventId) {
        String dbAddress = "jdbc:sqlite:" + dbUrl;
        try (Connection conn = DriverManager.getConnection(dbAddress)) {
            String checkEnrollmentQuery = "SELECT COUNT(*) FROM Attendance WHERE idGuest = ? AND idEvent = ?";
            try (PreparedStatement checkEnrollmentStmt = conn.prepareStatement(checkEnrollmentQuery)) {
                checkEnrollmentStmt.setString(1, email);
                checkEnrollmentStmt.setInt(2, eventId);

                try (ResultSet resultSet = checkEnrollmentStmt.executeQuery()) {
                    if (resultSet.next() && resultSet.getInt(1) > 0) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Exception reported:\r\n\t..." + e.getMessage());
        }
        return false;
    }

    @Override
    public synchronized List<Event> getUserEvents(String email) throws RemoteException {
        List<Event> userEvents = new ArrayList<>();
        String dbAddress = "jdbc:sqlite:" + dbUrl;

        try (Connection conn = DriverManager.getConnection(dbAddress)) {
                    String selectUserEventsQuery = "SELECT Events.*, COUNT(Attendance.idGuest) as AttendeesNumber " +
                            "FROM Events " +
                            "JOIN Attendance ON Events.id = Attendance.idEvent " +
                            "WHERE Attendance.idGuest = ? " +
                            "GROUP BY Events.id";
                    try (PreparedStatement selectUserEventsStmt = conn.prepareStatement(selectUserEventsQuery)) {
                        selectUserEventsStmt.setString(1, email);
                        ResultSet eventsResultSet = selectUserEventsStmt.executeQuery();

                        while (eventsResultSet.next()) {
                            int id = eventsResultSet.getInt("id");
                            String name = eventsResultSet.getString("name");
                            String location = eventsResultSet.getString("location");
                            String date = eventsResultSet.getString("date");
                            String startTime = eventsResultSet.getString("startTime");
                            String endTime = eventsResultSet.getString("endTime");
                            int numAttendees = eventsResultSet.getInt("AttendeesNumber");
                            userEvents.add(new Event(id, name, location, numAttendees, date, startTime, endTime));
                        }
                    }
        } catch (SQLException e) {
            System.out.println("Exception reported:\r\n\t..." + e.getMessage());
        }

        return userEvents;
    }


    @Override
    public synchronized void updateUserData(String name, String email, String passwd, String oldEmail) throws RemoteException {
        String dbAddress = "jdbc:sqlite:" + dbUrl;

        try (Connection conn = DriverManager.getConnection(dbAddress)) {
            String updateUserQuery = "UPDATE Users SET name=?, email=?, Password=? WHERE email=?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateUserQuery)) {
                pstmt.setString(1, name);
                pstmt.setString(2, email);
                pstmt.setString(3, passwd);
                pstmt.setString(4, oldEmail);

                int rowsUpdated = pstmt.executeUpdate();
                System.out.println("Update completed successfully.");
                // Check if any rows were updated
                if (rowsUpdated > 0) {
                    System.out.println("User data updated successfully.");
                } else {
                    System.out.println("No user found with the given email. Update failed.");
                }
            }
            updateVersion();
        } catch (SQLException e) {
            System.out.println("Exception reported:\r\n\t..." + e.getMessage());
        }

    }
    @Override
    public boolean isAdmin(String email) throws RemoteException {
        String dbAddress = "jdbc:sqlite:" + dbUrl;

        try (Connection conn = DriverManager.getConnection(dbAddress)) {
            String isAdminQuery = "SELECT Users.roleId, Roles.id " +
                    "FROM Users " +
                    "JOIN Roles ON Users.roleId = Roles.id " +
                    "WHERE Users.email = ? AND Roles.roleName = 'admin'";
            try (PreparedStatement pstmt = conn.prepareStatement(isAdminQuery)) {
                pstmt.setString(1, email);
                ResultSet resultSet = pstmt.executeQuery();
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public synchronized void deleteEvent(int eventId) throws RemoteException {
        String dbAddress = "jdbc:sqlite:" + dbUrl;

        try (Connection connection = DriverManager.getConnection(dbAddress)) {
            boolean hasAttendees = hasAttendees(connection, eventId);
            if (!hasAttendees) {
                String deleteEventQuery = "DELETE FROM Events WHERE id = ?";

                try (PreparedStatement preparedStatement = connection.prepareStatement(deleteEventQuery)) {
                    preparedStatement.setInt(1, eventId);

                    System.out.println("Deleting event...");

                    int rowsAffected = preparedStatement.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("Event remove");
                        //serverService.pruebaRMI(new Heardbeat(rmiPort,RMIname,getDbVersion()),"EVENT ELIMINATED");
                    } else {
                        System.out.println("Event not found to remove.");
                    }
                }
            } else {
                System.out.println("Event can not be removed. Attendees already registered.");
            }
        updateVersion();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized int deleteUserFromEvent(int eventId, String userEmail) {
        String dbAddress = "jdbc:sqlite:" + dbUrl;
        int rowsAffected = 0;
        try (Connection conn = DriverManager.getConnection(dbAddress)) {
            String deleteAttendanceQuery = "DELETE FROM Attendance WHERE idEvent = ? AND idGuest = ?";
            try (PreparedStatement deleteAttendanceStmt = conn.prepareStatement(deleteAttendanceQuery)) {
                deleteAttendanceStmt.setInt(1, eventId);
                deleteAttendanceStmt.setString(2, userEmail);

                rowsAffected = deleteAttendanceStmt.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("User removed from the event successfully.");
                } else {
                    System.out.println("User is not registered for the event.");
                }
            }
            updateVersion();
        } catch (SQLException e) {
            System.out.println("Exception reported:\r\n\t..." + e.getMessage());
        }
        return rowsAffected;
    }

    public synchronized boolean addUserToEvent(int eventId, String userEmail) {
        String dbAddress = "jdbc:sqlite:" + dbUrl;
        try (Connection conn = DriverManager.getConnection(dbAddress)) {
            String checkAttendanceQuery = "SELECT COUNT(*) FROM Attendance WHERE idEvent = ? AND idGuest = ?";
            try (PreparedStatement checkAttendanceStmt = conn.prepareStatement(checkAttendanceQuery)) {
                checkAttendanceStmt.setInt(1, eventId);
                checkAttendanceStmt.setString(2, userEmail);

                ResultSet resultSet = checkAttendanceStmt.executeQuery();
                resultSet.next();

                int existingAttendance = resultSet.getInt(1);

                if (existingAttendance > 0) {
                    System.out.println("User is already registered for the event.");
                    return false;
                }
            }

            String addAttendanceQuery = "INSERT INTO Attendance (idEvent, idGuest) VALUES (?, ?)";
            try (PreparedStatement addAttendanceStmt = conn.prepareStatement(addAttendanceQuery)) {
                addAttendanceStmt.setInt(1, eventId);
                addAttendanceStmt.setString(2, userEmail);

                int rowsAffected = addAttendanceStmt.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("User added to the event successfully.");
                } else {
                    System.out.println("Failed to add user to the event.");
                    return false;
                }
            }
            updateVersion();
        } catch (SQLException e) {
            System.out.println("Exception reported:\r\n\t..." + e.getMessage());
        }
        return true;
    }


    @Override
    public synchronized void updateEvent(int eventId, String newName, String newLocation, LocalDate newDate, String newStartTime, String newEndTime)throws RemoteException {
        String dbAddress = "jdbc:sqlite:" + dbUrl;

        try (Connection connection = DriverManager.getConnection(dbAddress)) {
            boolean hasAttendees = hasAttendees(connection, eventId);

            if (!hasAttendees) {
                String updateEventQuery = "UPDATE Events SET name=?, location=?, date=?, startTime=?, endTime=? WHERE id=?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(updateEventQuery)) {
                    preparedStatement.setString(1, newName);
                    preparedStatement.setString(2, newLocation);
                    preparedStatement.setString(3, String.valueOf(newDate));
                    preparedStatement.setString(4, newStartTime);
                    preparedStatement.setString(5, newEndTime);
                    preparedStatement.setInt(6, eventId);

                    int rowsUpdated = preparedStatement.executeUpdate();

                    System.out.println("Rows updated: " + rowsUpdated);

                    if (rowsUpdated > 0) {
                        System.out.println("Event successfully updated.");
                    } else {
                        System.out.println("No event was found with the provided ID. The update failed.");
                    }
                }
            } else {
                System.out.println("Event cannot be edited. There are already registered attendees.");
            }
            updateVersion();
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
        }
    }

    public synchronized List<Attendance> getUserAttendance(String email) {
        List<Attendance> userAttendance = new ArrayList<>();
        String dbAddress = "jdbc:sqlite:" + dbUrl;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        try (Connection conn = DriverManager.getConnection(dbAddress)) {
                    String selectUserAttendanceQuery = "SELECT Users.name AS userName, Users.email, " +
                            "Events.name AS eventName, Events.location, Events.date, Events.startTime, Events.endTime " +
                            "FROM Attendance " +
                            "JOIN Users ON Attendance.idGuest = Users.email " +
                            "JOIN Events ON Attendance.idEvent = Events.id " +
                            "WHERE Attendance.idGuest = ?";
                    try (PreparedStatement selectUserAttendanceStmt = conn.prepareStatement(selectUserAttendanceQuery)) {
                        selectUserAttendanceStmt.setString(1, email);
                        ResultSet attendanceResultSet = selectUserAttendanceStmt.executeQuery();

                        while (attendanceResultSet.next()) {
                            String userName = attendanceResultSet.getString("userName");
                            String emailFromAttendance = attendanceResultSet.getString("email");
                            String eventName = attendanceResultSet.getString("eventName");
                            String location = attendanceResultSet.getString("location");
                            LocalDate date = LocalDate.parse(attendanceResultSet.getString("date"), dateFormatter);
                            String startTime = attendanceResultSet.getString("startTime");
                            String endTime = attendanceResultSet.getString("endTime");

                            userAttendance.add(new Attendance(userName,emailFromAttendance, eventName, location, date, startTime, endTime));
                        }
                    }
        } catch (SQLException e) {
            System.out.println("Exception reported:\r\n\t..." + e.getMessage());
        }

        return userAttendance;
    }


    public synchronized List<Attendance> getEventAttendance(int eventId) throws RemoteException {
        List<Attendance> attendanceList = new ArrayList<>();

        String dbAddress = "jdbc:sqlite:" + dbUrl;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        try (Connection conn = DriverManager.getConnection(dbAddress)) {
            String selectAttendanceQuery = "SELECT Users.name AS userName, Users.email, " +
                    "Events.name AS eventName, Events.location, Events.date, Events.startTime, Events.endTime " +
                    "FROM Attendance " +
                    "JOIN Users ON Attendance.idGuest = Users.email " +
                    "JOIN Events ON Attendance.idEvent = Events.id " +
                    "WHERE Attendance.idEvent = ?";

            try (PreparedStatement selectAttendanceStmt = conn.prepareStatement(selectAttendanceQuery)) {
                selectAttendanceStmt.setInt(1, eventId);
                ResultSet attendanceResultSet = selectAttendanceStmt.executeQuery();

                while (attendanceResultSet.next()) {
                    String userName = attendanceResultSet.getString("userName");
                    String email = attendanceResultSet.getString("email");
                    String eventName = attendanceResultSet.getString("eventName");
                    String location = attendanceResultSet.getString("location");
                    LocalDate date = LocalDate.parse(attendanceResultSet.getString("date"), dateFormatter);
                    String startTime = attendanceResultSet.getString("startTime");
                    String endTime = attendanceResultSet.getString("endTime");

                    attendanceList.add(new Attendance(userName, email, eventName, location, date, startTime, endTime));
                }
            }
        } catch (SQLException e) {
            System.out.println("Exception reported:\r\n\t..." + e.getMessage());
        }

        return attendanceList;
    }


    @Override
    public synchronized Event getEvent(int eventId) throws RemoteException {
        String dbAddress = "jdbc:sqlite:" + dbUrl;

        try (Connection conn = DriverManager.getConnection(dbAddress)) {
            String selectEventQuery = "SELECT * FROM Events WHERE id = ?";
            try (PreparedStatement selectEventStmt = conn.prepareStatement(selectEventQuery)) {
                selectEventStmt.setInt(1, eventId);
                ResultSet eventResultSet = selectEventStmt.executeQuery();

                if (eventResultSet.next()) {
                    int id = eventResultSet.getInt("id");
                    String name = eventResultSet.getString("name");
                    String location = eventResultSet.getString("location");
                    String date = eventResultSet.getString("date");
                    String startTime = eventResultSet.getString("startTime");
                    String endTime = eventResultSet.getString("endTime");

                    return new Event(id, name, location, date, startTime, endTime);
                }
            }
        } catch (SQLException e) {
            System.out.println("Exception reported:\r\n\t..." + e.getMessage());
        }

        return null;
    }

    @Override
    public synchronized void createDB() throws RemoteException {
        String dbAddress = "jdbc:sqlite:" + dbUrl;

        try (Connection connection = DriverManager.getConnection(dbAddress);
             Statement statement = connection.createStatement()) {

            // USER TABLE
            statement.execute("CREATE TABLE IF NOT EXISTS Users ("
                    + "id INTEGER PRIMARY KEY,"
                    + "name VARCHAR(50),"
                    + "email VARCHAR(50) UNIQUE,"
                    + "password VARCHAR(100),"
                    + "roleId INTEGER,"
                    + "FOREIGN KEY (roleId) REFERENCES Roles(id)"
                    + ")");

            // EVENTS TABLE
            statement.execute("CREATE TABLE IF NOT EXISTS Events ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "name VARCHAR(50),"
                    + "location VARCHAR(100),"
                    + "date DATE,"
                    + "startTime DATETIME,"
                    + "endTime DATETIME"
                    + ")");

            // ATTENDANCE TABLE
            statement.execute("CREATE TABLE IF NOT EXISTS Attendance ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "idEvent INTEGER,"
                    + "idGuest VARCHAR(50),"
                    + "FOREIGN KEY (idEvent) REFERENCES Events(id),"
                    + "FOREIGN KEY (idGuest) REFERENCES Users(email)"
                    + ")");

            // ROLES TABLE
            statement.execute("CREATE TABLE IF NOT EXISTS Roles ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "roleName VARCHAR(30) UNIQUE"
                    + ")");

            // VERSION TABLE
            statement.execute("CREATE TABLE IF NOT EXISTS Version ("
                    + "id INTEGER PRIMARY KEY,"
                    + "versionNumber INTEGER"
                    + ")");

            // INITIAL INSERTS
            statement.execute("INSERT INTO Roles (roleName) VALUES ('admin')");
            statement.execute("INSERT INTO Roles (roleName) VALUES ('user')");
            statement.execute("INSERT INTO Version (versionNumber) VALUES (0)");
            statement.execute("INSERT INTO Users (name, email, password, roleId) " +
                    "VALUES ('admin', 'admin@example.com', '123', 1)");

        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }

    @Override
    public synchronized int getDbVersion() throws RemoteException {
        String dbAddress = "jdbc:sqlite:" + dbUrl;
        int versionNumber = 0;
        try (Connection connection = DriverManager.getConnection(dbAddress);
             Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery("SELECT versionNumber FROM Version");
            if (resultSet.next()) {
                versionNumber = resultSet.getInt("versionNumber");

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return versionNumber;
    }


    public synchronized void createEvent(String name, String location, LocalDate date, String startTime, String endTime) throws RemoteException {
        String dbAddress = "jdbc:sqlite:" + dbUrl;

        try (Connection connection = DriverManager.getConnection(dbAddress)) {
            String insertEventQuery = "INSERT INTO Events (name, location, date, startTime, endTime ) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertEventQuery)) {

                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

                preparedStatement.setString(1, name);
                preparedStatement.setString(2, location);
                preparedStatement.setString(3, date.format(dateFormatter));
                preparedStatement.setString(4, LocalTime.parse(startTime, timeFormatter).format(timeFormatter));
                preparedStatement.setString(5, LocalTime.parse(endTime, timeFormatter).format(timeFormatter));

                preparedStatement.executeUpdate();
            }
            updateVersion();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private synchronized boolean hasAttendees(Connection connection, int eventId) throws SQLException {

        String checkAttendeesQuery = "SELECT COUNT(*) FROM Attendance WHERE idEvent = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(checkAttendeesQuery)) {

            preparedStatement.setInt(1, eventId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        }
        return false;
    }
    private synchronized void updateVersion() {
        String dbAddress = "jdbc:sqlite:" + dbUrl;
        try (Connection connection = DriverManager.getConnection(dbAddress);
             Statement statement = connection.createStatement()) {

            statement.executeUpdate("UPDATE Version SET versionNumber = versionNumber + 1");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}



