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

        if (args.length != 2) {
            System.out.println("Sintaxe: java Client serverAddress serverTCPPort");
            return;
        }

        try (Socket socket = new Socket(InetAddress.getByName(args[0]), Integer.parseInt(args[1]));
             ObjectInputStream oin = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream oout = new ObjectOutputStream(socket.getOutputStream())) {


            socket.setSoTimeout(TIMEOUT * 1000);
            if(Menus.mainMenu() == 1){
                cr = Menus.showRegistryMenu();
                oout.writeObject(cr);
                oout.flush();
            } else{
                ca = Menus.showLoginMenu();
                oout.writeObject(ca);
                oout.flush();
            }

            //Deserializa a resposta recebida em socket
            response = (String) oin.readObject();

            if (response == null) {
                System.out.println("O servidor nao enviou qualquer respota antes de"
                        + " fechar aligacao TCP!");
            } else {
                System.out.println(response);
            }

        } catch (Exception e) {
            System.out.println("Ocorreu um erro no acesso ao socket:\n\t" + e);
        }
    }


}

