package Project.backupServer;

import Project.principalServer.Heartbeat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;

public class BackupServer {
    private static final String MULTICAST_ADDRESS = "230.44.44.44";
    private static final int MULTICAST_PORT = 4444;

    public static void main(String[] args) {
        Heartbeat heartbeat;
        try{
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            MulticastSocket socket = new MulticastSocket(MULTICAST_PORT);
            socket.joinGroup(group);

            // TIMEOUT FOR HEARTBEATS
            socket.setSoTimeout(30000);

            byte[] bf = new byte[1000];

            while(true) {
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
}
