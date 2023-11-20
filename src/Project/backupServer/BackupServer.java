package Project.backupServer;

import Project.principalServer.Heartbeat;
import Project.principalServer.PrincipalServerInterface;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class BackupServer extends UnicastRemoteObject implements BackupServerInterface{
    private static final String MULTICAST_ADDRESS = "230.44.44.44";
    private static final int MULTICAST_PORT = 4444;

    protected BackupServer() throws RemoteException {
    }

    public static void main(String[] args) {
        Heartbeat heartbeat;
        PrincipalServerInterface serverService;

        /*
            RMI CALLBACK
         */

        try {
            String objUrl = "rmi://localhost:4444/p1";
            serverService = (PrincipalServerInterface) Naming.lookup(objUrl);

            BackupServer observer = new BackupServer();
            System.out.println("Servicio serverBU creado y en ejecucion");

            serverService.addBackupServer(new BackupServer());

            System.out.println("<Enter> para terminar");
            System.out.println();
            System.in.read();

            serverService.removeBackupServer(observer);
            UnicastRemoteObject.unexportObject(observer, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }

        try {
            serverService.pruebaRMI("OJSHKJHDKHDKKHDHKHDGHKDGHJD");
        } catch (RemoteException e) {
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

    @Override
    public void notifyNewOperation(String msg) {
    System.out.println(msg);
    }
}
