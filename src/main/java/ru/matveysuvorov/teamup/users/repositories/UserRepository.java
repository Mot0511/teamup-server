package ru.matveysuvorov.teamup.users.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.matveysuvorov.teamup.users.entities.UserEntity;

import javax.swing.text.html.Option;
import java.awt.print.Pageable;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, String> {

    Optional<UserEntity> findByUid(String uid);
    Optional<UserEntity> findByUsername(String username);
    boolean existsByUid(String uid);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<UserEntity> findByUsernameContaining(String subString);
    long countByCreatedAtAfter(Instant createdAt);

}
