package Project.principalServer;

import Project.client.ClientRegistryData;
import Project.manageDB.DbOperations;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class PrincipalServer implements Runnable {

    DbOperations dbOperations;
    Socket s;
    String dbUrl;


    public PrincipalServer(Socket s, String dbUrl){
        this.s = s;
        this.dbUrl = dbUrl;
        this.dbOperations = new DbOperations(this.dbUrl);
    }


    public static void main(String[] args) throws IOException {
        int listeningPort;
        Socket toClient;
        Thread t;
        String dbUrl;

        listeningPort = Integer.parseInt(args[0]);
        dbUrl = args[1];

        try(ServerSocket psSocket = new ServerSocket(listeningPort)){
            while(true){
                toClient = psSocket.accept();

                t = new Thread(new PrincipalServer(toClient,dbUrl), "Thread 1");
                t.start();

            }
        }
    }

    @Override
    public void run() {

        ClientRegistryData receivedData;

        try(ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(s.getInputStream())){

            receivedData = (ClientRegistryData) in.readObject();
            System.out.println(receivedData.getName()+ receivedData.getId_number() + receivedData.getEmail() + receivedData.getPassword() + dbUrl);
            //Insert the data from the client in the database
            dbOperations.insertNewUser(receivedData.getName(),receivedData.getId_number(),receivedData.getEmail(),receivedData.getPassword());

            System.out.println("Registration information arrive: " +
                    receivedData.getId_number() + " / " + receivedData.getName() +
                    " / " + receivedData.getEmail() + " / " + receivedData.getPassword());

            out.writeObject("You correctly registered");
            out.flush();

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }


    }

    public void ManageDb(String name,int id, String email, String passwd, String dbUrl){
        String dbAddress = "jdbc:sqlite:" + dbUrl;
        System.out.println(dbAddress);
        try(Connection conn = DriverManager.getConnection(dbAddress);
            Statement stmt = conn.createStatement()){


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
