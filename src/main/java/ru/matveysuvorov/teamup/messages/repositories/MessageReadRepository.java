package ru.matveysuvorov.teamup.messages.repositories;

import jakarta.mail.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.matveysuvorov.teamup.messages.entities.MessageReadEntity;

import java.util.List;

public interface MessageReadRepository extends JpaRepository<MessageReadEntity, Integer> {

    List<MessageReadEntity> findByChatId(Integer chatId);

}
