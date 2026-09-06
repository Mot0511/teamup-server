package ru.matveysuvorov.teamup.notifications.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.matveysuvorov.teamup.notifications.entities.FcmTokenEntity;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmTokenEntity, Long> {

    List<FcmTokenEntity> findByUid(String uid);

    boolean existsByFcmToken(String fcmToken);

    Optional<FcmTokenEntity> findByFcmToken(String fcmToken);

    void deleteByFcmToken(String fcmToken);
}
