package ru.matveysuvorov.teamup.chats;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.matveysuvorov.teamup.chats.entities.ChatEntity;

import java.util.List;

@Controller
@RequestMapping("/chats")
public class ChatController {

    private final ChatService chatService;

    public ChatController(
            ChatService chatService
    ) {
        this.chatService = chatService;
    }

    @GetMapping("/")
    public List<ChatEntity> getChats(
            Authentication auth
    ) {
        return chatService.getChats(auth.getName());
    }

    @PostMapping("/")
    public void addChat(
            Authentication auth,
            @RequestParam String other
    ) {
        chatService.addChat(auth.getName(), other);
    }

    @GetMapping("/{chatId}")
    public ChatEntity getChat(
            Authentication auth,
            @PathVariable Integer chatId
    ) {
        return chatService.getChat(auth.getName(), chatId);
    }

    @DeleteMapping("/{chatId}")
    public void deleteChat(
            Authentication auth,
            @PathVariable Integer chatId
    ) {
        chatService.deleteChat(auth.getName(), chatId);
    }

    @GetMapping("/{chatId}/reads")
    public List<Integer> getReads(
            Authentication auth,
            @PathVariable Integer chatId
    ) {
        return chatService.getMessageReads(auth.getName(), chatId);
    }


}
