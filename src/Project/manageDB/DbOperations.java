package Project.manageDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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
}
