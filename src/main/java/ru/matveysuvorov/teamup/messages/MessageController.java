package ru.matveysuvorov.teamup.messages;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.matveysuvorov.teamup.messages.dto.MessageRequest;
import ru.matveysuvorov.teamup.messages.dto.MessageResponse;
import ru.matveysuvorov.teamup.messages.entities.MessageEntity;
import ru.matveysuvorov.teamup.messages.enums.MessageEvent;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messaging;

    public MessageController(
            MessageService messageService, SimpMessagingTemplate messaging
    ) {
        this.messageService = messageService;
        this.messaging = messaging;
    }


    @GetMapping("/{chatId}")
    public List<MessageEntity> getMessages(
            Authentication auth,
            @PathVariable Long chatId
    ) {
        return messageService.getMessages(auth.getName(), chatId);
    }

    @GetMapping("/{chatId}/last")
    public MessageEntity getLastMessage(
            Authentication auth,
            @PathVariable Long chatId
    ) {
        return messageService.getLastMessage(auth.getName(), chatId);
    }

    @MessageMapping("/chats/{chatId}")
    @Transactional
    public void sendMessage(
            Authentication auth,
            @DestinationVariable Long chatId,
            @Payload MessageRequest request
    ) {

        MessageEvent event = request.event();
        MessageResponse response = new MessageResponse();
        response.setEvent(event);
        MessageEntity message = switch (event) {
            case MessageEvent.SEND -> messageService.sendMessage(auth.getName(), chatId, request);
            case MessageEvent.EDIT -> messageService.editMessage(auth.getName(), request);
            case MessageEvent.DELETE -> messageService.deleteMessage(auth.getName(), request);
            case MessageEvent.READ -> messageService.setReaded(auth.getName(), request);
        };

        response.setPayload(message);
        messaging.convertAndSend(
                "/topic/chats/"+chatId,
                response
        );

    }

    @PostMapping("/{messageId}/attachment")
    public void uploadAttachment(
            Authentication auth,
            @PathVariable Long messageId,
            @RequestParam("file") MultipartFile file
    ) {
        messageService.uploadAttachment(auth.getName(), messageId, file);
    }

    @GetMapping("/{messageId}/attachment")
    public String getAttachmentUrl(
            Authentication auth,
            @PathVariable Long messageId
    ) {
        return messageService.getAttachmentUrl(auth.getName(), messageId);
    }

}
