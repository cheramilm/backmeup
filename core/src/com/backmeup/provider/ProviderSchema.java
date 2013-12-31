package com.backmeup.provider;

import com.backmeup.ApplicationException;
import com.backmeup.PropertyBasedObject;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

@XmlRootElement(name = "ProviderSchema")
public class ProviderSchema {
    static Logger logger = Logger.getLogger(ProviderSchema.class);
    private String version;
    private List<Provider> providers = new ArrayList<Provider>();
    private List<Property> commonProperties = new ArrayList<Property>();
    private Map<String, Provider> providerMap = new HashMap<String, Provider>();
    private static ProviderSchema instance;
    private Map<String,Property> commonPropertyMap=new HashMap<String,Property>();

    static {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ProviderSchema.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setListener(new UnmarshalListener());
            instance = (ProviderSchema) unmarshaller.unmarshal(ProviderSchema.class.getResource("/schema/providers.xml"));
            instance.validateSchema();
            instance.init();
        } catch (JAXBException e) {
            throw new RuntimeException("Can't init provider schema. Fatal error.", e);
        }
    }

    private ProviderSchema() {

    }

    public static Set<String> getStorageProviders() {
        return instance.providerMap.keySet();
    }

    public static ProviderSchema getInstance() {
        return instance;
    }

    private void validateSchema() {
        for (Property property : commonProperties) {
            for (Provider provider : providers) {
                for (Property p : provider.getProperties()) {
                    if (property.getName().equals(p.getName())) {
                        throw new RuntimeException("Invalid schema, provider " + provider.getName() + " contains duplicate property:" + p.getName());
                    }
                }
            }
        }
    }

    private void init() {
        for(Property property:commonProperties) {
            commonPropertyMap.put(property.getName(),property);
        }
        for (Provider provider : providers) {
            providerMap.put(provider.getName(), provider);
            provider.getProperties().addAll(commonProperties);
            provider.init();
        }
    }

    public static void initPropertyBasedObject(Properties properties, PropertyBasedObject object) {
        String provider = properties.getProperty("StorageProvider");
        Provider p = getProvider(provider);
        object.setProvider(p);
        for(Property property:p.getProperties()) {
            object.setProperty(property.getName(),property.getRealDefaultValue());
        }
        for (String name : properties.stringPropertyNames()) {
            Property property = p.getProperty(name);
            object.setProperty(name, property.getValue(properties.getProperty(name)));
        }
    }

    public static void validateConfigure(Properties object) throws ApplicationException {
        String provider = object.getProperty("StorageProvider");
        if (provider == null) {
            throw new ApplicationException("Must specify StorageProvider.");
        }
        Provider p = getProvider(provider);
        if (p == null) {
            throw new ApplicationException("Can't find corresponding provider type for:" + provider);
        }
        validateProperties(object, p);
    }

    public Property getCommonProperty(String name) {
        return commonPropertyMap.get(name);
    }

    private static void validateProperties(Properties object, Provider provider) throws ApplicationException {
        for (Property property : provider.getProperties()) {
            if (!property.isNullable() && object.getProperty(property.getName()) == null) {
                throw new ApplicationException("Doesn't contain not nullable properties:" + property.getName());
            }
        }
        for (String name : object.stringPropertyNames()) {
            Property property = provider.getProperty(name);
            if (property == null) {
                logger.info("Doesn't contain specified property in schema:" + name + ", ignored");
            } else {
                String value = object.getProperty(name);
                if (property.getAllowValues() != null && property.getAllowValues().size() > 0 && !property.getAllowValues().contains(value)) {
                    if (!property.isNullable()) {
                        throw new ApplicationException("Invalid value[" + value + "] for property " + name + ", valid values are:" + property.getAllowValues());
                    }
                }
            }
        }
    }

    public static Provider getProvider(String type) {
        return instance.providerMap.get(type);
    }

    @XmlAttribute(name = "Version")
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @XmlElement(name = "Provider")
    @XmlElementWrapper(name = "Providers")
    public List<Provider> getProviders() {
        return providers;
    }

    public void setProviders(List<Provider> providers) {
        this.providers = providers;
    }

    @XmlElement(name = "Property")
    @XmlElementWrapper(name = "CommonProperties")
    public List<Property> getCommonProperties() {
        return commonProperties;
    }

    public void setCommonProperties(List<Property> commonProperties) {
        this.commonProperties = commonProperties;
    }
}
