package pt.wastemanagement.api.model;

public class ConfigurationCommunication {
    public final int configurationId, communicationId, value;
    public final String communicationDesignation;

    public ConfigurationCommunication(int configurationId, int communicationId, String communicationDesignation, int value) {
        this.configurationId = configurationId;
        this.communicationId = communicationId;
        this.value = value;
        this.communicationDesignation = communicationDesignation;
    }

    public ConfigurationCommunication(int configurationId, int communicationId, int value) {
        this.configurationId = configurationId;
        this.communicationId = communicationId;
        this.value = value;
        this.communicationDesignation = "";
    }
}
