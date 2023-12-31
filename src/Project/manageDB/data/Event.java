package Project.manageDB.data;

import java.io.Serializable;

public class Event implements Serializable {
    private static final long serialVersionUID = 5L;
    private int id;
    private String name;
    private String location;
    private int ttendees;
    private String data;
    private String startTime;
    private String endTime;

    public Event(int id, String name, String location, int ttendees, String data, String startTime, String endTime) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.ttendees = ttendees;
        this.data = data;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    public Event(int id, String name, String location, String data, String startTime, String endTime) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.data = data;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public int getttendees() {
        return ttendees;
    }

    public String getData() {
        return data;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }
}
