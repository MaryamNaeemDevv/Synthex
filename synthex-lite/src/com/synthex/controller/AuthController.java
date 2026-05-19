package com.synthex.controller;

import com.synthex.FirebaseAuthService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final FirebaseAuthService auth = new FirebaseAuthService();

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        FirebaseAuthService.AuthResult result = auth.login(body.get("email"), body.get("password"));
        return Map.of(
            "success", result.success,
            "username", result.username != null ? result.username : "",
            "uid", result.uid != null ? result.uid : "",
            "message", result.message
        );
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> body) {
        FirebaseAuthService.AuthResult result = auth.register(
            body.get("email"), body.get("password"), body.get("username")
        );
        return Map.of(
            "success", result.success,
            "username", result.username != null ? result.username : "",
            "uid", result.uid != null ? result.uid : "",
            "message", result.message
        );
    }
}
