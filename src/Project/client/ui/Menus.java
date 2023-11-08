package Project.client.ui;

import Project.client.ClientAuthenticationData;
import Project.client.ClientRegistryData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Menus {
    public static ClientRegistryData showRegistryMenu(){
        String name = null, email = null, passwd = null;
        int id = 0;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Menú de Registro");
            System.out.println("1. Registrar un nuevo usuario");
            System.out.println("2. Salir");
            System.out.print("Elije una opción: ");

            String opcion = reader.readLine();

            switch (opcion) {
                case "1":
                    System.out.println("Ingresa los datos del usuario:");
                    System.out.print("Name: ");
                    name = reader.readLine();
                    System.out.print("Identification: ");
                    id = Integer.parseInt(reader.readLine());
                    System.out.print("Email: ");
                    email = reader.readLine();
                    System.out.print("Password: ");
                    passwd = reader.readLine();

                    break;
                case "2":
                    System.out.println("Saliendo del programa.");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Opción no válida. Por favor, elige 1 o 2.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ClientRegistryData(name,id,email, passwd);
    }


    public static int mainMenu(){
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            int option;

                System.out.println("Main Menu");
                System.out.println("1. Login");
                System.out.println("2. Sign up");
                System.out.println("3. Exit");
            do {
                System.out.println("----------------------------");
                System.out.print("Choose an option: ");

                option = Integer.parseInt(reader.readLine());

                if (option < 1 || option > 3) {
                    System.out.println("ENTER A VALID OPTION");
                }
            } while (option < 1 || option > 3);

            if (option == 3) {
                System.out.println("Saliendo del programa.");
                System.exit(0);
            }
            return option;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static ClientAuthenticationData showLoginMenu() {
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
}
