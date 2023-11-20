package Project.backupServer;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BackupServerInterface extends Remote {

    public void notifyNewOperation(String msg)throws RemoteException;
}
