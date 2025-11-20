package com.securecourse.backend.toggles;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/toggles")
@CrossOrigin(origins = "http://localhost:5173") // Allow frontend access
public class ToggleController {

    private final ToggleService toggleService;

    public ToggleController(ToggleService toggleService) {
        this.toggleService = toggleService;
    }

    @GetMapping
    public Map<String, Boolean> getToggles() {
        return toggleService.getAllToggles();
    }

    @PostMapping("/update")
    public Map<String, Boolean> updateToggle(@RequestBody Map<String, Object> payload) {
        String key = (String) payload.get("key");
        Boolean value = (Boolean) payload.get("value");
        
        if (key != null && value != null) {
            toggleService.updateToggle(key, value);
        }
        
        return toggleService.getAllToggles();
    }
}
