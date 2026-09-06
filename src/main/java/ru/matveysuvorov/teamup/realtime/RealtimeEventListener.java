package ru.matveysuvorov.teamup.realtime;

import org.springframework.context.event.EventListener;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import ru.matveysuvorov.teamup.users.UserService;

import java.security.Principal;

@Component
public class RealtimeEventListener {

    private final UserService userService;

    public RealtimeEventListener(UserService userService) {
        this.userService = userService;
    }

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        Principal principal = event.getUser();
        if (principal == null) return;

        String uid = principal.getName();
        userService.setOnlineStatus(uid, true);
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        Principal principal = event.getUser();
        if (principal == null) return;

        String uid = principal.getName();
        userService.setOnlineStatus(uid, false);
    }

}