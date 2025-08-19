// src/main/java/com/foro/forohub/api/AuthController.java
package com.foro.forohub.api;

import com.foro.forohub.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;                   // <-- nuevo
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ExceptionHandler; // <-- nuevo
import java.util.Map;                                         // <-- nuevo

record LoginRequest(String username, String password) {}
record LoginResponse(String token) {}

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authManager, JwtService jwtService) {
        this.authManager = authManager;
        this.jwtService = jwtService;
    }

    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json") // <-- cambiado
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        var authToken = new UsernamePasswordAuthenticationToken(req.username(), req.password());
        var auth = authManager.authenticate(authToken);
        var user = (UserDetails) auth.getPrincipal();
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new LoginResponse(token));
    }

    // --- Manejadores de error amigables ---
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "bad_credentials", "message", "Usuario o contraseña inválidos"));
    }

    @ExceptionHandler({ DisabledException.class, LockedException.class })
    public ResponseEntity<Map<String, String>> handleAccountStatus(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "account_restricted", "message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "invalid_request", "message", ex.getMessage()));
    }
}