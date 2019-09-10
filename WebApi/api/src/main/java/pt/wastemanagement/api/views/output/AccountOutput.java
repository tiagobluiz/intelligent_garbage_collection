package pt.wastemanagement.api.views.output;

public class AccountOutput {
    public final boolean error;
    public final String message;
    public final String password;

    public AccountOutput(boolean error, String message, String password) {
        this.error = error;
        this.message = message;
        this.password = password;
    }

    public AccountOutput(boolean error) {
        this.error = error;
        this.message = "";
        this.password = "";
    }
}
