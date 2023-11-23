package Project.client;

import Project.client.ui.Menus;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class Client {

    public static final int MAX_SIZE = 4000;
    public static final int TIMEOUT = 10; //segundos

    public static void main(String[] args) throws IOException {

        String response;
        ClientRegistryData cr;
        ClientAuthenticationData ca;
        String email;
        Menus menu = new Menus();

        if (args.length != 2) {
            System.out.println("Sintaxe: java Client serverAddress serverTCPPort");
            return;
        }

        try (Socket socket = new Socket(InetAddress.getByName(args[0]), Integer.parseInt(args[1]));
             ObjectInputStream oin = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream oout = new ObjectOutputStream(socket.getOutputStream())) {


            //socket.setSoTimeout(TIMEOUT * 1000);
            int opcionMainMenu;
            int optionProfile;
            do {
                opcionMainMenu = menu.mainMenu();
                if (opcionMainMenu == 1) {
                    cr = Menus.showRegistryMenu();
                    sendAndReceive(oout, oin, cr);
                } else if (opcionMainMenu == 2) {
                        ca = menu.showLoginMenu();
                        sendAndReceive(oout, oin, ca);
                    do {
                        optionProfile = menu.showProfile(ca.getEmail());
                    }while(optionProfile !=4);
                }
            }while (opcionMainMenu !=3);
            //new LoginForm(socket).setVisible(true);

        } catch (RuntimeException e){
            System.err.println("Authentication error: "+e);
        }catch (Exception e) {
            System.out.println("Ocorreu um erro no acesso ao socket:\n\t" + e);
        }
    }

    private static void sendAndReceive(ObjectOutputStream oout, ObjectInputStream oin, Object obj)
    {
        try {
            oout.writeObject(obj);
            oout.flush();
            String response = (String) oin.readObject();
            System.out.println(response);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


}

