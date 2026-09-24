package ru.matveysuvorov.teamup.auth;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.matveysuvorov.teamup.auth.dto.*;
import ru.matveysuvorov.teamup.auth.entities.AccountEntity;
import ru.matveysuvorov.teamup.auth.services.AuthService;
import ru.matveysuvorov.teamup.users.UserService;

import javax.security.auth.login.AccountNotFoundException;
import java.io.IOException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public String register(
            @RequestBody RegisterRequest request
    ) {
        authService.register(request);
        return "Please, check your email for verification!";
    }

    @GetMapping("/me")
    public AccountResponse getAccount(
            Authentication auth
    ) throws AccountNotFoundException {
        AccountEntity account = authService.getAccount(auth.getName());
        return new AccountResponse(account.getId(), account.getEmail(), account.isEmailConfirmed());
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

    @PostMapping("/discord")
    public String discord(
            @RequestBody DiscordAuthRequest request
    ) throws IOException, InterruptedException {
        return authService.discordAuth(request);
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

    @PostMapping("/reset")
    public void resetPassword(
            @RequestBody ResetPasswordRequest request
    ) {
        authService.resetPassword(request);
    }

    @GetMapping("/username/{username}/exists")
    public boolean isUsernameExists(
            @PathVariable String username
    ) {
        return userService.isUsernameExists(username);
    }

    @GetMapping("/email/{email}/exists")
    public boolean isEmailExists(
            @PathVariable String email
    ) {
        return userService.isEmailExists(email);
    }

}
