package schema;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "ConfigureItem")
public class ProviderConfigure {
    private String name;
    private List<ConfigureItem> configureItems;

    @XmlAttribute(name = "Name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "ConfigureItem")
    @XmlElementWrapper(name = "ConfigureItems")
    public List<ConfigureItem> getConfigureItems() {
        return configureItems;
    }

    public void setConfigureItems(List<ConfigureItem> configureItems) {
        this.configureItems = configureItems;
    }
}
