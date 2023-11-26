package Project.principalServer;

import Project.principalServer.data.Heardbeat;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface HeardbeatObserversInterface extends Remote {

    void notifyNewOperation(Heardbeat hb) throws RemoteException;
}
