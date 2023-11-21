package Project.principalServer;

import Project.backupServer.BackupServerInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PrincipalServerInterface extends Remote {

    void pruebaRMI(String msg)throws RemoteException;
    byte[] transferDatabase() throws  RemoteException;

    void addBackupServer(BackupServerInterface observer)throws RemoteException;

    void removeBackupServer(BackupServerInterface observer)throws RemoteException;
}
