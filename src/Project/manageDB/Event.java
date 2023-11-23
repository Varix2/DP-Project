package Project.manageDB;

import java.io.Serializable;

public class Event implements Serializable {
    private int id;
    private String name;
    private String location;
    private int assistants;
    private String data;
    private String startTime;
    private String endTime;

    public Event(int id, String name, String location, int assistants, String data, String startTime, String endTime) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.assistants = assistants;
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

    public int getAssistants() {
        return assistants;
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
