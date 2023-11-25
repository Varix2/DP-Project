package Project.manageDB;

import javax.naming.AuthenticationException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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


    public synchronized int insertNewUser(String name, String email, String passwd) {
        String dbAddress = "jdbc:sqlite:" + dbUrl;
        int insertState = -1;
        System.out.println(dbAddress);

        try (Connection conn = DriverManager.getConnection(dbAddress)) {
            // Utilizamos PreparedStatement para evitar la inyección SQL
            String createEntryQuery = "INSERT INTO Utilizador (name, email, Password) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(createEntryQuery)) {
                pstmt.setString(1, name);
                pstmt.setString(2, email);
                pstmt.setString(3, passwd);

                insertState = pstmt.executeUpdate();
                System.out.println("INSERT OPERATION COMPLETED. <<JUST FOR DEBUG>>");

                if (insertState < 1) {
                    System.out.println("Insertion failed");
                }
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
            String selectUserQuery = "SELECT name, email FROM Utilizador WHERE email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(selectUserQuery)) {
                pstmt.setString(1, email);
                ResultSet resultSet = pstmt.executeQuery();

                if (resultSet.next()) {
                    String username = resultSet.getString("name");
                    String storedEmail = resultSet.getString("email");

                    // Return user data as an array
                    return new String[]{username,storedEmail};
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
                    String date = resultSet.getString("date");
                    String startTime = resultSet.getString("startTime");
                    String endTime = resultSet.getString("endTime");
                    int numAssistants = resultSet.getInt("AssistantsNumber");
                    events.add(new Event(id, name, location, numAssistants,date, startTime, endTime));
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
            String checkEnrollmnetQuery = "SELECT COUNT(*) FROM Assistants a "
                                            + "INNER JOIN Events e ON a.idEvent = e.id"
                                            + "WHERE a.idGuest = ? AND ? BETWEEN e.startTime AND e.endTime";
            try (PreparedStatement checkEnrollmentStmt = conn.prepareStatement(checkEnrollmnetQuery)) {
                checkEnrollmentStmt.setString(1, email);

                //get current datetime
                Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
                checkEnrollmentStmt.setTimestamp(2, currentTimestamp);

                try (ResultSet resultSet = checkEnrollmentStmt.executeQuery()) {
                    //if the user is already enrolled in another active event
                    if (resultSet.next() && resultSet.getInt(1) > 0) {
                        System.out.println("The user " + email + " is already enrolled in an active event.");
                    } else {
                        //if the user is not enrolled in an active event
                        String insertAssistantQuery = "INSERT INTO Assistants (idEvent, idGuest) VALUES (?, ?)";
                        try (PreparedStatement insertAttendanceStmt = conn.prepareStatement(insertAssistantQuery)) {
                            insertAttendanceStmt.setInt(1, eventID);
                            insertAttendanceStmt.setString(2, email);
                            insertAttendanceStmt.executeUpdate();

                            System.out.println("The user " + email + " has join to the event number " + eventID);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Exception reported:\r\n\t..." + e.getMessage());
        }
    }

    @Override
    public List<Event> getUserEvents(String email) throws RemoteException {
        List<Event> userEvents = new ArrayList<>();
        String dbAddress = "jdbc:sqlite:" + dbUrl;

        try (Connection conn = DriverManager.getConnection(dbAddress)) {
                    // Obtener los eventos del usuario a través de los asistentes
                    String selectUserEventsQuery = "SELECT Events.*, COUNT(Assistants.idGuest) as AssistantsNumber " +
                            "FROM Events " +
                            "JOIN Assistants ON Events.id = Assistants.idEvent " +
                            "WHERE Assistants.idGuest = ? " +
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
                            int numAssistants = eventsResultSet.getInt("AssistantsNumber");
                            userEvents.add(new Event(id, name, location, numAssistants, date, startTime, endTime));
                        }
                    }
        } catch (SQLException e) {
            System.out.println("Exception reported:\r\n\t..." + e.getMessage());
        }

        return userEvents;
    }


    @Override
    public void updateUserData(String name, String email, String passwd, String oldEmail) throws RemoteException {
        String dbAddress = "jdbc:sqlite:" + dbUrl;

        try (Connection conn = DriverManager.getConnection(dbAddress)) {
            // Use a prepared statement to avoid SQL injection
            String updateUserQuery = "UPDATE Utilizador SET name=?, email=?, Password=? WHERE email=?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateUserQuery)) {
                pstmt.setString(1, name);
                pstmt.setString(2, email);
                pstmt.setString(3, passwd);
                pstmt.setString(4, oldEmail);

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

    @Override
    public synchronized void deleteEvent(int eventId) throws RemoteException {
        String dbAddress = "jdbc:sqlite:" + dbUrl;

        try (Connection connection = DriverManager.getConnection(dbAddress)) {
            // Verificar si hay asistentes para el evento
            boolean hasAssistants = hasAssistants(connection, eventId);

            // Si no hay asistentes, eliminar el evento
            if (!hasAssistants) {
                String deleteEventQuery = "DELETE FROM Events WHERE id = ?";

                try (PreparedStatement preparedStatement = connection.prepareStatement(deleteEventQuery)) {
                    // Establecer el parámetro de la consulta
                    preparedStatement.setInt(1, eventId);

                    System.out.println("Deleting event...");

                    // Ejecutar la consulta
                    int rowsAffected = preparedStatement.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("Evento eliminado");
                    } else {
                        System.out.println("Evento no encontrado para eliminar.");
                    }
                }
            } else {
                System.out.println("No se puede eliminar el evento. Hay asistentes registrados.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int deleteUserFromEvent(int eventId, String userEmail) {
        String dbAddress = "jdbc:sqlite:" + dbUrl;
        int rowsAffected = 0;
        try (Connection conn = DriverManager.getConnection(dbAddress)) {
            String deleteAttendanceQuery = "DELETE FROM Assistants WHERE idEvent = ? AND idGuest = ?";
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
        } catch (SQLException e) {
            System.out.println("Exception reported:\r\n\t..." + e.getMessage());
        }
        return rowsAffected;
    }

    public boolean addUserToEvent(int eventId, String userEmail) {
        String dbAddress = "jdbc:sqlite:" + dbUrl;
        try (Connection conn = DriverManager.getConnection(dbAddress)) {
            String checkAttendanceQuery = "SELECT COUNT(*) FROM Assistants WHERE idEvent = ? AND idGuest = ?";
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

            String addAttendanceQuery = "INSERT INTO Assistants (idEvent, idGuest) VALUES (?, ?)";
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
        } catch (SQLException e) {
            System.out.println("Exception reported:\r\n\t..." + e.getMessage());
        }
        return true;
    }


    @Override
    public void updateEvent(int eventId, String newName, String newLocation, LocalDate newDate, String newStartTime, String newEndTime)throws RemoteException {
        String dbAddress = "jdbc:sqlite:" + dbUrl;

        try (Connection connection = DriverManager.getConnection(dbAddress)) {
            // Verificar si hay asistentes para el evento
            boolean hasAssistants = hasAssistants(connection, eventId);

            // Si no hay asistentes, editar el evento
            if (!hasAssistants) {
                String updateEventQuery = "UPDATE Events SET name=?, location=?, date=?, startTime=?, endTime=? WHERE id=?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(updateEventQuery)) {
                    preparedStatement.setString(1, newName);
                    preparedStatement.setString(2, newLocation);
                    preparedStatement.setString(3, String.valueOf(newDate));
                    preparedStatement.setString(4, newStartTime);
                    preparedStatement.setString(5, newEndTime);
                    preparedStatement.setInt(6, eventId);

                    int rowsUpdated = preparedStatement.executeUpdate();

                    // Imprimir información de depuración
                    System.out.println("Rows updated: " + rowsUpdated);

                    if (rowsUpdated > 0) {
                        System.out.println("Evento actualizado exitosamente.");
                    } else {
                        System.out.println("No se encontró ningún evento con el ID proporcionado. La actualización falló.");
                    }
                }
            } else {
                System.out.println("No se puede editar el evento. Hay asistentes registrados.");
            }

        } catch (SQLException e) {
            // Manejar excepciones
            System.out.println("SQLException: " + e.getMessage());
        }
    }

    public List<Attendance> getUserAttendance(String email) {
        List<Attendance> userAttendance = new ArrayList<>();
        String dbAddress = "jdbc:sqlite:" + dbUrl;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try (Connection conn = DriverManager.getConnection(dbAddress)) {
                    // Obtener los eventos del usuario a través de los asistentes
                    String selectUserAttendanceQuery = "SELECT Utilizador.name AS userName, Utilizador.email, " +
                            "Events.name AS eventName, Events.location, Events.date, Events.startTime, Events.endTime " +
                            "FROM Assistants " +
                            "JOIN Utilizador ON Assistants.idGuest = Utilizador.email " +
                            "JOIN Events ON Assistants.idEvent = Events.id " +
                            "WHERE Assistants.idGuest = ?";
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


    public List<Attendance> getEventAttendance(int eventId) throws RemoteException {
        List<Attendance> attendanceList = new ArrayList<>();

        String dbAddress = "jdbc:sqlite:" + dbUrl;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try (Connection conn = DriverManager.getConnection(dbAddress)) {
            String selectAttendanceQuery = "SELECT Utilizador.name AS userName, Utilizador.email, " +
                    "Events.name AS eventName, Events.location, Events.date, Events.startTime, Events.endTime " +
                    "FROM Assistants " +
                    "JOIN Utilizador ON Assistants.idGuest = Utilizador.email " +
                    "JOIN Events ON Assistants.idEvent = Events.id " +
                    "WHERE Assistants.idEvent = ?";

            try (PreparedStatement selectAttendanceStmt = conn.prepareStatement(selectAttendanceQuery)) {
                selectAttendanceStmt.setInt(1, eventId);
                ResultSet attendanceResultSet = selectAttendanceStmt.executeQuery();

                while (attendanceResultSet.next()) {
                    String userName = attendanceResultSet.getString("userName");
                    int userId = attendanceResultSet.getInt("userId");
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
    public Event getEvent(int eventId) throws RemoteException {
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
    public void createDB() throws RemoteException {
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
    public synchronized void createEvent(String name, String location, LocalDate date, String startTime, String endTime) throws RemoteException {

        String dbAddress = "jdbc:sqlite:" + dbUrl;

        try (Connection connection = DriverManager.getConnection(dbAddress)) {
            // Crear la consulta de inserción
            String insertEventQuery = "INSERT INTO Events (name, location, date, startTime, endTime ) VALUES (?, ?, ?, ?, ?)";

            // Preparar la consulta
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertEventQuery)) {
                // Establecer los parámetros de la consulta
                preparedStatement.setString(1, name);
                preparedStatement.setString(2, location);
                preparedStatement.setString(3, String.valueOf(date));
                preparedStatement.setString(4, startTime);
                preparedStatement.setString(5, endTime);

                // Ejecutar la consulta
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean hasAssistants(Connection connection, int eventId) throws SQLException {
        // Verificar si hay asistentes para el evento
        String checkAssistantsQuery = "SELECT COUNT(*) FROM Assistants WHERE idEvent = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(checkAssistantsQuery)) {
            // Establecer el parámetro de la consulta
            preparedStatement.setInt(1, eventId);

            // Ejecutar la consulta y obtener el resultado
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        }
        return false;
    }


}



