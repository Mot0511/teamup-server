package ru.matveysuvorov.teamup.users.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.matveysuvorov.teamup.users.entities.UserEntity;

import javax.swing.text.html.Option;
import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, String> {

    Optional<UserEntity> findByUid(String uid);
    boolean existsByUid(String uid);
    boolean existsByUsername(String username);

}
