package Project.principalServer;

import Project.client.ClientAuthenticationData;
import Project.client.ClientRegistryData;
import Project.manageDB.DbOperations;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.RemoteException;

public class TCPService implements Runnable{

    private Socket s;
    private DbOperations dbOperations;
    public TCPService(Socket s, String dbUrl) throws RemoteException {
        this.s=s;
        this.dbOperations = new DbOperations(dbUrl);
    }
    @Override
    public void run() {

        Object receivedMsg;

        try(ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(s.getInputStream())){


            receivedMsg = in.readObject();
            if(receivedMsg instanceof ClientRegistryData regisData){
                System.out.println("Registration information arrive: " +
                        regisData.getName() + " / " + regisData.getEmail() + " / " + regisData.getPassword());

                //Insert the data from the client in the database
                int insertState = dbOperations.insertNewUser(regisData.getName(),regisData.getId_number(),regisData.getEmail(),regisData.getPassword());
                if(insertState == 1){
                    out.writeObject("You correctly registered");
                } else {
                    out.writeObject("Registration fail");
                }
                out.flush();
            } else{
                ClientAuthenticationData authData = (ClientAuthenticationData) receivedMsg;
                System.out.println("Authentication information arrive: " +
                        authData.getEmail() + " / " + authData.getPassword());
                dbOperations.authenticateUser(authData.getEmail(), authData.getPassword());
                out.writeObject("You correctly authenticate");
                out.flush();
            }

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }


    }
}
