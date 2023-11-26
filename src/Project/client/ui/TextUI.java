package Project.client.ui;

import Project.client.data.ClientAuthenticationData;
import Project.client.data.ClientRegistryData;
import Project.client.exceptions.AuthenticationErrorException;
import Project.manageDB.DbOperationsInterface;
import Project.manageDB.data.Event;

import java.io.*;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

public class TextUI {
    private final DbOperationsInterface dbOperations;
    InetAddress serverAddress;
    int tcpPort;
    AdminMenus adminMenus;
    public TextUI(InetAddress serverAddress, int tcpPort) {
        this.serverAddress = serverAddress;
        this.tcpPort = tcpPort;
        this.adminMenus = new AdminMenus();
        try {
            dbOperations = (DbOperationsInterface) Naming.lookup("rmi://localhost:2000/DB-service");
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            throw new RuntimeException(e);
        }

    }
    public void run() {
        try (
                Socket socket = new Socket(serverAddress, tcpPort);
                ObjectInputStream oin = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream()))
        {
            //socket.setSoTimeout(TIMEOUT * 1000);
            int opcionMainMenu;
            int optionProfile;
            //adminMenu.showProfile();
            do {
                opcionMainMenu = mainMenu();
                try {
                    if (opcionMainMenu == 1) {
                        ClientRegistryData cr = showRegistryMenu();
                        sendAndReceive(out, oin, cr);
                    } else if (opcionMainMenu == 2) {
                        ClientAuthenticationData ca = showLoginMenu();
                        out.writeObject(ca);
                        out.flush();
                        Boolean authState = (Boolean) oin.readObject();
                        if (!authState) {
                            throw new AuthenticationErrorException("Authentication unsuccessful. Please check your credentials.");
                        }
                        if (dbOperations.isAdmin(ca.getEmail())) {
                            int option;
                            do {
                                option = adminMenus.showProfile();
                            } while (option != 12);
                        } else {
                            showProfile(ca.getEmail());
                        }
                    }
                } catch (AuthenticationErrorException e) {
                    System.err.println(e);
                }
            } while (opcionMainMenu != 3);

        } catch (SocketException e) {
            System.err.println("Error: " + e);
        } catch (NumberFormatException e) {
            System.err.println("Wrong datatype inserted.");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public ClientRegistryData showRegistryMenu() {
        String name = null, email = null, passwd = null;
        int id = 0;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Login Menu");
            System.out.print("Name: ");
            name = reader.readLine();
            System.out.print("Identification: ");
            id = Integer.parseInt(reader.readLine());
            System.out.print("Email: ");
            email = reader.readLine();
            System.out.print("Password: ");
            passwd = reader.readLine();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e){
            System.err.println("The identification only accepts numbers.");
        }
        return new ClientRegistryData(name, id,email, passwd);
    }


    public int mainMenu() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            int option;
            System.out.println("\n********************");
            System.out.println("Main Menu");
            System.out.println("1. Login");
            System.out.println("2. Sign up");
            System.out.println("3. Exit");
            do {
                System.out.println("----------------------------");
                System.out.print("Choose an option: ");

                option = Integer.parseInt(reader.readLine());

                if (option < 1 || option > 3) {
                    System.out.println("You selected an invalid option.");
                }
            } while (option < 1 || option > 3);

            return option;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ClientAuthenticationData showLoginMenu() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            System.out.print("Email: ");
            String email = reader.readLine();
            System.out.print("Password: ");
            String password = reader.readLine();

            return new ClientAuthenticationData(email, password);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void showUserData(String email) throws RemoteException {
        clearConsole();
        String[] userData = dbOperations.getUserData(email);

        if (userData != null) {
            System.out.println("\nUser Profile:");
            System.out.println("--------------------------------------");
            System.out.println("Username: " + userData[0]);
            //System.out.println("Identification: " + userData[1]);
            System.out.println("Email: " + userData[1]);

            System.out.println("<Enter> to go back to your profile");
            System.out.println();
            try {
                System.in.read();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("User not found.");
        }
    }


    public void showProfile(String email) {
        int option;
        String userEmail = email;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            do {
                System.out.println("\nWelcome " + userEmail);
                System.out.println("1. See your data");
                System.out.println("2. Edit your data");
                System.out.println("3. Register for an event");
                System.out.println("4. Consult your events");
                System.out.println("5. Exit");
                System.out.println("----------------------------");
                System.out.print("Choose an option: ");

                option = Integer.parseInt(reader.readLine());

                switch (option) {
                    case 1:
                        showUserData(userEmail);
                        break;
                    case 2:
                        userEmail = editUserData(userEmail);
                        break;
                    case 3:
                        registerForEvent(userEmail);
                        break;
                    case 4:
                        consultYourEvents(userEmail);

                        break;
                    case 5:
                        System.out.println("Exiting...");
                        break;
                    default:
                        System.out.println("Invalid option inserted.");
                        break;
                }
            } while (option != 5);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void consultYourEvents(String email) {
        List<Event> events;
        try {
            events = dbOperations.getUserEvents(email);
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

    private String editUserData(String email) {
        String newName, newEmail, newPassword;
        int newID;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Editing data for " + email + ":");

            // Prompt for new entries for the data the user wants to edit
            System.out.print("Enter your new name: ");
            newName = reader.readLine();

            System.out.print("Enter the new identification number: ");
            newID = Integer.parseInt(reader.readLine());

            System.out.print("Enter the new email: ");
            newEmail = reader.readLine();

            System.out.print("Enter the new password: ");
            newPassword = reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            dbOperations.updateUserData(newName, newEmail, newPassword, email);
            return newEmail;
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

    }

    public void registerForEvent(String email) {
        List<Event> events;
        try {
            events = dbOperations.getAllEvents();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        if(events.isEmpty()){
            System.out.println("No events available ");
        } else{
            eventsDisplay(events);
        }
        int option;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("1. Join an event:");
            System.out.println("2. Go back to menu");
            do {
                System.out.println("----------------------------");
                System.out.print("Choose an option: ");

                option = Integer.parseInt(reader.readLine());

                if (option < 1 || option > 2) {
                    System.out.println("ENTER A VALID OPTION");
                }
            } while (option < 1 || option > 2);
            if(option == 1){
                System.out.println("Select the ID of the event you want to join: ");
                int eventID = Integer.parseInt(reader.readLine());
                dbOperations.joinAnEvent(email,eventID);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("<Enter> to go back to your profile");
        System.out.println();
        try {
            System.in.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
    }

    private static void sendAndReceive(ObjectOutputStream out, ObjectInputStream oin, Object obj)
    {
        try {
            out.writeObject(obj);
            out.flush();
            String response = (String) oin.readObject();
            System.out.println(response);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    private void clearConsole(){
        try {
            new ProcessBuilder("clear").inheritIO().start().waitFor();
        } catch (Exception e) {
            /*No hacer nada*/
        }
    }

}
