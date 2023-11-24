package Project.manageDB;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;

public interface DbOperationsInterface extends Remote {
    String[] getUserData(String email) throws RemoteException;
    void updateUserData(String newName, int newId, String newEmail, String newPasswd, String oldEmail) throws RemoteException;

    List<Event> getAllEvents() throws RemoteException;
    void joinAnEvent(String email, int eventID) throws RemoteException;
    List<Event> getUserEvents(String email) throws RemoteException;
    boolean authenticateUser(String email, String password) throws RemoteException;
    void createEvent(String name, String location, LocalDate date, String startTime, String endTime) throws RemoteException;
    void deleteEvent(int eventId) throws RemoteException;

    void updateEvent(int eventId, String newName, String newLocation, LocalDate newDate, String newStartTime, String newEndTime)throws RemoteException;
}
