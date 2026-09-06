package ru.matveysuvorov.teamup.auth;

import org.springframework.web.bind.annotation.*;
import ru.matveysuvorov.teamup.auth.dto.*;
import ru.matveysuvorov.teamup.auth.services.AuthService;

import java.io.IOException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public String register(
            @RequestBody RegisterRequest request
    ) {
        authService.register(request);
        return "Please, check your email for verification!";
    }

    @PostMapping("/login")
    public AuthResponse login(
            @RequestBody LoginRequest request
    ) {
        return authService.login(request);
    }

    @PostMapping("/google")
    public AuthResponse google(
            @RequestBody GoogleAuthRequest request
    ) throws IOException, java.security.GeneralSecurityException {
        return authService.googleAuth(request);
    }

    @GetMapping("/verify-email")
    public AuthResponse verifyEmail(
            @RequestParam String token
    ) {
        return authService.verifyEmail(token);
    }

    @PostMapping("/forgot")
    public String forgotPassword(
            @RequestBody ForgotPasswordRequest request
    ) {
        authService.forgotPassword(request.email());
        return "Please, check your email for reset password";
    }

}
