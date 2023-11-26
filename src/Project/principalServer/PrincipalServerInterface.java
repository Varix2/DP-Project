package Project.principalServer;

import Project.principalServer.data.Heardbeat;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PrincipalServerInterface extends Remote {

    void pruebaRMI(Heardbeat hb)throws RemoteException;
    byte[] transferDatabase() throws  RemoteException;

    void addObserver(HeardbeatObserversInterface observer)throws RemoteException;

    void removeObserver(HeardbeatObserversInterface observer)throws RemoteException;
}
