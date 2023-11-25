package Project.client.ui;

import Project.manageDB.Attendance;
import Project.manageDB.DbOperationsInterface;
import Project.manageDB.Event;

import java.io.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminMenus {

    private final DbOperationsInterface dbOperations;
    public AdminMenus() {
        try {
            dbOperations = (DbOperationsInterface) Naming.lookup("rmi://localhost:2000/DB-service");
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void showProfile() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            int option;
            System.out.println("\n********************");
            System.out.println("Main Menu");
            System.out.println("1. Create event");
            System.out.println("2. Edit event");
            System.out.println("3. Delete event");
            System.out.println("4. Consult events");
            System.out.println("5. Generation of a code to record attendance at an event that is currently taking place.");
            System.out.println("6. Consult attendance to an event");
            System.out.println("7. Obtain a CSV file of the attendance (from last point)");
            System.out.println("8. Consult specific user attendances");
            System.out.println("9. Obtain a CSV file of user events");
            System.out.println("10. Delete an attendance");
            System.out.println("11. Create an attendance");
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
                case 6:
                    consultEventAttendance();
                    break;
                case 7:
                    obtainCsvEventAttendance();
                    break;
                case 8:
                    consultUserEvents();
                    break;
                case 9:
                    obtainCsvUserEvents();
                    break;
                case 10:
                    deleteAttendance();
                    break;
                case 11:
                    addAttendance();
                    break;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addAttendance() {
        String email;
        int eventId;
        boolean addState;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("To add an attendance enter the email of the user and the id event: ");
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
                System.out.println("User added to the event successfully.");
            } else {
                System.out.println("Something went wrong, check if the user is already registered");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteAttendance() {
        String email;
        int eventId, deleteState;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("To remove an attendance enter the email of the user and the id event: ");
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
    }


    private void obtainCsvUserEvents() {
        List<Attendance> attendance;
        String email;
        String [] userData;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Enter the email of the user to download their events: ");
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
        System.out.println("The .csv file will be located at " + projectDirectory);
        String filePath = projectDirectory + File.separator + email + "-Events.csv";

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
    }
    private void consultUserEvents() {
        List<Attendance> attendance;
        String [] userData;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            // Solicitar al usuario que ingrese el ID del evento para consultar la asistencia
            System.out.println("Enter the email of the user to consult their events: ");
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

    }

    private void obtainCsvEventAttendance() {
        Event event;
        List<Attendance> attendance;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            // Solicitar al usuario que ingrese el ID del evento para consultar la asistencia
            System.out.println("Enter the ID of the event you want to download the attendance: ");
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
        System.out.println("The .csv file will be located at "+ projectDirectory);
        String filePath = projectDirectory + File.separator + "attendance.csv";
        
        try (FileWriter writer = new FileWriter(filePath)) {
            // Escribir encabezados en el archivo CSV
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
    }

    private void consultEventAttendance() {
        List<Attendance> attendance;
        Event event;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            // Solicitar al usuario que ingrese el ID del evento para consultar la asistencia
            System.out.println("Enter the ID of the event to consult attendance: ");
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

    }

    private void editEvent() {
        int eventId;
        String newName, newLocation, newStartTime, newEndTime;
        LocalDate newDate;

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            // Solicitar al usuario que ingrese el ID del evento que desea editar
            System.out.println("Enter the ID of the event you want to edit: ");
            eventId = Integer.parseInt(reader.readLine());

            // Solicitar al usuario los nuevos valores para el evento
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
                // Llamar a la función remota para editar el evento
                dbOperations.updateEvent(eventId, newName, newLocation, newDate, newStartTime, newEndTime);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }


    public void deleteEvent(){
        int eId;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Select the id of the event you want to edit: ");
            eId = Integer.parseInt(reader.readLine());

            try {
                dbOperations.deleteEvent(eId);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }

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

            System.out.println("Creating a new event:");

            // Prompt for new entries for the data the user wants to edit
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
    }

    private void userEventsDisplay(String[]userData, List<Attendance> attendance){
        System.out.println("User:");
        System.out.format("%-5s%-10s%-15s\n", "ID", "NAME", "EMAIL");
        System.out.println("---------------------------------------------------");
        System.out.format("%-5s%-10s%-15s\n",
                userData[1], userData[0], userData[2]);

        System.out.println("\nEvents Table:");
        System.out.format("%-10s%-15s%-15s%-15s%-15s\n", "NAME", "LOCATION", "DATA", "START TIME", "END TIME");
        System.out.println("---------------------------------------------------");
        for (Attendance event : attendance) {
            System.out.format("%-10s%-15s%-17s%-14s%-15s\n", event.getEventName(), event.getLocation(),
                    event.getDate(), event.getStartTime(), event.getEndTime());
        }


    }
    private void attendanceDisplay(Event event, List<Attendance> attendance) {
        // Mostrar la tabla del evento
        System.out.println("Events Table:");
        System.out.format("%-5s%-10s%-15s%-15s%-15s%-15s\n", "ID", "NAME", "LOCATION", "DATA", "START TIME", "END TIME");
        System.out.println("---------------------------------------------------");
        System.out.format("%-5d%-10s%-15s%-17s%-14s%-15s\n", event.getId(), event.getName(), event.getLocation(),
                event.getData(), event.getStartTime(), event.getEndTime());

        // Mostrar la tabla de asistentes
        System.out.println("\nAttendance Table:");
        System.out.format("%-5s%-10s%-15s\n", "ID", "NAME", "EMAIL");
        System.out.println("---------------------------------------------------");

        // Mostrar los asistentes
        for (Attendance Attendee : attendance) {
            System.out.format("%-5d%-10s%-15s\n",
                    Attendee.getUserId(), Attendee.getUserName(), Attendee.getEmail());
        }
    }

    public void consultEvents() {
        List<Event> events;
        try {
            events = dbOperations.getAllEvents();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        eventsDisplay(events);
        System.out.println("<Enter> to go back to your profile");
        System.out.println();
        try {
            System.in.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void eventsDisplay(List<Event> events){
        System.out.println("Tabla de Eventos:");
        System.out.format("%-5s%-10s%-15s%-15s%-15s%-15s%-15s\n", "ID", "NAME", "LOCATION", "DATA", "START TIME", "END TIME", "ASSISTANCE");
        System.out.println("------------------------------------------------------------------------------------------");

        for (Event event : events) {
            System.out.format("%-5d%-10s%-15s%-17s%-14s%-15s%-17d\n",
                    event.getId(), event.getName(), event.getLocation(),
                    event.getData(), event.getStartTime(), event.getEndTime(), event.getttendees());
        }
    }

}
