package Project.manageDB;

import java.io.Serializable;
import java.time.LocalDate;

public class Attendance implements Serializable {
    private String userName;
    private int userId;
    private String email;

    // Constructor
    public Attendance(String userName, int userId, String email) {
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
}
