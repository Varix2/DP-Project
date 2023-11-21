package Project.principalServer;

import Project.backupServer.BackupServer;
import Project.backupServer.BackupServerInterface;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PrincipalServerInterface extends Remote {

    public void pruebaRMI(String msg)throws RemoteException;
    public byte[] transferDatabase() throws  RemoteException;

    public void addBackupServer(BackupServerInterface observer)throws RemoteException;

    public void removeBackupServer(BackupServerInterface observer)throws RemoteException;
}
