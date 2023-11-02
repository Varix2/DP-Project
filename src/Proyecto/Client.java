package Proyecto;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class Client {

    public static final int MAX_SIZE = 4000;
    public static final int TIMEOUT = 10; //segundos

    public static void main(String[] args) throws IOException {

        String response;

        if (args.length != 2) {
            System.out.println("Sintaxe: java Client serverAddress serverTCPPort");
            return;
        }

        try (Socket socket = new Socket(InetAddress.getByName(args[0]), Integer.parseInt(args[1]));
             ObjectInputStream oin = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream oout = new ObjectOutputStream(socket.getOutputStream())) {

            socket.setSoTimeout(TIMEOUT * 1000);

            ClientRegistryData cr = MenuRegistryClient();

            //Serializa a string TIME_REQUEST para o OutputStream associado a socket
            oout.writeObject(cr);
            oout.flush();

            //Deserializa a resposta recebida em socket
            response = (String) oin.readObject();

            if (response == null) {
                System.out.println("O servidor nao enviou qualquer respota antes de"
                        + " fechar aligacao TCP!");
            } else {
                System.out.println("Hora indicada pelo servidor: " + response);
            }

        } catch (Exception e) {
            System.out.println("Ocorreu um erro no acesso ao socket:\n\t" + e);
        }
    }

    public static ClientRegistryData MenuRegistryClient() {

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

                        System.out.println("Usuario registrado con éxito.");
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

