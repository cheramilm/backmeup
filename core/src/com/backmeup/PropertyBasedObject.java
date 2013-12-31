package com.backmeup;

import com.backmeup.provider.Provider;
import com.backmeup.provider.ProviderSchema;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertyBasedObject {
    static Logger logger = Logger.getLogger(PropertyBasedObject.class);
    private Map<String, Object> values = new HashMap<String, Object>();
    private Provider provider;

    private void load(InputStream input, boolean closeInput) throws ApplicationException {
        try {
            Properties properties = new Properties();
            properties.load(input);
            ProviderSchema.validateConfigure(properties);
            ProviderSchema.initPropertyBasedObject(properties, this);
        } catch (IOException e) {
            throw new ApplicationException("Can't load properties.", e);
        } finally {
            if (closeInput && input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    logger.warn("Can't close input stream.", e);
                }
            }
        }
    }

    public Properties getProperties() {
        Properties properties = new Properties();
        for(Map.Entry<String,Object> entry:values.entrySet()) {
            if (entry.getValue()!=null) {
                 properties.put(entry.getKey(),entry.getValue());
            }
        }
        return properties;
    }

    public void initFrom(Properties properties) throws ApplicationException {
        ProviderSchema.validateConfigure(properties);
        ProviderSchema.initPropertyBasedObject(properties, this);
    }

    public void loadFrom(File file) throws ApplicationException {
        try {
            load(new FileInputStream(file), true);
        } catch (FileNotFoundException e) {
            throw new ApplicationException("Can't find file:" + file, e);
        }
    }

    public void loadFrom(String path) throws ApplicationException {
        loadFrom(new File(path));
    }

    public void loadFromResource(String path) throws ApplicationException {
        URL url = PropertyBasedObject.class.getResource(path);
        try {
            load(url.openStream(), true);
        } catch (IOException e) {
            throw new ApplicationException("Can't open stream from:" + url, e);
        }
    }

    public void setProperty(String name, Object value) {
        values.put(name, value);
    }

    public void applyProperty(String name, PropertyBasedObject object) {
        values.put(name, object.getProperty(name));
    }

    public void applyProperties(PropertyBasedObject object) {
        object.values.putAll(values);
    }

    public Object getProperty(String name) {
        return values.get(name);
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public void save(File f) {
        Properties properties = new Properties();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (entry.getValue() != null) {
                properties.put(entry.getKey(), entry.getValue().toString());
            }
        }
        FileOutputStream outputFile = null;
        try {
            outputFile = new FileOutputStream(f);
            properties.store(outputFile, "Saved by Backmeup");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (outputFile != null) {
                try {
                    outputFile.close();
                } catch (IOException e) {
                    throw new RuntimeException("Can't save configure.", e);
                }
            }
        }
    }

    public void save(String path) {
        save(new File(path));
    }
}
