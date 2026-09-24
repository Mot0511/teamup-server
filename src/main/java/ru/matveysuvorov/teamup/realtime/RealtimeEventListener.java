package ru.matveysuvorov.teamup.realtime;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import ru.matveysuvorov.teamup.users.UserService;

import java.security.Principal;

@Component
public class RealtimeEventListener {

    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    public RealtimeEventListener(UserService userService, SimpMessagingTemplate messagingTemplate) {
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        Principal principal = event.getUser();
        if (principal == null) return;

        String uid = principal.getName();
        userService.setOnlineStatus(uid, true);
        messagingTemplate.convertAndSend("/topic/user/"+uid+"/status", true);
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        Principal principal = event.getUser();
        if (principal == null) return;

        String uid = principal.getName();
        userService.setOnlineStatus(uid, false);
        messagingTemplate.convertAndSend("/topic/user/"+uid+"/status", false);
    }

}