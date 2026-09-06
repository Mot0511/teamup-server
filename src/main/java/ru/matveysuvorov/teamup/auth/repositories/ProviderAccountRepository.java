package ru.matveysuvorov.teamup.auth.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.matveysuvorov.teamup.auth.entities.ProviderAccountEntity;

import java.util.Optional;

public interface ProviderAccountRepository extends JpaRepository<ProviderAccountEntity, Integer> {

    ProviderAccountEntity findByProviderUserId(String provider_user_id);
    boolean existsByProviderUserId(String provider_user_id);
}