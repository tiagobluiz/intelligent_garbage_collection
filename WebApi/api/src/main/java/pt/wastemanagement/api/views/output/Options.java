package pt.wastemanagement.api.views.output;

/**
 * This class need to be generic, because this needs to be use by both Siren and Collection
 */
public class Options {
    public final String text;
    public final String value;


    public Options(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * This toString() method will only be used to give info to user about
     * the possible values that the field 'containerType' accepts. This information
     * is given by our @value field, so only this field will be printed
     */
    @Override
    public String toString() {
        return value;
    }
}
