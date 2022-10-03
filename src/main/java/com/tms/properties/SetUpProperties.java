package com.tms.properties;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class SetUpProperties {

    private String PROPERTY_FILE = "SetUp.properties";
    private static SetUpProperties setup = new SetUpProperties();
    private Properties properties = null;

    private SetUpProperties() {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream is = classLoader.getResourceAsStream(PROPERTY_FILE);
        properties = new Properties();

        try {
            properties.load(is);
        } catch (IOException e) {
            log.error("Properties could not be loaded: {}", e.getMessage());
        }
    }

    public static SetUpProperties getInstance() {
        return setup;
    }

    public String getProperty(String key) {
        String value = properties.getProperty(key);
        return (value != null) ? value.trim() : value;
    }

    public int getIntProperty(String key) {
        String value = properties.getProperty(key);
        return (value != null) ? Integer.parseInt(value.trim()) : null;
    }
}
