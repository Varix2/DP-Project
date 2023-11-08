package Project.client.ui;

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
}
