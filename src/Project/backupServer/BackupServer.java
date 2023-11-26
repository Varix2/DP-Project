package Project.backupServer;

import Project.principalServer.Heartbeat;
import Project.principalServer.PrincipalServerInterface;

import java.io.*;
import java.net.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;

public class BackupServer extends UnicastRemoteObject implements BackupServerInterface {
    private static final String MULTICAST_ADDRESS = "230.44.44.44";
    private static final int MULTICAST_PORT = 4444;
    static String objUrl = "rmi://localhost:4444/p1";
    static File localDirectory;
    static String filePath;

    protected BackupServer() throws RemoteException {
    }

    public static void main(String[] args) {
        Heartbeat heartbeat;
        PrincipalServerInterface serverService;
        /*
            OPTAINING THE DATABASE BACKUP
         */
        try {
            serverService = (PrincipalServerInterface) Naming.lookup(objUrl);
            localDirectory = new File(args[0]);

            getDbBackup(localDirectory, serverService);
        } catch (MalformedURLException | NotBoundException | RemoteException ex) {
            throw new RuntimeException(ex);
        }



        /*
            RMI CALLBACK
         */
        try {
            BackupServer observer = new BackupServer();
            System.out.println("Servicio <<backupServer>> creado y en ejecucion");

            serverService.addBackupServer(new BackupServer());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        try {
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            MulticastSocket socket = new MulticastSocket(MULTICAST_PORT);
            socket.joinGroup(group);

            // TIMEOUT FOR HEARTBEATS
            socket.setSoTimeout(30000);

            byte[] bf = new byte[1000];

            while (true) {
                // SERIALISATION
                DatagramPacket packet = new DatagramPacket(bf, bf.length);
                socket.receive(packet);

                try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(packet.getData(), 0, packet.getLength()))) {

                    heartbeat = (Heartbeat) in.readObject();

                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }

                System.out.println("Heartbeat: ");
                System.out.println("\tDatabase version: " + heartbeat.getDbVersion() + "\n" +
                        "\tRMI Services Name: " + heartbeat.getRmiServicesName() + "\n" +
                        "\tRegistry port: " + heartbeat.getRegistryPort()
                );
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized static void getDbBackup(File localDirectory, PrincipalServerInterface serverService) {

        String fileName = "backupFile.db";
        try {
            filePath = new File(localDirectory.getPath() + File.separator + fileName).getCanonicalPath();
            try (FileOutputStream fout = new FileOutputStream(filePath)) {
                System.out.println("BACKUP FILE " + filePath + " CREATED.");

                byte[] dbBackupFile = serverService.transferDatabase();
                fout.write(dbBackupFile);

                System.out.println("BACKUP (" + fileName + ") TRANSFER COMPLETE.");
            }
            System.out.println("Backup made successfully");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void notifyNewOperation(Heartbeat hb) throws RemoteException {
        try {
            String objUrl = "rmi://localhost:" + hb.getRegistryPort() + "/" + hb.getRmiServicesName();
            PrincipalServerInterface serverService = (PrincipalServerInterface) Naming.lookup(objUrl);
            if(hb.getDbVersion() == getDbVersion()){
                getDbBackup(localDirectory, serverService);
            }


        } catch (MalformedURLException | NotBoundException e) {
            throw new RuntimeException(e);
        }

    }

    public static synchronized int getDbVersion() throws RemoteException {
        String dbAddress = "jdbc:sqlite:" + filePath;
        int versionNumber = 0;
        try (Connection connection = DriverManager.getConnection(dbAddress);
             Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery("SELECT versionNumber FROM Version");
            if (resultSet.next()) {
                versionNumber = resultSet.getInt("versionNumber");

            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return versionNumber;
    }
}
