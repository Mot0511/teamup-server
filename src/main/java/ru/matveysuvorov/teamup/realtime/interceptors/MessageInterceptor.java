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
import ru.matveysuvorov.teamup.messages.repositories.MessageRepository;

import java.nio.file.AccessDeniedException;
import java.security.Principal;

@Component
public class MessageInterceptor implements ChannelInterceptor {

    private final MessageRepository messageRepository;

    public MessageInterceptor(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel
    ) {;
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(
                        message,
                        StompHeaderAccessor.class
                );

        if (accessor == null) return message;

        Principal auth = accessor.getUser();
        if (auth == null || "anonymousUser".equals(auth.getName())) {
            throw new UnauthorizedException("Unauthorized");
        }

        String destination = accessor.getDestination();
        if (destination == null) return message;

        if (destination.startsWith("/realtime/messages")) {
            Long messageId = Long.parseLong(destination.split("/")[2]);
            boolean isSender = messageRepository.findById(messageId).map(msg -> msg.getUid().equals(auth.getName())).orElse(false);
            if (!isSender) {
                throw new UnauthorizedException("Unauthorized");
            }
        }

        return message;
    }

}
