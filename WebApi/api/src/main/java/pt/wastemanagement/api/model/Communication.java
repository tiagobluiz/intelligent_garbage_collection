package pt.wastemanagement.api.model;

public class Communication {
    public final int communicationId;
    public final String communicationDesignation;

    public Communication(int communicationId, String communicationDesignation) {
        this.communicationId = communicationId;
        this.communicationDesignation = communicationDesignation;
    }
}
