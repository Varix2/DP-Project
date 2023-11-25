package Project.manageDB;

import java.io.Serializable;
import java.time.LocalDate;

public class Attendance implements Serializable {
    private String userName;
    private int userId;
    private String email;
    private String eventName;
    private String location;
    private LocalDate date;
    private String startTime;
    private String endTime;

    // Constructor
    public Attendance(String userName, /*int userId,*/ String email, String eventName, String location, LocalDate date, String startTime, String endTime) {
        this.userName = userName;
        //this.userId = userId;
        this.email = email;
        this.eventName = eventName;
        this.location = location;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Constructor
    public Attendance(String userName, /*int userId,*/ String email) {
        this.userName = userName;
        this.userId = userId;
        this.email = email;
    }

    // Getters
    public String getUserName() {
        return userName;
    }

    public int getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getEventName() {
        return eventName;
    }

    public String getLocation() {
        return location;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }
}
