package ru.matveysuvorov.teamup.auth.services;

import jakarta.transaction.Transactional;
import org.apache.commons.codec.digest.DigestUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.data.repository.cdi.Eager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.matveysuvorov.teamup.auth.entities.AccountEntity;
import ru.matveysuvorov.teamup.auth.entities.EmailVerificationTokenEntity;
import ru.matveysuvorov.teamup.auth.entities.ProviderAccountEntity;
import ru.matveysuvorov.teamup.auth.entities.ResetPasswordTokenEntity;
import ru.matveysuvorov.teamup.auth.repositories.AccountRepository;
import ru.matveysuvorov.teamup.auth.dto.*;
import ru.matveysuvorov.teamup.auth.repositories.EmailVerificationTokenRepository;
import ru.matveysuvorov.teamup.auth.repositories.ProviderAccountRepository;
import ru.matveysuvorov.teamup.auth.repositories.ResetPasswordTokenRepository;
import ru.matveysuvorov.teamup.security.JwtService;
import ru.matveysuvorov.teamup.users.exceptions.UserNotFoundException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AuthService {

    private final AccountRepository accountRepository;
    private final ProviderAccountRepository providerAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GoogleAuthService googleAuthService;
    private final EmailVerificationTokenRepository emailTokenRepository;
    private final ResetPasswordTokenRepository resetPasswordTokenRepository;
    private final EmailService emailService;

    public AuthService(
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            GoogleAuthService googleAuthService,
            ProviderAccountRepository providerAccountRepository,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            ResetPasswordTokenRepository resetPasswordTokenRepository,
            EmailService emailService
    ) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.googleAuthService = googleAuthService;
        this.providerAccountRepository = providerAccountRepository;
        this.emailTokenRepository = emailVerificationTokenRepository;
        this.resetPasswordTokenRepository = resetPasswordTokenRepository;
        this.emailService = emailService;
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        AccountEntity account = new AccountEntity();
        account.setEmail(request.getEmail());
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        account.setEnabled(true);
        account.setIsEmailConfirmed(false);
        account.setCraetedAt(Instant.now());

        accountRepository.save(account);

        String verificationToken = UUID.randomUUID().toString();
        String tokenHash = DigestUtils.sha256Hex(verificationToken);
        emailTokenRepository.deleteAllByAccountId(account.getId());
        EmailVerificationTokenEntity verificationEntity = new EmailVerificationTokenEntity();
        verificationEntity.setAccount(account);
        verificationEntity.setTokenHash(tokenHash);
        verificationEntity.setCreatedAt(Instant.now());
        verificationEntity.setExpiredAt(Instant.now().plus(24, ChronoUnit.HOURS));

        emailTokenRepository.save(verificationEntity);

        emailService.sendVerificationEmail(account.getEmail(), verificationToken);
    }

    public AuthResponse login(LoginRequest request) {
        AccountEntity account = accountRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!account.isEnabled()) {
            throw new DisabledException("Account is disabled");
        }

        if (account.getPasswordHash() == null || !passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(account.getId());
        return new AuthResponse(token, "Bearer");
    }

    @Transactional
    public AuthResponse verifyEmail(String token) {
        String tokenHash = DigestUtils.sha256Hex(token);
        EmailVerificationTokenEntity verification = emailTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

        if (verification.getUsedAt() != null) {
            throw new IllegalArgumentException("Token already used");
        }

        if (verification.getExpiredAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Token expried");
        }

        AccountEntity account = verification.getAccount();
        account.setIsEmailConfirmed(true);

        verification.setUsedAt(Instant.now());

        accountRepository.save(account);
        emailTokenRepository.save(verification);

        String accessToken = jwtService.generateToken(account.getId());
        return new AuthResponse(accessToken, "Bearer");
    }

    public AuthResponse googleAuth(GoogleAuthRequest request) throws IOException, GeneralSecurityException {
        GoogleUser googleUser = googleAuthService.verify(request.provider_id_token());
        if (!providerAccountRepository.existsByProviderUserId(googleUser.user_id())) {
            AccountEntity account = new AccountEntity();
            account.setEmail(googleUser.email());
            account.setEnabled(true);
            account.setIsEmailConfirmed(true);
            account.setCraetedAt(Instant.now());
            accountRepository.save(account);

            ProviderAccountEntity providerAccount = new ProviderAccountEntity();
            providerAccount.setProvider("google");
            providerAccount.setUserId(account.getId());
            providerAccount.setProviderUserId(googleUser.user_id());

            String token = jwtService.generateToken(account.getId());
            return new AuthResponse(token, "Bearer");
        }

        ProviderAccountEntity providerAccount = providerAccountRepository.findByProviderUserId(googleUser.user_id());

        String token = jwtService.generateToken(providerAccount.getUserId());
        return new AuthResponse(token, "Bearer");
    }

    public void forgotPassword(String email) {
        boolean isUserExists = accountRepository.existsByEmail(email);

        if (!isUserExists) throw new UserNotFoundException("Account not found");

        UUID token = UUID.randomUUID();
        String tokenHash = DigestUtils.sha256Hex(token.toString());
        ResetPasswordTokenEntity entity = new ResetPasswordTokenEntity();
        entity.setEmail(email);
        entity.setTokenHash(tokenHash);
        entity.setCreatedAt(Instant.now());
        entity.setExpiredAt(Instant.now().plus(1, ChronoUnit.HOURS));

        resetPasswordTokenRepository.save(entity);

        emailService.sendResetPasswordEmail(email, token);
   }

}
