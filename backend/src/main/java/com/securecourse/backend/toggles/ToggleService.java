package com.securecourse.backend.toggles;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ToggleService {

    private final Map<String, Boolean> toggles = new ConcurrentHashMap<>();

    public ToggleService() {
        // Default all protections to TRUE (Secure by default)
        toggles.put("sqlInjectionProtection", true);
        toggles.put("fileUploadSecurity", true);
        toggles.put("xssProtection", true);
    }

    public boolean isSqlInjectionProtectionEnabled() {
        return toggles.getOrDefault("sqlInjectionProtection", true);
    }

    public boolean isFileUploadSecurityEnabled() {
        return toggles.getOrDefault("fileUploadSecurity", true);
    }

    public boolean isXssProtectionEnabled() {
        return toggles.getOrDefault("xssProtection", true);
    }

    public Map<String, Boolean> getAllToggles() {
        return new HashMap<>(toggles);
    }

    public void updateToggle(String key, boolean value) {
        if (toggles.containsKey(key)) {
            toggles.put(key, value);
        } else {
            throw new IllegalArgumentException("Invalid toggle key: " + key);
        }
    }
}
