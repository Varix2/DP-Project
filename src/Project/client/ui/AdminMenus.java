package Project.client.ui;

import Project.manageDB.DbOperationsInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            int option;

            System.out.println("Main Menu");
            System.out.println("1. Create event");
            System.out.println("2. Edit event");
            System.out.println("3. Delete event");
            System.out.println("4. Consult events");
            System.out.println("5. Generation of a code to record attendance at an event that is currently taking place.");
            System.out.println("6. Consult attendance to an event");
            System.out.println("7. Obtain a CSV file of the attendance (from last point)");
            System.out.println("8. Consult specific user attendances");
            System.out.println("9. Obtain a CSV file of the attendance of a specific user");
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

                    break;
            }

            return option;

        } catch (IOException e) {
            throw new RuntimeException(e);
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
}
