package Project.principalServer;

import Project.backupServer.BackupServer;
import Project.backupServer.BackupServerInterface;
import Project.client.ClientAuthenticationData;
import Project.client.ClientRegistryData;
import Project.manageDB.DbOperations;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class PrincipalServer extends UnicastRemoteObject implements Runnable,PrincipalServerInterface {



    DbOperations dbOperations;
    Socket s;
    String dbUrl;
    final List<BackupServerInterface> backupServers;

    public PrincipalServer()throws RemoteException{
        this.backupServers = new ArrayList<>();
    }

    public PrincipalServer(Socket s, String dbUrl) throws RemoteException {
        this.s = s;
        this.dbUrl = dbUrl;
        this.dbOperations = new DbOperations(this.dbUrl);
        backupServers = new ArrayList<>();
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

        registryPort = Integer.parseInt(args[3]);
        servicioRMI ="rmi://localhost:" + registryPort + "/" + args[2];

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
            Creamos, ejecutamos y registramos servicio
         */
        try {
            PrincipalServer serverService = new PrincipalServer();
            System.out.println("Servico serverService criado e em execucao...");

            Naming.bind(servicioRMI, serverService);
            System.out.println(servicioRMI);
            System.out.println("Servico " + args[2] + " registado no registry...");

        }catch(RemoteException e){
            System.out.println("Erro remoto - " + e);
            System.exit(1);
        }catch(Exception e){
            System.out.println("Erro - " + e);
            System.exit(1);
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

    @Override
    public void pruebaRMI(String msg) throws RemoteException {
        int i;

        List<BackupServerInterface> backupServersToRemove = new ArrayList<>();

        for(BackupServerInterface backupServer:backupServers){
            try{
                backupServer.notifyNewOperation(msg);
            }catch (RemoteException e){
                backupServersToRemove.add(backupServer);
                System.out.println("- um observador (observador inaccesivel)");
            }
        }

        synchronized (backupServers){
            backupServers.removeAll(backupServersToRemove);
        }
    }

    @Override
    public void addBackupServer(BackupServerInterface observer) throws RemoteException {
        backupServers.add(observer);
    }

    @Override
    public void removeBackupServer(BackupServerInterface observer) throws RemoteException {
        backupServers.remove(observer);
    }
}
