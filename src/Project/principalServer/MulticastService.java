package Project.principalServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;

public class MulticastService implements Runnable{

    private final Heartbeat heartbeat;
    MulticastSocket socket;

    public MulticastService(Heartbeat heartbeat){
        this.heartbeat = heartbeat;
        try {
            socket = new MulticastSocket();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        DatagramPacket packet;
        InetAddress multicastGroup;
        String MULTICAST_ADDRESS = "230.44.44.44";
        int MULTICAST_PORT = 4444;
        try {

            multicastGroup = InetAddress.getByName(MULTICAST_ADDRESS);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        while(true) {
            //SERIALISATION
            try(ByteArrayOutputStream buff = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(buff)) {

                out.writeObject(heartbeat);

                packet = new DatagramPacket(buff.toByteArray(), buff.size(), multicastGroup, MULTICAST_PORT);
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
