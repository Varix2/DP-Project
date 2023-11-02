package Proyecto;

import java.io.Serializable;

public class ClientRegistryData implements Serializable {

    private String name;
    private int id_number;
    private String email;
    private String password;

    public ClientRegistryData(String name, int id_number, String email, String password) {
        this.name = name;
        this.id_number = id_number;
        this.email = email;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public int getId_number() {
        return id_number;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}

