package com.peykasa.authserver;

/**
 * @author Yaser(amin) Sadeghi
 */
public class ConfigEntry {
    private String key;
    private String value;

    public ConfigEntry(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
