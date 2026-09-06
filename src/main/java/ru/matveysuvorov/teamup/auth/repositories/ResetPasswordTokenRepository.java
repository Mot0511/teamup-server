package ru.matveysuvorov.teamup.auth.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.matveysuvorov.teamup.auth.entities.ResetPasswordTokenEntity;

import java.util.Optional;

public interface ResetPasswordTokenRepository extends JpaRepository<ResetPasswordTokenEntity, Integer> {

    Optional<ResetPasswordTokenEntity> findByTokenHash(String tokenHash);

}
