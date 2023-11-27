package Project.client;

import Project.client.ui.TextUI;

import java.io.IOException;
import java.net.InetAddress;


public class Client {

    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            System.out.println("Sintaxe: java Client serverAddress serverTCPPort");
            return;
        }

        InetAddress serverAddress = InetAddress.getByName(args[0]);
        int tcpPort = Integer.parseInt(args[1]);



        TextUI textUI = new TextUI(serverAddress,tcpPort);
        textUI.run();






    }




}

