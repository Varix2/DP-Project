package Project.client.ui;

import Project.manageDB.data.Attendance;
import Project.manageDB.DbOperationsInterface;
import Project.manageDB.data.Event;

import java.io.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class AdminMenus {

    private final DbOperationsInterface dbOperations;
    public AdminMenus() {
        try {
            dbOperations = (DbOperationsInterface) Naming.lookup("rmi://localhost:2000/DB-service");
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public int showProfile() {
        clearConsole();
        int option;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));


            System.out.println("----------------------------");
            System.out.println("Main Menu");
            System.out.println("1. Create event");
            System.out.println("2. Edit event");
            System.out.println("3. Delete event");
            System.out.println("4. Consult events");
            System.out.println("5. Consult attendance to an event");
            System.out.println("6. Obtain a CSV file of the attendance (from last point)");
            System.out.println("7. Consult specific user attendances");
            System.out.println("8. Obtain a CSV file of user events");
            System.out.println("9. Delete an attendance");
            System.out.println("10. Create an attendance");
            System.out.println("11. Exit");
            do {
                System.out.println("----------------------------");
                System.out.print("Choose an option: ");

                option = Integer.parseInt(reader.readLine());

                if (option < 1 || option > 11) {
                    System.out.println("ENTER A VALID OPTION");
                }
            } while (option < 1 || option > 11);

            switch (option) {
                case 1:
                    createEvent();
                    break;
                case 2:
                    editEvent();
                    break;
                case 3:
                    deleteEvent();
                    break;
                case 4:
                    consultEvents();
                    break;
                case 5:
                    consultEventAttendance();
                    break;
                case 6:
                    obtainCsvEventAttendance();
                    break;
                case 7:
                    consultUserEvents();
                    break;
                case 8:
                    obtainCsvUserEvents();
                    break;
                case 9:
                    deleteAttendance();
                    break;
                case 10:
                    addAttendance();
                    break;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return option;
    }

    private void addAttendance() {

        String email;
        int eventId;
        boolean addState;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            clearConsole();
            System.out.println("To add an attendance enter the email of the user and the id event: ");
            System.out.println("---------------------------------------------------");
            System.out.print("Email:");
            email = reader.readLine();
            System.out.print("Event id:");
            eventId = Integer.parseInt(reader.readLine());
            try {
               addState = dbOperations.addUserToEvent(eventId,email);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            if (addState) {
                System.out.println("Successfully added.");
            } else {
                System.out.println("Something went wrong");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        waitToUser();
    }

    private void deleteAttendance() {
        String email;
        int eventId, deleteState;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            clearConsole();
            System.out.println("To remove an attendance enter the email of the user and the id event: ");
            System.out.println("---------------------------------------------------");
            System.out.print("Email:");
            email = reader.readLine();
            System.out.print("Event id:");
            eventId = Integer.parseInt(reader.readLine());
            try {
                deleteState = dbOperations.deleteUserFromEvent(eventId,email);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            if (deleteState > 0) {
                System.out.println("User removed from the event successfully.");
            } else {
                System.out.println("User is not registered for the event.");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        waitToUser();
    }


    private void obtainCsvUserEvents() {
        List<Attendance> attendance;
        String email;
        String [] userData;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            clearConsole();
            System.out.println("Enter the email of the user to download their events: ");
            System.out.println("---------------------------------------------------");
            email = reader.readLine();
            System.out.println("Creating file for " + email + "...");

            try {
                attendance = dbOperations.getUserAttendance(email);
                userData = dbOperations.getUserData(email);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String projectDirectory = System.getProperty("user.dir");
        String filePath = projectDirectory + File.separator + email + "-Events.csv";
        System.out.println("The .csv file will be located at " + filePath);

        try (FileWriter writer = new FileWriter(filePath)) {
            //User data
            writer.append("Nome;Número identificação;Email;\n");
            writer.append(String.format("%s;%s;%s;\n",
                    userData[0], userData[1], userData[2]));
            writer.append(";;;\n");


            //Events
            writer.append("Designação;Local;Data;Hora Inicio;Hora fim;\n");
            for (Attendance a : attendance) {
                writer.append(String.format("%s;%s;%s;%s;%s\n",
                        a.getEventName(), a.getLocation(), a.getDate(), a.getStartTime(), a.getEndTime()));

            }
        } catch (IOException e) {
            System.err.println("Error al generar el archivo CSV: " + e.getMessage());
        }
        waitToUser();
    }
    private void consultUserEvents() {
        List<Attendance> attendance;
        String [] userData;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            clearConsole();
            System.out.println("Enter the email of the user to consult their events: ");
            System.out.println("---------------------------------------------------");
            String email = reader.readLine();
            try {
                userData = dbOperations.getUserData(email);
                attendance = dbOperations.getUserAttendance(email);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (attendance.isEmpty()) {
            System.out.println("No events records found for this user.");
        } else {
           userEventsDisplay(userData,attendance);
        }
        waitToUser();

    }

    private void obtainCsvEventAttendance() {
        Event event;
        List<Attendance> attendance;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            clearConsole();
            System.out.println("Enter the ID of the event you want to download the attendance: ");
            System.out.println("---------------------------------------------------");
            int eventId = Integer.parseInt(reader.readLine());
            System.out.println("Creating file attendance for the event " + eventId + ":");

            try {
                attendance = dbOperations.getEventAttendance(eventId);
                event = dbOperations.getEvent(eventId);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String projectDirectory = System.getProperty("user.dir");
        String filePath = projectDirectory + File.separator + "attendance.csv";
        System.out.println("The .csv file will be located at "+ filePath);
        
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.append(String.format("Designação;%s;\n" +
                            "Local;%s;\n" +
                            "Data;%s;\n" +
                            "Hora Inicio;%s;\n" +
                            "Hora fim;%s;\n",
                    event.getName(), event.getLocation(), event.getData(), event.getStartTime(), event.getEndTime()));

            writer.append(";;\nNome;Número identificação;Email\n");
            for (Attendance a : attendance) {
                writer.append(String.format("\"%s\";\"%d\";\"%s\"\n",
                        a.getUserName(), a.getUserId(), a.getEmail()));
            }
        } catch (IOException e) {
            System.err.println("Error al generar el archivo CSV: " + e.getMessage());
        }
        waitToUser();
    }

    private void consultEventAttendance() {
        List<Attendance> attendance;
        Event event;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            clearConsole();
            getAllEvents();
            System.out.println("Enter the ID of the event to consult attendance: ");
            System.out.println("---------------------------------------------------");
            int eventId = Integer.parseInt(reader.readLine());
            System.out.println("Attendance for Event ID " + eventId + ":");

            try {
                attendance = dbOperations.getEventAttendance(eventId);
                event = dbOperations.getEvent(eventId);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (attendance.isEmpty()) {
            System.out.println("No attendance records found for this event.");
        } else {
            attendanceDisplay(event,attendance);
        }
        waitToUser();

    }

    private void editEvent() {
        int eventId;
        String newName, newLocation, newStartTime, newEndTime;
        LocalDate newDate;
        getAllEvents();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            clearConsole();
            System.out.println("Enter the ID of the event you want to edit: ");
            System.out.println("---------------------------------------------------");
            eventId = Integer.parseInt(reader.readLine());

            System.out.print("New event name: ");
            newName = reader.readLine();

            System.out.print("New event location: ");
            newLocation = reader.readLine();

            System.out.print("New event date (XX-XX-XXXX): ");
            newDate = LocalDate.parse(reader.readLine(), dateFormatter);

            System.out.print("New event start time: ");
            newStartTime = reader.readLine();

            System.out.print("New event end time: ");
            newEndTime = reader.readLine();

            try {
                dbOperations.updateEvent(eventId, newName, newLocation, newDate, newStartTime, newEndTime);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            waitToUser();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }


    public void deleteEvent(){
        int eId;
        getAllEvents();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            clearConsole();
            System.out.println("Select the id of the event you want to delete: ");
            System.out.println("---------------------------------------------------");
            eId = Integer.parseInt(reader.readLine());


            try {
                dbOperations.deleteEvent(eId);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            waitToUser();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public void createEvent(){
        String eName, eLocation, eStartTime,eEndTime;
        LocalDate eDate;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            clearConsole();
            System.out.println("Creating a new event:");
            System.out.println("---------------------------------------------------");

            System.out.print("Event name: ");
            eName = reader.readLine();

            System.out.print("Event location: ");
            eLocation = reader.readLine();

            System.out.print("Event date (XX-XX-XXXX): ");
            eDate = LocalDate.parse(reader.readLine(),dateFormatter);

            System.out.print("Event start time: ");
            eStartTime = reader.readLine();

            System.out.print("Event end time: ");
            eEndTime = reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            dbOperations.createEvent(eName,eLocation,eDate,eStartTime,eEndTime);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        waitToUser();
    }

    private void userEventsDisplay(String[]userData, List<Attendance> attendance){
        System.out.println("User:");
        System.out.format("%-5s%-10s%-15s\n", "ID", "NAME", "EMAIL");
        System.out.println("----------------------------------------------------------------------------------");
        System.out.format("%-5s%-10s%-15s\n",
                userData[1], userData[0], userData[2]);
        System.out.println("----------------------------------------------------------------------------------");

        System.out.println("\nEvents Table:");
        System.out.format("%-10s%-15s%-15s%-15s%-15s\n", "NAME", "LOCATION", "DATA", "START TIME", "END TIME");
        System.out.println("----------------------------------------------------------------------------------");
        for (Attendance event : attendance) {
            System.out.format("%-10s%-15s%-17s%-14s%-15s\n", event.getEventName(), event.getLocation(),
                    event.getDate(), event.getStartTime(), event.getEndTime());
        }
        System.out.println("----------------------------------------------------------------------------------");


    }
    private void attendanceDisplay(Event event, List<Attendance> attendance) {
        System.out.println("Events Table:");
        System.out.format("%-5s%-10s%-15s%-15s%-15s%-15s\n", "ID", "NAME", "LOCATION", "DATA", "START TIME", "END TIME");
        System.out.println("----------------------------------------------------------------------------------");
        System.out.format("%-5d%-10s%-15s%-17s%-14s%-15s\n", event.getId(), event.getName(), event.getLocation(),
                event.getData(), event.getStartTime(), event.getEndTime());
        System.out.println("----------------------------------------------------------------------------------");

        System.out.println("\nAttendance Table:");
        System.out.format("%-5s%-10s%-15s\n", "ID", "NAME", "EMAIL");
        System.out.println("----------------------------------------------------------------------------------");

        for (Attendance Attendee : attendance) {
            System.out.format("%-5d%-10s%-15s\n",
                    Attendee.getUserId(), Attendee.getUserName(), Attendee.getEmail());
        }
        System.out.println("---------------------------------------------------");
    }

    public void consultEvents() {
        List<Event> events;
        clearConsole();
        try {
            events = dbOperations.getAllEvents();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        eventsDisplay(events);
        waitToUser();
    }

    private void getAllEvents(){
        List<Event> events;
        try {
            events = dbOperations.getAllEvents();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        eventsDisplay(events);
    }

    private void eventsDisplay(List<Event> events){
        System.out.println("Events table:");
        System.out.format("%-5s%-10s%-15s%-15s%-15s%-15s%-15s\n", "ID", "NAME", "LOCATION", "DATA", "START TIME", "END TIME", "ASSISTANCE");
        System.out.println("------------------------------------------------------------------------------------------");

        for (Event event : events) {
            System.out.format("%-5d%-10s%-15s%-17s%-14s%-15s%-17d\n",
                    event.getId(), event.getName(), event.getLocation(),
                    event.getData(), event.getStartTime(), event.getEndTime(), event.getttendees());
        }
        System.out.println("------------------------------------------------------------------------------------------");
    }
    private void clearConsole(){
        try {
            new ProcessBuilder("clear").inheritIO().start().waitFor();
        } catch (Exception e) {
        }
    }
    private void waitToUser() {
        System.out.println("\nPress Enter to go back");
        try {
            new Scanner(System.in).nextLine();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
