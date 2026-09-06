package ru.matveysuvorov.teamup.auth.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.matveysuvorov.teamup.auth.entities.AccountEntity;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {

    Optional<AccountEntity> findByEmail(String email);
    boolean existsByEmail(String email);
}