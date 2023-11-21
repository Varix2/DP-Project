package Project.principalServer;


import Project.backupServer.BackupServerInterface;

import Project.manageDB.DbOperations;

import java.io.*;
import java.net.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class PrincipalServer extends UnicastRemoteObject implements PrincipalServerInterface {
    private final String DBNAME = "javaProgramming.db";
    DbOperations dbOperations;
    Socket s;
    String dbUrl;
    final List<BackupServerInterface> backupServers;

    public PrincipalServer()throws RemoteException{
        this.backupServers = new ArrayList<>();

    }
    public static void main(String[] args){
        int listeningPort;
        Socket toClient;
        Thread tcp,mcThread;
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
            System.out.println("Servico <<serverService>> criado e em execucao...");

            Naming.bind(servicioRMI, serverService);

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

        /*
            ACCEPT CLIENT REQUEST
         */

        try(ServerSocket psSocket = new ServerSocket(listeningPort)){
            while(true) {
                toClient = psSocket.accept();

                tcp = new Thread(new TCPService(toClient, dbUrl), "Thread 1");
                tcp.start();

            }
        } catch (IOException e) {
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
    public byte[] transferDatabase() throws RemoteException {
        String requestedCanonicalFilePath = null;
        byte [] fileChunk = null;
        File localDirectory = new File("C:/Users/Varix/IdeaProjects/PracticalProyect");

        try {
            requestedCanonicalFilePath = new File(localDirectory+File.separator+DBNAME).getCanonicalPath();


            if(!requestedCanonicalFilePath.startsWith(localDirectory.getCanonicalPath()+File.separator)){
                System.out.println("Nao e' permitido aceder ao ficheiro " + requestedCanonicalFilePath + "!");
                System.out.println("A directoria de base nao corresponde a " + localDirectory.getCanonicalPath()+"!");
            }

            //BLOCK ANY OPERATION TO THE DATABASE
            DbOperations db = DbOperations.getInstance(dbUrl);
            synchronized (db) {
                try (FileInputStream fin = new FileInputStream(requestedCanonicalFilePath);
                     ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

                    byte[] buffer = new byte[4096]; // Adjust the buffer size as needed
                    int bytesRead;

                    while ((bytesRead = fin.read(buffer)) != -1) {
                        bos.write(buffer, 0, bytesRead);
                    }

                    fileChunk = bos.toByteArray();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileChunk;
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
