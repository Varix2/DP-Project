package Project.backupServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class BackupServer {
    private static final String MULTICAST_ADDRESS = "230.44.44.44";
    private static final int MULTICAST_PORT = 4444;

    public static void main(String[] args) {
        try {
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            MulticastSocket socket = new MulticastSocket(MULTICAST_PORT);
            socket.joinGroup(group);
            byte[] bf = new byte[1000];

            while(true) {
                DatagramPacket packet = new DatagramPacket(bf, bf.length);
                socket.receive(packet);

                String mensajeRecibido = new String(packet.getData(), 0, packet.getLength(), "UTF-8");

                System.out.println("Mensaje recibido: " + mensajeRecibido);
            }


        } catch (IOException e) {

            throw new RuntimeException(e);
        }
    }
}
