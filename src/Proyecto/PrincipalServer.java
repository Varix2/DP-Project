package Proyecto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

class ManageDb implements Runnable{

    private final String dbAddress;
    private final String name;

    private final int idNumber;

    public ManageDb (String dbAddress, String name, int idNumber){
        this.dbAddress = dbAddress;
        this.name = name;
        this.idNumber = idNumber;
    }

    @Override
    public void run() {
        String dbUrl = "jdbc:sqlite:" + dbAddress;
        System.out.println(dbUrl);
        try(Connection conn = DriverManager.getConnection(dbUrl);
            Statement stmt = conn.createStatement()){

            String createEntryQuery = "INSERT OR REPLACE INTO Utilizador (IdNumber, Uname) VALUES " +
                    "('" + idNumber + "', '" + name + "');";


            if(stmt.executeUpdate(createEntryQuery)<1){
                System.out.println("Insertion failed");
            }


        } catch (SQLException e) {
            System.out.println("Exception reported:\r\n\t..." + e.getMessage());
        }
    }
}

public class PrincipalServer implements Runnable {

    Socket s;
    String dbUrl;
    public PrincipalServer(Socket s, String dbUrl){
        this.s = s;
        this.dbUrl = dbUrl;
    }


    public static void main(String[] args) throws IOException {
        int listeningPort;
        Socket toClient;
        Thread t, manageDb;
        String dbUrl;

        /*manageDb = new Thread(new ManageDb(args[1],"Jorge", 2),"Thread Db");
        manageDb.start();*/



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
            //Insert the data from the client into the database
            ManageDb(receivedData.getName(),receivedData.getId_number(),receivedData.getEmail(),receivedData.getPassword(),dbUrl);

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
