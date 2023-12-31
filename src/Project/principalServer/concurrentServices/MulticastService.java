package Project.principalServer.concurrentServices;

import Project.manageDB.DbOperations;
import Project.principalServer.data.Heardbeat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;

public class MulticastService implements Runnable{

    private DbOperations dbOperations;
    private int registryPort;
    private String rmiName;
    MulticastSocket socket;

    public MulticastService(int registryPort, String rmiName,DbOperations dbOperations){
        this.dbOperations = dbOperations;
        this.registryPort = registryPort;
        this.rmiName = rmiName;
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
                int versionNumber = dbOperations.getDbVersion();
                out.writeObject(new Heardbeat(registryPort,rmiName, versionNumber));

                packet = new DatagramPacket(buff.toByteArray(), buff.size(), multicastGroup, MULTICAST_PORT);
                socket.send(packet);

                Thread.sleep(10000);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
