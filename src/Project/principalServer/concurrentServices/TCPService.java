package Project.principalServer.concurrentServices;

import Project.client.data.ClientAuthenticationData;
import Project.client.data.ClientRegistryData;
import Project.client.exceptions.AuthenticationErrorException;
import Project.manageDB.DbOperations;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.Socket;
import java.rmi.RemoteException;

public class TCPService implements Runnable{
    public static int MAX_SIZE = 1000;
    private Socket s;
    private String dbUrl;
    DatagramPacket pkt;
    private final DbOperations dbOperations;
    public TCPService(Socket s, String dbUrl, String localDbPath) throws RemoteException {
        this.dbUrl = dbUrl;
        this.s=s;
        this.dbOperations = new DbOperations(localDbPath);
    }
    @Override
    public void run() {

        Object receivedMsg;


        try (ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(s.getInputStream())) {

            while (true) {
                receivedMsg = in.readObject();
                if (receivedMsg instanceof ClientRegistryData regisData) {
                    System.out.println("Registration information arrive: " +
                            regisData.getName() + " / " + regisData.getEmail() + " / " + regisData.getPassword());

                    //Insert the data from the client in the database
                    int insertState = dbOperations.insertNewUser(regisData.getName(), regisData.getId_number(), regisData.getEmail(), regisData.getPassword());
                    if (insertState == 1) {
                        out.writeObject("You correctly registered");
                    } else {
                        out.writeObject("Registration fail");
                    }
                    out.flush();
                } else if(receivedMsg instanceof ClientAuthenticationData authData){
                    System.out.println("Authentication information arrive: " +
                            authData.getEmail() + " / " + authData.getPassword());
                    boolean state =dbOperations.authenticateUser(authData.getEmail(), authData.getPassword());
                    out.writeObject(state);
                    out.flush();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (AuthenticationErrorException e) {
            System.out.println(e);
        }


        }
    }

