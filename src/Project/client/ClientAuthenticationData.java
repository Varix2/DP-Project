package Project.client;

import java.io.Serializable;

public class ClientAuthenticationData implements Serializable {

    private final String email;
    private final String password;

    public ClientAuthenticationData(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
