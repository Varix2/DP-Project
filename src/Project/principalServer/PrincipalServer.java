package Project.principalServer;

import Project.client.ClientAuthenticationData;
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

        Object receivedMsg;

        try(ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(s.getInputStream())){

            receivedMsg = in.readObject();
            if(receivedMsg instanceof ClientRegistryData regisData){
                System.out.println("Registration information arrive: " +
                        regisData.getId_number() + " / " + regisData.getName() +
                        " / " + regisData.getEmail() + " / " + regisData.getPassword());

                //Insert the data from the client in the database
                dbOperations.insertNewUser(regisData.getName(),regisData.getId_number(),regisData.getEmail(),regisData.getPassword());
                out.writeObject("You correctly registered");
                out.flush();
            } else{
                ClientAuthenticationData authData = (ClientAuthenticationData) receivedMsg;
                System.out.println("Authentication information arrive: " +
                        authData.getEmail() + " / " + authData.getPassword());
                if (dbOperations.authenticateUser(authData.getEmail(), authData.getPassword())) {
                    out.writeObject("You correctly authenticate");
                } else {
                    out.writeObject("Authentication failed");
                }
                out.flush();
            }

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }


    }
}
