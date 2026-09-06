package ru.matveysuvorov.teamup.messages.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.matveysuvorov.teamup.messages.entities.MessageEntity;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<MessageEntity, Integer> {

    List<MessageEntity> findByChatId(Integer chatId);
    Optional<MessageEntity> findById(Integer id);
    void deleteById(Integer id);

}
