package Project.backupServer;

import Project.principalServer.Heartbeat;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BackupServerInterface extends Remote {

    void notifyNewOperation(Heartbeat hb) throws RemoteException;
}
