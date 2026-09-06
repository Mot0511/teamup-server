package ru.matveysuvorov.teamup.chats.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.matveysuvorov.teamup.chats.entities.ChatEntity;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<ChatEntity, Integer> {

    Optional<ChatEntity> findById(Integer id);
    List<ChatEntity> findByIsPublic(boolean isPublic);
    boolean existsById(Integer id);
    long countById(Integer id);
}
