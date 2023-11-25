package Project.client;

import Project.client.ui.AdminMenus;
import Project.client.ui.UserMenus;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

public class Client {

    public static final int MAX_SIZE = 4000;
    public static final int TIMEOUT = 10; //segundos

    public static void main(String[] args) throws IOException {

        String response;
        ClientRegistryData cr;
        ClientAuthenticationData ca;
        String email;
        UserMenus userMenu = new UserMenus();
        AdminMenus adminMenu = new AdminMenus();

        if (args.length != 2) {
            System.out.println("Sintaxe: java Client serverAddress serverTCPPort");
            return;
        }

        try (Socket socket = new Socket(InetAddress.getByName(args[0]), Integer.parseInt(args[1]));
             ObjectInputStream oin = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {


            //socket.setSoTimeout(TIMEOUT * 1000);
            int opcionMainMenu;
            int optionProfile;
           //adminMenu.showProfile();
            do {
                opcionMainMenu = userMenu.mainMenu();
                if (opcionMainMenu == 1) {
                    cr = userMenu.showRegistryMenu();
                    sendAndReceive(out, oin, cr);
                } else if (opcionMainMenu == 2) {
                        ca = userMenu.showLoginMenu();
                        userMenu.showProfile(ca.getEmail());
                }
            }while (opcionMainMenu !=3);
            //new LoginForm(socket).setVisible(true);

        } catch (SocketException e){
            System.err.println("Error: "+e);
        }catch (IOException e) {
            e.printStackTrace();
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


}

