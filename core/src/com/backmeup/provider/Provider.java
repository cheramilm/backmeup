package com.backmeup.provider;

import com.backmeup.StorageProvider;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

@XmlRootElement(name = "Provider")
public class Provider {
    private String name;
    private List<Property> properties = new ArrayList<Property>();
    private Map<String, Property> propertyMap = new HashMap<String, Property>();
    private List<Property> idProperties = new ArrayList<Property>();
    private String implementClass;
    private static PropertyIdComparator comparator = new PropertyIdComparator();

    @XmlAttribute(name = "Name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute(name = "ImplementClass")
    public String getImplementClass() {
        return implementClass;
    }

    public void setImplementClass(String implementClass) {
        this.implementClass = implementClass;
    }

    @XmlElement(name = "Property")
    @XmlElementWrapper(name = "Properties")
    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public Property getProperty(String name) {
        return propertyMap.get(name);
    }

    protected void init() {
        initPropertyMap();
        initIdProperties();
    }

    protected void initPropertyMap() {
        for (Property property : properties) {
            propertyMap.put(property.getName(), property);
        }
    }

    public void initIdProperties() {
        for (Property property : properties) {
            if (property.getIdPosition() > 0) {
                idProperties.add(property);
            }
        }
        Collections.sort(idProperties, comparator);
    }

    public List<Property> getIdProperties() {
        return idProperties;
    }

    public StorageProvider getStorageProvider() {
        try {
            Class ownerClass = Class.forName(implementClass);
            return (StorageProvider) ownerClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Can't get specified class:" + implementClass, e);
        } catch (InstantiationException e) {
            throw new RuntimeException("Can't get specified class instance:" + implementClass, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Can't get specified class instance:" + implementClass, e);
        }
    }
}
