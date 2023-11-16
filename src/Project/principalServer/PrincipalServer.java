package Project.principalServer;

import Project.client.ClientAuthenticationData;
import Project.client.ClientRegistryData;
import Project.manageDB.DbOperations;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class PrincipalServer implements Runnable {



    DbOperations dbOperations;
    Socket s;
    String dbUrl;


    public PrincipalServer(Socket s, String dbUrl){
        this.s = s;
        this.dbUrl = dbUrl;
        this.dbOperations = new DbOperations(this.dbUrl);
    }


    public static void main(String[] args){
        int listeningPort;
        Socket toClient;
        Thread t,mcThread;
        String dbUrl;
        int registryPort;
        String servicioRMI;

        if(args.length != 4){
            System.out.println("Deve passar na linha de comando: "
                    + "(0) um porto de escuta TCP, onde irá aguardar pela conexão de clientes "
                    + "(1) o caminho da diretoria de armazenamento da sua base de dados SQLite"
                    + "(2) o nome com o qual deve registar um serviço RMI"
                    + "(3) o porto de escuta no qual deve lançar o registry local"
            );
            System.out.println();
            return;
        }


        listeningPort = Integer.parseInt(args[0]);
        dbUrl = args[1];
        servicioRMI = args[2];
        registryPort = Integer.parseInt(args[3]);

        /*
            CREATION OF REGISTRY LOCAL
         */
        try{

            System.out.println("Tentativa de lancamento do registry no porto " +
                    registryPort + "...");

            LocateRegistry.createRegistry(registryPort);

            System.out.println("Registry lancado!");

        }catch(RemoteException e){
            System.out.println("Registry provavelmente ja' em execucao!");
        }

        /*
            START MULTICAST SERVICE
         */

        mcThread = new Thread(new MulticastService(new Heartbeat(registryPort,servicioRMI,1)));
        mcThread.start();


        try(ServerSocket psSocket = new ServerSocket(listeningPort)){
            while(true) {
                toClient = psSocket.accept();

                t = new Thread(new PrincipalServer(toClient, dbUrl), "Thread 1");
                t.start();

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
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
