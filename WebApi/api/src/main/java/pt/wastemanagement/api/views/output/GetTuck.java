package pt.wastemanagement.api.views.output;

import pt.wastemanagement.api.model.Truck;

public class GetTuck {
    public final String registrationPlate, active;

    public GetTuck(Truck truck) {
        this.registrationPlate = truck.registrationPlate;
        this.active = truck.active.equalsIgnoreCase("T")? Boolean.toString(true) : Boolean.toString(false);
    }
}
