package com.backmeup.provider;

import com.backmeup.Utils;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.Set;

@XmlRootElement(name = "Property")
public class Property {
    private String name;
    private String valueType;
    private boolean nullable;
    private String defaultValue;
    private Set<String> allowValues;
    private int idPosition;

    private Set<Object> realAllowValues;
    private Object realDefaultValue;

    @XmlAttribute(name = "Name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute(name = "IdPosition")
    public int getIdPosition() {
        return idPosition;
    }

    public void setIdPosition(int idPosition) {
        this.idPosition = idPosition;
    }

    @XmlAttribute(name = "ValueType")
    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    @XmlAttribute(name = "Nullable")
    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    @XmlAttribute(name = "DefaultValue")
    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Set<Object> getRealAllowValues() {
        return realAllowValues;
    }

    protected void init() {
        realAllowValues=new HashSet<Object>();
        if (allowValues!=null) {
            for(String s:allowValues) {
                  realAllowValues.add(getValue(s));
            }
        }
        if (defaultValue!=null) {
            realDefaultValue=getValue(defaultValue);
        }
    }

    @XmlAttribute(name = "AllowValues")
    @XmlList
    public Set<String> getAllowValues() {
        return allowValues;
    }

    public void setAllowValues(Set<String> allowValues) {
        this.allowValues = allowValues;
    }

    public Object getRealDefaultValue() {
        return realDefaultValue;
    }

    public Object getValue(String value) {
        Object result=Utils.getValue(valueType,value);
        if (result==null&&realDefaultValue!=null) {
            return realDefaultValue;
        } else {
            return result;
        }
    }

}
