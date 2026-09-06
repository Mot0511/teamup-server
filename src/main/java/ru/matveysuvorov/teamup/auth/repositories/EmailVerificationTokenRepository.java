package ru.matveysuvorov.teamup.auth.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.matveysuvorov.teamup.auth.entities.EmailVerificationTokenEntity;

import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationTokenEntity, Integer> {

    Optional<EmailVerificationTokenEntity> findByTokenHash(String tokenHash);
    void deleteAllByAccountId(UUID account_id);
}
