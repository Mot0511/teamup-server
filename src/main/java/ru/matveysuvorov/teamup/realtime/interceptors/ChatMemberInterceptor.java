package ru.matveysuvorov.teamup.realtime.interceptors;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import ru.matveysuvorov.teamup.auth.exceptions.UnauthorizedException;
import ru.matveysuvorov.teamup.chats.ChatService;
import ru.matveysuvorov.teamup.chats.entities.MemberEntity;
import ru.matveysuvorov.teamup.chats.repositories.MemberRepository;

import java.security.Principal;
import java.util.List;


@Component
public class ChatMemberInterceptor implements ChannelInterceptor {

    private final MemberRepository memberRepository;

    public ChatMemberInterceptor(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel
    ) throws AccessDeniedException {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(
                        message,
                        StompHeaderAccessor.class
                );

        if (accessor == null) return message;

        Principal auth = accessor.getUser();
        if (auth == null) throw new UnauthorizedException("Unauthorized");

        String uid = auth.getName();

        String destination = accessor.getDestination();
        if (destination == null) return message;

        if (destination.startsWith("/realtime/chats")) {
            Long chatId = Long.parseLong(destination.split("/")[2]);
            List<MemberEntity> members = memberRepository.findByMemberUid(uid);
            List<Long> chat_ids = members.stream().map(member -> member.getChat().getId()).toList();
            if (!chat_ids.contains(chatId)) {
                throw new AccessDeniedException("User is not a member of this chat");
            }

        }

        return message;
    }

}
