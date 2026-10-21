package ru.matveysuvorov.teamup.auth.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.matveysuvorov.teamup.auth.entities.EmailVerificationCodeEntity;
import ru.matveysuvorov.teamup.auth.entities.EmailVerificationTokenEntity;

import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCodeEntity, Integer> {

    Optional<EmailVerificationCodeEntity> findByCode(Number code);
    Optional<EmailVerificationCodeEntity> findByAccountId(UUID accountId);
    void deleteAllByAccountId(UUID account_id);
}
