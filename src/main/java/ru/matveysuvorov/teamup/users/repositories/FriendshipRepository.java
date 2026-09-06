package ru.matveysuvorov.teamup.users.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.matveysuvorov.teamup.users.entities.FriendshipEntity;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<FriendshipEntity, Integer> {

    Optional<FriendshipEntity> findByFromUserAndToUser(String fromUser, String toUser);
    Optional<List<FriendshipEntity>> findByFromUserOrToUser(String from_user, String to_user);
    void deleteByFromUserAndToUser(String fromUser, String toUser);

}
