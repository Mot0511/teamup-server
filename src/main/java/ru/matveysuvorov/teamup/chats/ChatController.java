package ru.matveysuvorov.teamup.chats;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.matveysuvorov.teamup.chats.entities.ChatEntity;
import ru.matveysuvorov.teamup.users.entities.UserEntity;

import java.util.List;

@RestController
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

    @GetMapping("/{chatId}/users")
    public List<UserEntity> getUsersByTeam(
            Authentication auth,
            @PathVariable Long chatId
    ) {
        return chatService.getUsersByTeam(auth.getName(), chatId);
    }

    @PostMapping("/")
    public ChatEntity addChat(
            Authentication auth,
            @RequestParam String other
    ) { 
        return chatService.addChat(auth.getName(), other);
    }

    @GetMapping("/{chatId}")
    public ChatEntity getChat(
            Authentication auth,
            @PathVariable Long chatId
    ) {
        return chatService.getChat(auth.getName(), chatId);
    }

    @DeleteMapping("/{chatId}")
    public void deleteChat(
            Authentication auth,
            @PathVariable Long chatId
    ) {
        chatService.deleteChat(auth.getName(), chatId);
    }

    @GetMapping("/{chatId}/reads")
    public List<Long> getReads(
            Authentication auth,
            @PathVariable Long chatId
    ) {
        return chatService.getMessageReads(auth.getName(), chatId);
    }


}
