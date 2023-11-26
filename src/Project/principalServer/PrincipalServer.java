package Project.principalServer;


import Project.manageDB.DbOperations;
import Project.principalServer.concurrentServices.MulticastService;
import Project.principalServer.concurrentServices.TCPService;
import Project.principalServer.data.Heardbeat;

import java.io.*;
import java.net.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class PrincipalServer extends UnicastRemoteObject implements PrincipalServerInterface {
    private static final String DBNAME = "PD-database.db";
    Socket s;
    String dbUrl;
    final List<HeardbeatObserversInterface> observers;
    private static String localDbPath;

    public PrincipalServer()throws RemoteException{
        this.observers = new ArrayList<>();

    }
    public static void main(String[] args){
        int listeningPort;
        Socket toClient;
        Thread tcp,mcThread;
        File dbDirectory;
        int registryPort;
        String servicioRMI,servicioRMIDatabase;


        if(args.length != 4){
            System.out.println("You must pass on the command line : "
                    + "(0) a TCP listening port, which will wait for the connecting clients "
                    + "(1) the storage directory path of your SQLite database "
                    + "(2) the name under which you must register an RMI service"
                    + "(3) the listening port on which to launch the local registry."
            );
            System.out.println();
            return;
        }


        listeningPort = Integer.parseInt(args[0]);
        dbDirectory = new File(args[1]);
        registryPort = Integer.parseInt(args[3]);
        servicioRMI ="rmi://localhost:" + registryPort + "/" + args[2];
        servicioRMIDatabase = "rmi://localhost:2000/DB-service";


        if(!dbDirectory.exists()){
            System.out.println("The directory " + dbDirectory + " does not exist!");
            return;
        }

        if(!dbDirectory.isDirectory()){
            System.out.println("The path " + dbDirectory + " does not refer to a directory!");
            return;
        }
        if(!dbDirectory.canWrite()){
            System.out.println("No writing permissions for the directory " + dbDirectory);
            return;
        }
        try{
            localDbPath = new File(dbDirectory.getPath() + File.separator + DBNAME).getCanonicalPath();
        }catch(IOException ex){
            System.out.println(ex);
            return;
        }
        /*
            CREATION OF REGISTRY LOCAL
         */
        try{

            System.out.println("Attempting to launch the registry in port " +
                    registryPort + "...");

            LocateRegistry.createRegistry(registryPort);
            LocateRegistry.createRegistry(2000);

            System.out.println("Registry launched!");

        }catch(RemoteException e){
            System.out.println("Registry probably already running!");
        }





        /*
            Creamos, ejecutamos y registramos servicio
         */
        try {
            PrincipalServer serverService = new PrincipalServer();
            System.out.println("Service <<serverService>> created and executed...");

            Naming.bind(servicioRMI, serverService);

            System.out.println("Service " + args[2] + " registered...");

        }catch(RemoteException e){
            System.out.println("Remote error - " + e);
            System.exit(1);
        }catch(Exception e){
            System.out.println("Error - " + e);
            System.exit(1);
        }

        /*
            Servicio Database
         */
        DbOperations dbService = null;
        try {
            dbService = new DbOperations(localDbPath);
            System.out.println("Service <<dbService>> created and executed...");
            // Registra la instancia en el registro RMI
            Naming.rebind(servicioRMIDatabase, dbService);

            //Check if db exists
            File db = new File(localDbPath);
            if(!db.exists()){
                System.out.println("The db does not exist. Creating...");
                dbService.createDB();
            }
            System.out.println("Connection established with database");
        } catch (RemoteException | MalformedURLException e) {
            throw new RuntimeException(e);
        }

        /*
            START MULTICAST SERVICE
         */
        int dbVersion;
        try {
            dbVersion = dbService.getDbVersion();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        mcThread = new Thread(new MulticastService(registryPort,servicioRMI,dbService));
        mcThread.start();

        /*
            ACCEPT CLIENT REQUEST
         */

        try(ServerSocket psSocket = new ServerSocket(listeningPort)){
            int i = 0;
            System.out.println();
            while(true) {
                toClient = psSocket.accept();

                tcp = new Thread(new TCPService(toClient,servicioRMI, localDbPath), "Client"+ ++i);
                tcp.start();

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void pruebaRMI(Heardbeat hb) throws RemoteException {
        int i;

        List<HeardbeatObserversInterface> backupServersToRemove = new ArrayList<>();

        for(HeardbeatObserversInterface backupServer:observers){
            try{
                backupServer.notifyNewOperation(hb);
            }catch (RemoteException e){
                backupServersToRemove.add(backupServer);
                System.out.println("- um observador (observador inaccesivel)");
            }
        }

        synchronized (observers){
            observers.removeAll(backupServersToRemove);
        }
    }

    @Override
    public synchronized byte[] transferDatabase() throws RemoteException {
        byte[] fileChunk = null;
        try (FileInputStream fin = new FileInputStream(localDbPath);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096]; // Ajusta el tamaño del búfer según sea necesario
            int bytesRead;

            while ((bytesRead = fin.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }

            fileChunk = bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileChunk;
    }


    @Override
    public void addObserver(HeardbeatObserversInterface observer) throws RemoteException {
        synchronized (observers){
            if(!observers.contains(observer)) {
                observers.add(observer);
                System.out.println("+ um observador");
            }
        }
    }

    @Override
    public void removeObserver(HeardbeatObserversInterface observer) throws RemoteException {
        synchronized (observers){
            if(observers.remove(observer)){
                System.out.println("- um observador");
            }
        }
    }
}
