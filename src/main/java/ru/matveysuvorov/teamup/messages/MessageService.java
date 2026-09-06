package ru.matveysuvorov.teamup.messages;

import jakarta.transaction.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.matveysuvorov.teamup.auth.exceptions.UnauthorizedException;
import ru.matveysuvorov.teamup.chats.ChatService;
import ru.matveysuvorov.teamup.chats.entities.ChatEntity;
import ru.matveysuvorov.teamup.chats.exceptions.ChatNotFoundException;
import ru.matveysuvorov.teamup.chats.repositories.ChatRepository;
import ru.matveysuvorov.teamup.messages.dto.Message;
import ru.matveysuvorov.teamup.messages.dto.MessageResponse;
import ru.matveysuvorov.teamup.messages.dto.MessageResponseById;
import ru.matveysuvorov.teamup.messages.entities.MessageEntity;
import ru.matveysuvorov.teamup.messages.entities.MessageReadEntity;
import ru.matveysuvorov.teamup.messages.enums.MessageEvent;
import ru.matveysuvorov.teamup.messages.exceptions.AttachmentNotFoundException;
import ru.matveysuvorov.teamup.messages.exceptions.MessageNotFoundException;
import ru.matveysuvorov.teamup.messages.repositories.MessageReadRepository;
import ru.matveysuvorov.teamup.messages.repositories.MessageRepository;
import ru.matveysuvorov.teamup.notifications.services.FcmService;
import ru.matveysuvorov.teamup.storage.FileEntity;
import ru.matveysuvorov.teamup.storage.FileRepository;
import ru.matveysuvorov.teamup.storage.services.StorageService;
import ru.matveysuvorov.teamup.users.entities.UserEntity;
import ru.matveysuvorov.teamup.users.exceptions.UserNotFoundException;
import ru.matveysuvorov.teamup.users.repositories.UserRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final MessageReadRepository messageReadRepository;
    private final ChatRepository chatRepository;
    private final ChatService chatService;
    private final StorageService storageService;
    private final FileRepository fileRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final FcmService fcmService;
    private final UserRepository userRepository;

    public MessageService(
            MessageRepository messageRepository,
            MessageReadRepository messageReadRepository,
            ChatService chatService,
            ChatRepository chatRepository,
            StorageService storageService,
            FileRepository fileRepository,
            SimpMessagingTemplate messagingTemplate,
            FcmService fcmService,
            UserRepository userRepository
    ) {
        this.messageRepository = messageRepository;
        this.messageReadRepository = messageReadRepository;
        this.chatService = chatService;
        this.chatRepository = chatRepository;
        this.storageService = storageService;
        this.fileRepository = fileRepository;
        this.messagingTemplate = messagingTemplate;
        this.fcmService = fcmService;
        this.userRepository = userRepository;
    }

    public boolean isSender(String uid, Integer messageId) {
        MessageEntity message = messageRepository.findById(messageId).orElseThrow(() -> new MessageNotFoundException("Message not found"));
        return message.getUid() == uid;
    }

    public List<MessageEntity> getMessages(String uid, Integer chatId) {
        if (!chatService.isMember(uid, chatId)) throw new UnauthorizedException("Unauthorized");
        if (!chatRepository.existsById(chatId)) throw new ChatNotFoundException("Chat not found");

        return messageRepository.findByChatId(chatId);
    }

    public void sendMessage(String uid, Integer chatId, Message request) {
        if (!chatService.isMember(uid, chatId)) throw new UnauthorizedException("Unauthorized");

        ChatEntity chat = chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException("Chat not found"));

        MessageEntity message = new MessageEntity();
        message.setChat(chat);
        message.setUid(uid);
        message.setText(request.text());
        message.setSendedAt(LocalDateTime.parse(request.localDateTime()).atZone(ZoneId.of(request.zoneId())).toInstant());

        request.repliedMessage().ifPresent(
                messageId -> messageRepository.findById(messageId).ifPresent(repliedMessage -> message.setRepliedMessage(repliedMessage))
        );

        messageRepository.save(message);

        MessageResponse response = new MessageResponse(MessageEvent.SEND, message);

        messagingTemplate.convertAndSend("/topic/chats/"+chatId, response);

        UserEntity sender = userRepository.findByUid(uid)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        fcmService.sendChatMessageNotification(chatId, uid, sender.getUsername(), request.text());
    }

    public void editMessage(String uid, Integer messageId, Message request) {
        MessageEntity message = messageRepository.findById(messageId).orElseThrow(() -> new MessageNotFoundException("Message not found"));
        if (message.getUid() != uid) throw new UnauthorizedException("Unauthorized");

        message.setText(request.text());
        messageRepository.save(message);
        MessageResponse response = new MessageResponse(MessageEvent.EDIT, message);
        messagingTemplate.convertAndSend("/topic/chats/"+message.getChat().getId(), response);
    }

    @Transactional
    public void deleteMessage(String uid, Integer messageId) {
        MessageEntity message = messageRepository.findById(messageId).orElseThrow(() -> new MessageNotFoundException("Message not found"));
        if (message.getUid() != uid) throw new UnauthorizedException("Unauthorized");
        messageRepository.deleteById(messageId);

        if (message.getAttachment() != null) {
            FileEntity fileEntity = fileRepository.findById(message.getAttachment()).orElseThrow(() -> new AttachmentNotFoundException("Attachment not found"));
            fileRepository.delete(fileEntity);
            storageService.delete(fileEntity.getStorageKey());
        }

        MessageResponseById response = new MessageResponseById(MessageEvent.DELETE, messageId);
        messagingTemplate.convertAndSend("/topic/chats/"+message.getChat().getId(), response);
    }


    public void setReaded(String uid, Integer messageId) {
        MessageEntity message = messageRepository.findById(messageId).orElseThrow(() -> new MessageNotFoundException("Message not found"));
        if (!chatService.isMember(uid, message.getChat().getId()));

        MessageReadEntity messageRead = new MessageReadEntity();
        messageRead.setChatId(message.getChat().getId());
        messageRead.setMessageId(message.getId());
        messageRead.setUserId(uid);
        messageRead.setReadedAt(Instant.now());
        messageReadRepository.save(messageRead);

        MessageResponseById response = new MessageResponseById(MessageEvent.READ, messageId);
        messagingTemplate.convertAndSend("/topic/chats/"+message.getChat().getId(), response);
    }

    @Transactional
    public void uploadAttachment(String uid, Integer messageId, MultipartFile file) {
        MessageEntity message = messageRepository.findById(messageId).orElseThrow(() -> new MessageNotFoundException("Message not found"));
        if (message.getUid() != uid) throw new UnauthorizedException("Unauthorized");

        UUID id = UUID.randomUUID();
        String key = "attachments/"+id;

        storageService.upload(file, key);

        try {
            FileEntity fileEntity = new FileEntity();
            fileEntity.setId(id);
            fileEntity.setStorageKey(key);
            fileEntity.setOriginalFilename(file.getOriginalFilename());
            fileEntity.setContentType(file.getContentType());
            fileEntity.setUploaderId(uid);
            fileEntity.setUploadedAt(Instant.now());
            fileEntity.setSize(file.getSize());
            fileRepository.save(fileEntity);

            message.setAttachment(id);
            messageRepository.save(message);

        } catch (Exception e) {
            storageService.delete(key);
        }
    }

    public String getAttachmentUrl(String uid, Integer messageId) {
        MessageEntity message = messageRepository.findById(messageId).orElseThrow(() -> new MessageNotFoundException("Message not found"));
        if (!chatService.isMember(uid, message.getChat().getId())) throw new UnauthorizedException("Unauthorized");

        FileEntity fileEntity = fileRepository.findById(message.getAttachment()).orElseThrow(() -> new AttachmentNotFoundException("Attachment not found"));
        return storageService.getPresignedUrl(fileEntity.getStorageKey());
    }

}
