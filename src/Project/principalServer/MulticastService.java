package Project.principalServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastService implements Runnable{

    private final String MULTICAST_ADDRESS = "230.44.44.44";
    private final int MULTICAST_PORT = 4444;
    private String heartbeat;
    private String dbVersion;

    public MulticastService(String heartbeat, String dbVersion) {
        this.heartbeat = heartbeat;
        this.dbVersion = dbVersion;
    }

    @Override
    public void run() {
        while(true) {
            try (MulticastSocket socket = new MulticastSocket()) {
                InetAddress multicastGroup = InetAddress.getByName(MULTICAST_ADDRESS);
                DatagramPacket packet = new DatagramPacket(
                        heartbeat.getBytes(),
                        heartbeat.length(),
                        multicastGroup,
                        MULTICAST_PORT
                );
                socket.send(packet);

                System.out.println("Heartbeat message sent.");
                Thread.sleep(10000);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
