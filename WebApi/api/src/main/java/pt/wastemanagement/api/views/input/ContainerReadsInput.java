package pt.wastemanagement.api.views.input;

public class ContainerReadsInput {
    public String data;
    public short battery = -100, temperature = -100, occupation = -100;

    @Override
    public String toString() {
        return "{" +
                "battery=" + battery +
                ", temperature=" + temperature +
                ", occupation=" + occupation +
                '}';
    }
}
