package Project.manageDB;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface DbOperationsInterface extends Remote {
    String[] getUserData(String email) throws RemoteException;
    void updateUserData(String newName, int newId, String newEmail, String newPasswd, String oldEmail) throws RemoteException;

    List<Event> getAllEvents() throws RemoteException;
    void joinAnEvent(String email, int eventID) throws RemoteException;
    boolean authenticateUser(String email, String password) throws RemoteException;
}
