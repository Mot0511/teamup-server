package ru.matveysuvorov.teamup.notifications.services;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.matveysuvorov.teamup.chats.entities.MemberEntity;
import ru.matveysuvorov.teamup.chats.repositories.MemberRepository;
import ru.matveysuvorov.teamup.notifications.entities.FcmTokenEntity;
import ru.matveysuvorov.teamup.notifications.repositories.FcmTokenRepository;
import ru.matveysuvorov.teamup.users.entities.UserEntity;
import ru.matveysuvorov.teamup.users.repositories.UserRepository;

import java.util.List;
import java.util.Map;

@Service
public class FcmService {

    private static final Logger log = LoggerFactory.getLogger(FcmService.class);

    private final FcmTokenRepository fcmTokenRepository;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;

    public FcmService(
            FcmTokenRepository fcmTokenRepository,
            UserRepository userRepository,
            MemberRepository memberRepository
    ) {
        this.fcmTokenRepository = fcmTokenRepository;
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
    }

    /**
     * Универсальный метод отправки уведомления пользователю по всем его FCM-токенам.
     * Не проверяет онлайн-статус — используется другими методами как низкоуровневый примитив.
     */
    public void sendToUser(String uid, String title, String body, Map<String, String> data) {
        List<String> tokens = fcmTokenRepository.findByUid(uid)
                .stream()
                .map(FcmTokenEntity::getFcmToken)
                .toList();

        for (String token : tokens) {
            sendToToken(token, title, body, data);
        }
    }

    /**
     * Отправка уведомления о входящем сообщении всем офлайн-участникам чата,
     * кроме отправителя.
     */
    public void sendChatMessageNotification(Long chatId, String senderUid, String senderName, String text) {
        List<MemberEntity> members = memberRepository.findByChatId(chatId);

        members.stream()
                .map(MemberEntity::getMember)
                .filter(memberUid -> !memberUid.equals(senderUid))
                .filter(this::isOffline)
                .forEach(member -> sendToUser(
                        member.getUid(),
                        senderName,
                        text,
                        Map.of("type", "new_message", "chatId", String.valueOf(chatId))
                ));
    }

    /**
     * Отправка уведомления о заявке в друзья офлайн-получателю (toUser).
     */
    public void sendFriendRequestNotification(UserEntity toUser, UserEntity fromUser) {
        if (isOffline(toUser)) {
            sendToUser(
                    toUser.getUid(),
                    "Новая заявка в друзья",
                    fromUser.getUsername() + " хочет добавить вас в друзья",
                    Map.of("type", "friend_request", "fromUser", fromUser.getUid())
            );
        }
    }

    /**
     * Отправка уведомления о завершении поиска всем офлайн-участникам найденной группы.
     */
    public void sendSearchCompleteNotification(List<UserEntity> members) {
        members.stream()
                .filter(this::isOffline)
                .forEach(member -> sendToUser(
                        member.getUid(),
                        "Поиск завершён",
                        "Команда найдена! Соберитесь и начните играть вместе.",
                        Map.of("type", "search_complete", "uid", member.getUid())
                ));
    }

    /**
     * Регистрация (обновление) FCM-токена пользователя.
     */
    public void registerToken(String uid, String fcmToken) {
        if (fcmToken == null || fcmToken.isBlank()) {
            return;
        }
        if (fcmTokenRepository.existsByFcmToken(fcmToken)) {
            return;
        }

        FcmTokenEntity entity = new FcmTokenEntity();
        entity.setUid(uid);
        entity.setFcmToken(fcmToken);
        fcmTokenRepository.save(entity);
    }

    /**
     * Удаление (отзыв) FCM-токена пользователя.
     */
    public void removeToken(String fcmToken) {
        if (fcmToken == null) {
            return;
        }
        fcmTokenRepository.deleteByFcmToken(fcmToken);
    }

    private void sendToToken(String token, String title, String body, Map<String, String> data) {
        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putAllData(data)
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            if ("registration-token-not-registered".equals(e.getMessagingErrorCode())
                    || "invalid-argument".equals(e.getMessagingErrorCode())) {
                log.warn("Removing invalid FCM token with error: {}", e.getMessagingErrorCode());
                removeToken(token);
            } else {
                log.warn("Failed to send FCM notification: {}", e.getMessage());
            }
        }
    }

    private boolean isOffline(UserEntity member) {
        return userRepository.findByUid(member.getUid())
                .map(user -> !Boolean.TRUE.equals(user.isOnline()))
                .orElse(true);
    }
}
