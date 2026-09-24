package ru.matveysuvorov.teamup.chats.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.matveysuvorov.teamup.chats.entities.ChatEntity;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<ChatEntity, Integer> {

    Optional<ChatEntity> findById(Long id);
    List<ChatEntity> findByIsPublic(boolean isPublic, Sort sort);
    boolean existsById(Long id);
    long countById(Long id);
}
