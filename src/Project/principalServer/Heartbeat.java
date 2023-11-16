package Project.principalServer;

import java.io.Serializable;

public class Heartbeat implements Serializable {
    private final int registryPort;
    private final String rmiServicesName;
    private final int dbVersion;

    public Heartbeat(int registryPort, String rmiServicesName, int dbVersion) {
        this.registryPort = registryPort;
        this.rmiServicesName = rmiServicesName;
        this.dbVersion = dbVersion;
    }

    public int getRegistryPort() {
        return registryPort;
    }

    public String getRmiServicesName() {
        return rmiServicesName;
    }

    public int getDbVersion() {
        return dbVersion;
    }
}
