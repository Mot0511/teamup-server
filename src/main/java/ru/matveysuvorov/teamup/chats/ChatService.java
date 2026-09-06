package ru.matveysuvorov.teamup.chats;

import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.matveysuvorov.teamup.chats.entities.ChatEntity;
import ru.matveysuvorov.teamup.chats.entities.MemberEntity;
import ru.matveysuvorov.teamup.chats.exceptions.ChatNotFoundException;
import ru.matveysuvorov.teamup.chats.repositories.ChatRepository;
import ru.matveysuvorov.teamup.chats.repositories.MemberRepository;
import ru.matveysuvorov.teamup.messages.entities.MessageReadEntity;
import ru.matveysuvorov.teamup.messages.repositories.MessageReadRepository;
import ru.matveysuvorov.teamup.users.entities.UserEntity;
import ru.matveysuvorov.teamup.users.exceptions.UserNotFoundException;
import ru.matveysuvorov.teamup.users.repositories.UserRepository;

import java.lang.module.ResolutionException;
import java.lang.reflect.Member;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    private final ChatRepository chatRepository;
    private final MemberRepository memberRepository;
    private final MessageReadRepository messageReadRepository;

    public ChatService(
            ChatRepository chatRepository,
            MemberRepository memberRepository,
            MessageReadRepository messageReadRepository
    ) {
        this.chatRepository = chatRepository;
        this.memberRepository = memberRepository;
        this.messageReadRepository = messageReadRepository;
    }

    public boolean isMember(String uid, Integer chatId) {
        List<MemberEntity> members = memberRepository.findByMemberId(uid);
        List<Integer> chat_ids = members.stream().map(member -> member.getChat().getId()).toList();
        return chat_ids.contains(chatId);
    }

    public List<ChatEntity> getChats(String uid) {
        List<MemberEntity> members = memberRepository.findByMemberId(uid);
        if (members.isEmpty()) return List.of();

        List<ChatEntity> chats = members.stream().map(MemberEntity::getChat).toList();
        return chats;
    }

    public ChatEntity getChat(String uid, Integer chatId) {
        if (!isMember(uid, chatId)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        return chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException("Chat not found"));

    }

    @Transactional
    public void addChat(String uid1, String uid2) {
        ChatEntity chat = new ChatEntity();
        chat.setIsPublic(false);
        chat.setIsTeam(false);
        chat.setCreatedAt(Instant.now());
        chatRepository.save(chat);

        MemberEntity member1 = new MemberEntity();
        member1.setMemberId(uid1);
        member1.setChat(chat);
        member1.setCreatedAt(Instant.now());

        MemberEntity member2 = new MemberEntity();
        member2.setMemberId(uid2);
        member2.setChat(chat);
        member2.setCreatedAt(Instant.now());

        memberRepository.save(member1);
        memberRepository.save(member2);
    }

    @Transactional
    public void deleteChat(String uid, Integer chatId) {
        if (!isMember(uid, chatId)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        ChatEntity chat = chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException("Chat not found"));
        chatRepository.delete(chat);
        memberRepository.deleteByChatId(chatId);
    }

    public List<Integer> getMessageReads(String uid, Integer chatId) {
        if (!isMember(uid, chatId)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        if (!chatRepository.existsById(chatId)) throw new ChatNotFoundException("Chat not found");

        List<MessageReadEntity> messageReads = messageReadRepository.findByChatId(chatId);
        return messageReads.stream().map(MessageReadEntity::getMessageId).toList();
    }

}
