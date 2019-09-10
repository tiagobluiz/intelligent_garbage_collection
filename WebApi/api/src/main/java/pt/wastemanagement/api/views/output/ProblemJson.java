package pt.wastemanagement.api.views.output;

public class ProblemJson {
    public final String type;
    public final String title;
    public final int status;
    public final String message;
    public final String detail;

    public ProblemJson(String type, String title, int status, String message, String detail) {
        this.type = type;
        this.title = title;
        this.status = status;
        this.message = message;
        this.detail = detail;
    }
}
