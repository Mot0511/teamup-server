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
import ru.matveysuvorov.teamup.messages.dto.MessageRequest;
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
import ru.matveysuvorov.teamup.teams.exceptions.TeamNotFoundException;
import ru.matveysuvorov.teamup.users.entities.UserEntity;
import ru.matveysuvorov.teamup.users.exceptions.UserNotFoundException;
import ru.matveysuvorov.teamup.users.repositories.UserRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
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

    public boolean isSender(String uid, Long messageId) {
        MessageEntity message = messageRepository.findById(messageId).orElseThrow(() -> new MessageNotFoundException("Message not found"));
        return Objects.equals(message.getUid(), uid);
    }

    public List<MessageEntity> getMessages(String uid, Long chatId) {
        ChatEntity chat = chatRepository.findById(chatId).orElseThrow(() -> new TeamNotFoundException("Chat not found"));
        if (!chatService.isMember(uid, chatId) && !chat.getPublic()) throw new UnauthorizedException("Unauthorized");

        return messageRepository.findByChatId(chatId);
    }

    public MessageEntity getLastMessage(String uid, Long chatId) {
        if (!chatService.isMember(uid, chatId)) throw new UnauthorizedException("Unauthorized");
        if (!chatRepository.existsById(chatId)) throw new ChatNotFoundException("Chat not found");

        return messageRepository.findFirstByChatIdOrderBySendedAtDesc(chatId)
                .orElseThrow(() -> new MessageNotFoundException("Messages not found"));
    }

    public MessageEntity sendMessage(String uid, Long chatId, MessageRequest request) {
        if (!chatService.isMember(uid, chatId)) throw new UnauthorizedException("Unauthorized");

        ChatEntity chat = chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException("Chat not found"));

        Message data = request.payload();
        MessageEntity message = new MessageEntity();
        message.setChat(chat);
        message.setUid(uid);
        message.setText(data.text());
        message.setSendedAt(LocalDateTime.parse(data.localDateTime()).atZone(ZoneId.of(data.zoneId())).toInstant());

        data.repliedMessage().ifPresent(
                messageId -> messageRepository.findById(messageId).ifPresent(repliedMessage -> message.setRepliedMessage(repliedMessage))
        );

        MessageEntity newMessage = messageRepository.save(message);

        UserEntity sender = userRepository.findByUid(uid)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        fcmService.sendChatMessageNotification(chatId, uid, sender.getUsername(), data.text());

        return newMessage;
    }

    public MessageEntity editMessage(String uid, MessageRequest request) {
        Long messageId = request.payload().id().orElseThrow(() -> new IllegalArgumentException("Message id can't be null"));
        MessageEntity message = messageRepository.findById(messageId).orElseThrow(() -> new MessageNotFoundException("Message not found"));
        if (!Objects.equals(message.getUid(), uid)) throw new UnauthorizedException("Unauthorized");

        message.setText(request.payload().text());
        return messageRepository.save(message);
    }

    @Transactional
    public MessageEntity deleteMessage(String uid, MessageRequest request) {
        Long messageId = request.payload().id().orElseThrow(() -> new IllegalArgumentException("Message id can't be null"));
        MessageEntity message = messageRepository.findById(messageId).orElseThrow(() -> new MessageNotFoundException("Message not found"));
        if (!Objects.equals(message.getUid(), uid)) throw new UnauthorizedException("Unauthorized");
        messageRepository.deleteById(messageId);

        if (message.getAttachment() != null) {
            FileEntity fileEntity = fileRepository.findById(message.getAttachment()).orElseThrow(() -> new AttachmentNotFoundException("Attachment not found"));
            fileRepository.delete(fileEntity);
            storageService.delete(fileEntity.getStorageKey());
        }

        return message;
    }


    public MessageEntity setReaded(String uid, MessageRequest request) {
        Long messageId = request.payload().id().orElseThrow(() -> new IllegalArgumentException("Message id can't be null"));
        MessageEntity message = messageRepository.findById(messageId).orElseThrow(() -> new MessageNotFoundException("Message not found"));
        if (!chatService.isMember(uid, message.getChat().getId()));

        MessageReadEntity messageRead = new MessageReadEntity();
        messageRead.setChatId(message.getChat().getId());
        messageRead.setMessageId(message.getId());
        messageRead.setUserId(uid);
        messageRead.setReadedAt(Instant.now());
        messageReadRepository.save(messageRead);

        return message;
    }

    @Transactional
    public void uploadAttachment(String uid, Long messageId, MultipartFile file) {
        System.out.println(messageId);
        MessageEntity message = messageRepository.findById(messageId).orElseThrow(() -> new MessageNotFoundException("Message not found"));
        System.out.println(messageId);
        if (message.getUid() != uid) throw new UnauthorizedException("Unauthorized");

        UUID id = UUID.randomUUID();
        String key = "attachments/"+id;
        storageService.upload(file, key);

        System.out.println(messageId);
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
            throw e;
        }
    }

    public String getAttachmentUrl(String uid, Long messageId) {
        MessageEntity message = messageRepository.findById(messageId).orElseThrow(() -> new MessageNotFoundException("Message not found"));
        if (!chatService.isMember(uid, message.getChat().getId())) throw new UnauthorizedException("Unauthorized");

        FileEntity fileEntity = fileRepository.findById(message.getAttachment()).orElseThrow(() -> new AttachmentNotFoundException("Attachment not found"));
        return storageService.getPresignedUrl(fileEntity.getStorageKey());
    }

}
