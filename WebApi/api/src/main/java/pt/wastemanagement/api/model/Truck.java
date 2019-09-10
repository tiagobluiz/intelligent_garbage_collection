package pt.wastemanagement.api.model;

public class Truck {
    public final String registrationPlate, active;

    public Truck(String registrationPlate, String active) {
        this.registrationPlate = registrationPlate;
        this.active = active;
    }
}
