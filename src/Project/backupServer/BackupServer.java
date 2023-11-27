package Project.backupServer;

import Project.principalServer.data.Heardbeat;
import Project.principalServer.HeardbeatObserversInterface;
import Project.principalServer.PrincipalServerInterface;

import java.io.*;
import java.net.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;

public class BackupServer extends UnicastRemoteObject implements HeardbeatObserversInterface {
    private static final String MULTICAST_ADDRESS = "230.44.44.44";
    private static final int MULTICAST_PORT = 4444;
    static String objUrl;
    static File localDirectory;
    static String filePath;
    static PrincipalServerInterface serverService;

    protected BackupServer() throws RemoteException {
    }

    public static void main(String[] args) {
        Heardbeat heardbeat;


        if (args.length != 1) {
            System.out.println("Deve passar na linha de comando: (0) o caminho da diretoria para\n" +
                    "armazenamento da uma réplica da base de dados");
            return;
        }

            localDirectory = new File(args[0]);


        /*
            RMI CALLBACK

        try {
            serverService.addBackupObserver(new BackupServer());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/


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

                    heardbeat = (Heardbeat) in.readObject();
                    objUrl = "rmi://localhost:"+heardbeat.getRegistryPort()+"/"+heardbeat.getRmiServicesName();
                    serverService = (PrincipalServerInterface) Naming.lookup(objUrl);
                    getDbBackup(localDirectory, serverService);

                } catch (ClassNotFoundException | NotBoundException e) {
                    throw new RuntimeException(e);
                }

                System.out.println("Heardbeat: ");
                System.out.println("\tDatabase version: " + heardbeat.getDbVersion() + "\n" +
                        "\tRMI Services Name: " + heardbeat.getRmiServicesName() + "\n" +
                        "\tRegistry port: " + heardbeat.getRegistryPort()
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
                //System.out.println("Backup file " + filePath + " created.");

                byte[] dbBackupFile = serverService.transferDatabase();
                fout.write(dbBackupFile);

                System.out.println("Backup updated");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void notifyNewOperation(Heardbeat hb) throws RemoteException {
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
