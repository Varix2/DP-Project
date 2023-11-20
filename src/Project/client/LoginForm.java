package Project.client;

// LoginForm.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class LoginForm extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    private Socket clientSocket; // Assuming you have a pre-established socket

    public LoginForm(Socket clientSocket) {
        this.clientSocket = clientSocket;

        setTitle("Login Form");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 2));

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();
        loginButton = new JButton("Login");

        add(usernameLabel);
        add(usernameField);
        add(passwordLabel);
        add(passwordField);
        add(new JLabel()); // Empty label for spacing
        add(loginButton);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                // Assuming clientSocket is already established elsewhere in your client code
                sendCredentials(username, password);
            }
        });
    }

    private void sendCredentials(String username, String password) {
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream())) {

            // Send the username and password to the server
            objectOutputStream.writeObject(username);
            objectOutputStream.writeObject(password);

            // Receive the server's response
            String response = (String) objectInputStream.readObject();

            // Display the response
            JOptionPane.showMessageDialog(this, response);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

