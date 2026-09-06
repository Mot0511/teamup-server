package ru.matveysuvorov.teamup.auth.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.matveysuvorov.teamup.auth.entities.AccountEntity;
import ru.matveysuvorov.teamup.auth.repositories.AccountRepository;

import java.io.IOException;
import java.util.UUID;

@Component
public class EmailVerificationFilter extends OncePerRequestFilter {

    private final AccountRepository accountRepository;

    public EmailVerificationFilter(
            AccountRepository accountRepository
    ) {
        this.accountRepository = accountRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            UUID uid = UUID.fromString(auth.getName());

            AccountEntity account = accountRepository.findById(uid).orElse(null);

            if (account == null || !account.isEnabled() || !account.isEmailConfirmed()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

}
