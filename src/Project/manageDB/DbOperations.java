package Project.manageDB;

import java.sql.*;

public class DbOperations {

    private final String dbUrl;
    public DbOperations(String url){
        this.dbUrl = url;
    }

    public void insertNewUser(String name,int id, String email, String passwd){
        String dbAddress = "jdbc:sqlite:" + dbUrl;
        System.out.println(dbAddress);

        try(Connection conn = DriverManager.getConnection(dbAddress);
            Statement stmt = conn.createStatement()){

            //IT IS BETTER TO USE SOMETHING CALL "preparedStatement" but I barely know how that works
            String createEntryQuery = "INSERT OR REPLACE INTO Utilizador (IdNumber, Uname, Email, Password) VALUES " +
                    "('" + id + "', '" + name + "', '" + email + "', '" + passwd + "');";

            if(stmt.executeUpdate(createEntryQuery)<1){
                System.out.println("Insertion failed");
            }


        } catch (SQLException e) {
            System.out.println("Exception reported:\r\n\t..." + e.getMessage());
        }
    }

    public boolean authenticateUser(String email, String password) {
        String dbAddress = "jdbc:sqlite:" + dbUrl;

        try (Connection conn = DriverManager.getConnection(dbAddress)) {
            String selectUserQuery = "SELECT Email, Password FROM Utilizador WHERE Email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(selectUserQuery)) {
                pstmt.setString(1, email);
                ResultSet resultSet = pstmt.executeQuery();

                if (resultSet.next()) {
                    String storedEmail = resultSet.getString("Email");
                    String storedPassword = resultSet.getString("Password");
                    // Compara el email y la contraseña proporcionados
                    if (email.equals(storedEmail) && password.equals(storedPassword)) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Exception reported:\r\n\t..." + e.getMessage());
        }

        return false;
    }
}