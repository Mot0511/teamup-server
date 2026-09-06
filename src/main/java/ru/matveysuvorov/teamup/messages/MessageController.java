package ru.matveysuvorov.teamup.messages;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.matveysuvorov.teamup.messages.dto.Message;
import ru.matveysuvorov.teamup.messages.entities.MessageEntity;

import java.util.List;

@Controller
@RestController("/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(
            MessageService messageService
    ) {
        this.messageService = messageService;
    }


    @GetMapping("/{chatId}")
    public List<MessageEntity> getMessages(
            Authentication auth,
            @PathVariable Integer chatId
    ) {
        return messageService.getMessages(auth.getName(), chatId);
    }

    @MessageMapping("/chats/{chatId}/send")
    public void sendMessage(
            Authentication auth,
            @DestinationVariable Integer chatId,
            @RequestAttribute Message request
    ) {
        messageService.sendMessage(auth.getName(), chatId, request);
    }

    @MessageMapping("/messages/{messageId}/edit")
    public void editMessage(
            Authentication auth,
            @DestinationVariable Integer messageId,
            @RequestAttribute Message request
    ) {
        messageService.editMessage(auth.getName(), messageId, request);
    }

    @MessageMapping("/messages/{messageId}/delete")
    public void deleteMessage(
            Authentication auth,
            @DestinationVariable Integer messageId
    ) {
        messageService.deleteMessage(auth.getName(), messageId);
    }

    @PostMapping("/messages/{messageId}/read")
    public void setReadedMessage(
            Authentication auth,
            @PathVariable Integer messageId
    ) {
        messageService.setReaded(auth.getName(), messageId);
    }

    @PostMapping("/{messageId}/attachment")
    public void uploadAttachment(
            Authentication auth,
            @PathVariable Integer messageId,
            @RequestParam("file") MultipartFile file
    ) {
        messageService.uploadAttachment(auth.getName(), messageId, file);
    }

    @GetMapping("/{messageId}/attachment")
    public String getAttachmentUrl(
            Authentication auth,
            @PathVariable Integer messageId
    ) {
        return messageService.getAttachmentUrl(auth.getName(), messageId);
    }

}
