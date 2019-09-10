package pt.wastemanagement.api.model;

public class Employee {
    public final String username, name, email, job;
    public final int phoneNumber;

    public Employee(String username, String name, String email, int phoneNumber, String job) {
        this.username = username;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.job = job;
    }
}
