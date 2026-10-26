package ru.matveysuvorov.teamup.stats;

import org.springframework.stereotype.Service;
import org.springframework.web.server.NotAcceptableStatusException;
import ru.matveysuvorov.teamup.chats.repositories.ChatRepository;
import ru.matveysuvorov.teamup.stats.dto.Stats;
import ru.matveysuvorov.teamup.users.entities.UserEntity;
import ru.matveysuvorov.teamup.users.exceptions.UserNotFoundException;
import ru.matveysuvorov.teamup.users.repositories.UserRepository;

import java.time.Instant;

@Service
public class StatsService {

    private final UserRepository userRepository;
    private final ChatRepository chatRepository;

    public StatsService(UserRepository userRepository, ChatRepository chatRepository) {
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
    }

    public Stats getStats(String uid, Long period) {
        UserEntity admin = userRepository.findByUid(uid).orElseThrow(() -> new UserNotFoundException("User not found"));
        if (!admin.getUsername().equals("Matvey")) throw new NotAcceptableStatusException("Not accepted");

        Instant minDate = Instant.now().minusMillis(period);

        Long userCount = userRepository.countByCreatedAtAfter(minDate);
        Long teamsCount = chatRepository.countByIsTeamAndCreatedAtAfter(true, minDate);
        Long publicTeamsCount = chatRepository.countByIsPublicAndCreatedAtAfter(true, minDate);

        return new Stats(
                userCount,
                teamsCount,
                publicTeamsCount
        );
    }

    public Stats getStats(String uid) {
        UserEntity admin = userRepository.findByUid(uid).orElseThrow(() -> new UserNotFoundException("User not found"));
        if (!admin.getUsername().equals("Matvey")) throw new NotAcceptableStatusException("Not accepted");

        Long userCount = userRepository.count();
        Long teamsCount = chatRepository.countByIsTeam(true);
        Long publicTeamsCount = chatRepository.countByIsPublic(true);

        return new Stats(
                userCount,
                teamsCount,
                publicTeamsCount
        );
    }
}
