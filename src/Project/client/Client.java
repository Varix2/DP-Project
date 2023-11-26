package Project.client;

import Project.client.ui.TextUI;
import Project.principalServer.PrincipalServerInterface;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.NotBoundException;

public class Client {

    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            System.out.println("Sintaxe: java Client serverAddress serverTCPPort");
            return;
        }

        InetAddress serverAddress = InetAddress.getByName(args[0]);
        int tcpPort = Integer.parseInt(args[1]);

        String objUrl;


        TextUI textUI = new TextUI(serverAddress,tcpPort);
        textUI.run();

        /*
            RMI
         */






    }




}

