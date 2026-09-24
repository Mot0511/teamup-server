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
import ru.matveysuvorov.teamup.auth.exceptions.AccountNotFound;
import ru.matveysuvorov.teamup.auth.exceptions.UnauthorizedException;
import ru.matveysuvorov.teamup.auth.repositories.AccountRepository;
import ru.matveysuvorov.teamup.auth.dto.*;
import ru.matveysuvorov.teamup.auth.repositories.EmailVerificationTokenRepository;
import ru.matveysuvorov.teamup.auth.repositories.ProviderAccountRepository;
import ru.matveysuvorov.teamup.auth.repositories.ResetPasswordTokenRepository;
import ru.matveysuvorov.teamup.security.JwtService;
import ru.matveysuvorov.teamup.users.exceptions.UserNotFoundException;

import javax.security.auth.login.AccountNotFoundException;
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
    private final DiscordAuthService discordAuthService;
    private final EmailVerificationTokenRepository emailTokenRepository;
    private final ResetPasswordTokenRepository resetPasswordTokenRepository;
    private final EmailService emailService;

    public AuthService(
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            GoogleAuthService googleAuthService,
            DiscordAuthService discordAuthService,
            ProviderAccountRepository providerAccountRepository,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            ResetPasswordTokenRepository resetPasswordTokenRepository,
            EmailService emailService
    ) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.googleAuthService = googleAuthService;
        this.discordAuthService = discordAuthService;
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
        AccountEntity account;
        if (!accountRepository.existsByEmail(googleUser.email())) {
            account = new AccountEntity();
            account.setEmail(googleUser.email());
            account.setEnabled(true);
            account.setIsEmailConfirmed(true);
            account.setCraetedAt(Instant.now());
            accountRepository.save(account);
        } else {
            account = accountRepository.findByEmail(googleUser.email()).orElseThrow(() -> new AccountNotFound("Account not found"));
        }

        if (!providerAccountRepository.existsByProviderUserId(googleUser.user_id())) {
            ProviderAccountEntity providerAccount = new ProviderAccountEntity();
            providerAccount.setProvider("google");
            providerAccount.setUserId(account.getId());
            providerAccount.setProviderUserId(googleUser.user_id());
            providerAccountRepository.save(providerAccount);

            String token = jwtService.generateToken(account.getId());
            return new AuthResponse(token, "Bearer");
        }

        ProviderAccountEntity providerAccount = providerAccountRepository.findByProviderUserId(googleUser.user_id());

        String token = jwtService.generateToken(providerAccount.getUserId());
        return new AuthResponse(token, "Bearer");
    }

    @Transactional
    public String discordAuth(DiscordAuthRequest request) throws IOException, InterruptedException {
        DiscordUser discordUser = discordAuthService.verify(request.code());

        if (providerAccountRepository.existsByProviderUserId(discordUser.user_id())) {
            ProviderAccountEntity providerAccount = providerAccountRepository.findByProviderUserId(discordUser.user_id());

            accountRepository.findById(providerAccount.getUserId())
                    .orElseThrow(() -> new AccountNotFound("Account not found"));

            return jwtService.generateToken(providerAccount.getUserId());
        }

        AccountEntity account = accountRepository.findByEmail(discordUser.email())
                .orElseGet(() -> {
                    AccountEntity newAccount = new AccountEntity();
                    newAccount.setEmail(discordUser.email());
                    newAccount.setEnabled(true);
                    newAccount.setIsEmailConfirmed(true);
                    newAccount.setCraetedAt(Instant.now());

                    return accountRepository.save(newAccount);
                });

        ProviderAccountEntity providerAccount = new ProviderAccountEntity();
        providerAccount.setProvider("discord");
        providerAccount.setUserId(account.getId());
        providerAccount.setProviderUserId(discordUser.user_id());
        providerAccountRepository.save(providerAccount);

        return jwtService.generateToken(account.getId());
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

   public void resetPassword(ResetPasswordRequest request) {
        String tokenHash = DigestUtils.sha256Hex(request.token());

        ResetPasswordTokenEntity entity = resetPasswordTokenRepository.findByTokenHash(tokenHash).orElseThrow(() -> new UnauthorizedException("Token hash is not found"));
        if (entity.getExpiredAt().isBefore(Instant.now())) throw new UnauthorizedException("Token is expired");

        entity.setUsedAt(Instant.now());
        resetPasswordTokenRepository.save(entity);

        String newPasswordHash = passwordEncoder.encode(request.newPassword());
        AccountEntity account = accountRepository.findByEmail(request.email()).orElseThrow(() -> new AccountNotFound("Account not found"));
        account.setPasswordHash(newPasswordHash);
        accountRepository.save(account);
   }

    public AccountEntity getAccount(String uid) throws AccountNotFoundException {
        AccountEntity account = accountRepository.findById(UUID.fromString(uid)).orElseThrow(() -> new AccountNotFoundException("Account not found"));
        return account;
    }
}
