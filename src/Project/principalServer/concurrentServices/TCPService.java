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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TCPService implements Runnable {
    public static int MAX_SIZE = 1000;
    private final Socket s;
    private final String dbUrl;
    int rmiPort;
    String RMIname;
    private final DbOperations dbOperations;
    private final ObjectOutputStream out;
    public static List<TCPService> clients = new ArrayList<>();
    private static List<ObjectOutputStream> clientsOutput = new ArrayList<>();

    public TCPService(Socket s, String dbUrl, String localDbPath,int rmiPort,String RMIname) throws RemoteException {
        this.dbUrl = dbUrl;
        this.RMIname = RMIname;
        this.rmiPort = rmiPort;
        clients.add(this);
        this.s = s;
        this.dbOperations = new DbOperations(localDbPath,rmiPort,RMIname);
        try {
            out = new ObjectOutputStream(s.getOutputStream());
            clientsOutput.add(out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void notifyForUpdate(String message) {
        for (ObjectOutputStream clientOut : clientsOutput) {
            try {
                clientOut.writeObject(message);
                clientOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {


        Object receivedMsg;


        try (ObjectInputStream in = new ObjectInputStream(s.getInputStream())) {

            while (true) {
                receivedMsg = in.readObject();
                if (receivedMsg instanceof ClientRegistryData regisData) {
                    System.out.println("New registry: \n" +
                            "Name: " +  regisData.getName() +"\n"+
                            "Identification: " +  regisData.getId_number() +"\n"+
                            "Email: " +  regisData.getEmail());

                    int insertState = dbOperations.insertNewUser(regisData.getName(), regisData.getId_number(), regisData.getEmail(), regisData.getPassword());
                    if (insertState == 1) {
                        out.writeObject("You correctly registered");
                    } else {
                        out.writeObject("Registration fail");
                    }
                    out.flush();
                } else if (receivedMsg instanceof ClientAuthenticationData authData) {
                    System.out.println("The user: " + authData.getEmail() + "has logged in to the system");
                    boolean state = dbOperations.authenticateUser(authData.getEmail(), authData.getPassword());
                    out.writeObject(state);
                    out.flush();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (AuthenticationErrorException e) {
            System.out.println(e);
        } finally {

            clients.remove(this);
            clientsOutput.remove(out);
            try {
                if (out != null) {
                    out.close();
                }
                if (s != null && !s.isClosed()) {
                    s.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }
}

