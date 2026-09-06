package ru.matveysuvorov.teamup.realtime.interceptors;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import ru.matveysuvorov.teamup.auth.exceptions.UnauthorizedException;
import ru.matveysuvorov.teamup.chats.ChatService;
import ru.matveysuvorov.teamup.messages.MessageService;

import java.nio.file.AccessDeniedException;
import java.security.Principal;

@Component
public class MessageInterceptor implements ChannelInterceptor {

    private final MessageService messageService;

    public MessageInterceptor(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel
    ) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(
                        message,
                        StompHeaderAccessor.class
                );

        if (accessor == null) return message;

        Principal auth = accessor.getUser();

        String destination = accessor.getDestination();
        if (destination == null) return message;

        if (destination.startsWith("/realtime/messages")) {
            Integer messageId = Integer.parseInt(destination.split("/")[2]);
            if (!messageService.isSender(auth.getName(), messageId)) {
                throw new UnauthorizedException("Unauthorized");
            }
        }

        return message;
    }

}
