package pt.wastemanagement.api.model;

public class EmployeeDetailedInfo extends Employee {
    public final byte[] hashedPassword;
    public final int salt;

    public EmployeeDetailedInfo(String username, String name, String email, int phoneNumber, String job,
                                byte[] hashedPassword, int salt) {
        super(username, name, email, phoneNumber, job);
        this.hashedPassword = hashedPassword;
        this.salt = salt;
    }
}
