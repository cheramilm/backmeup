package schema;

import com.backmeup.provider.Property;
import com.backmeup.provider.ProviderSchema;
import com.backmeup.provider.UnmarshalListener;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

@XmlRootElement(name = "UISchema")
public class UISchema {
    static Logger logger = Logger.getLogger(UISchema.class);
    private String version;
    private List<ProviderConfigure> providerConfigures = new ArrayList<ProviderConfigure>();
    private Map<String, ProviderConfigure> providerConfigureMap = new HashMap<String, ProviderConfigure>();
    private List<ConfigureItem> commonConfigureItems = new ArrayList<ConfigureItem>();
    private Map<String, ConfigureItem> commonConfigureItemMap = new HashMap<String, ConfigureItem>();
    private static UISchema instance;
    private static ConfigureItemOrderComparator comparator = new ConfigureItemOrderComparator();

    static {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(UISchema.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setListener(new UnmarshalListener());
            instance = (UISchema) unmarshaller.unmarshal(UISchema.class.getResource("/schema/ui.xml"));
            instance.init();
        } catch (JAXBException e) {
            throw new RuntimeException("Can't init ui schema. Fatal error.", e);
        }
    }

    private Property getProperty(String provider, String name) {
        Property result = ProviderSchema.getInstance().getCommonProperty(name);
        if (result == null) {
            result = ProviderSchema.getProvider(provider).getProperty(name);
        }
        return result;
    }

    private void init() {
        Collections.sort(commonConfigureItems, comparator);
        for (ConfigureItem item : commonConfigureItems) {
            item.setProperty(getProperty(null, item.getName()));
            commonConfigureItemMap.put(item.getName(), item);
        }
        for (ProviderConfigure configure : providerConfigures) {
            providerConfigureMap.put(configure.getName(), configure);
            Collections.sort(configure.getConfigureItems(), comparator);
            for (ConfigureItem item : configure.getConfigureItems()) {
                item.setProperty(getProperty(configure.getName(), item.getName()));
            }
        }
    }

    public ConfigureItem getCommonConfigureItem(String name) {
        return commonConfigureItemMap.get(name);
    }

    public static UISchema getInstance() {
        return instance;
    }

    private UISchema() {
    }

    @XmlAttribute(name = "Version")
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @XmlElement(name = "ProviderConfigure")
    @XmlElementWrapper(name = "ProviderConfigures")
    public List<ProviderConfigure> getProviderConfigures() {
        return providerConfigures;
    }

    public void setProviderConfigures(List<ProviderConfigure> providerConfigures) {
        this.providerConfigures = providerConfigures;
    }

    @XmlElement(name = "ConfigureItem")
    @XmlElementWrapper(name = "CommonConfigureItems")
    public List<ConfigureItem> getCommonConfigureItems() {
        return commonConfigureItems;
    }

    public void setCommonConfigureItems(List<ConfigureItem> commonConfigureItems) {
        this.commonConfigureItems = commonConfigureItems;
    }

    public ProviderConfigure getProviderConfigure(String name) {
        return providerConfigureMap.get(name);
    }
}
