package pt.wastemanagement.api.views.output;

import pt.wastemanagement.api.model.ConfigurationCommunication;

import java.util.List;

public class ContainerConfiguration {
    public int height;
    public List<ConfigurationCommunication> configurationCommunications;

    public ContainerConfiguration(int height, List<ConfigurationCommunication> configurationCommunications) {
        this.height = height;
        this.configurationCommunications = configurationCommunications;
    }
}
