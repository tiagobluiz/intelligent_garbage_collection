package pt.wastemanagement.api.model;

public class Configuration {
    public final int configurationId;
    public final String configurationName;

    public Configuration(int configurationId, String configurationName) {
        this.configurationId = configurationId;
        this.configurationName = configurationName;
    }
}
