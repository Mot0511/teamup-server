package ru.matveysuvorov.teamup.teams;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.matveysuvorov.teamup.auth.exceptions.UnauthorizedException;
import ru.matveysuvorov.teamup.chats.ChatService;
import ru.matveysuvorov.teamup.chats.entities.ChatEntity;
import ru.matveysuvorov.teamup.chats.entities.MemberEntity;
import ru.matveysuvorov.teamup.chats.repositories.ChatRepository;
import ru.matveysuvorov.teamup.chats.repositories.MemberRepository;
import ru.matveysuvorov.teamup.games.GameEntity;
import ru.matveysuvorov.teamup.games.GameRepository;
import ru.matveysuvorov.teamup.games.exceptions.GameNotFoundException;
import ru.matveysuvorov.teamup.storage.FileEntity;
import ru.matveysuvorov.teamup.storage.FileRepository;
import ru.matveysuvorov.teamup.storage.services.StorageService;
import ru.matveysuvorov.teamup.teams.dto.AddTeamRequest;
import ru.matveysuvorov.teamup.teams.dto.EditTeamRequest;
import ru.matveysuvorov.teamup.teams.entities.InviteEntity;
import ru.matveysuvorov.teamup.teams.exceptions.*;
import ru.matveysuvorov.teamup.teams.repositories.InviteRepository;
import ru.matveysuvorov.teamup.users.entities.FriendshipEntity;
import ru.matveysuvorov.teamup.users.entities.UserEntity;
import ru.matveysuvorov.teamup.users.exceptions.UserNotFoundException;
import ru.matveysuvorov.teamup.users.repositories.FriendshipRepository;
import ru.matveysuvorov.teamup.users.repositories.UserRepository;

import java.lang.reflect.Member;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class TeamService {

    private final ChatRepository chatRepository;
    private final MemberRepository memberRepository;
    private final GameRepository gameRepository;
    private final ChatService chatService;
    private final InviteRepository inviteRepository;
    private final StorageService storageService;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    public TeamService(
            ChatRepository chatRepository,
            MemberRepository memberRepository, GameRepository gameRepository,
            ChatService chatService,
            InviteRepository inviteRepository,
            StorageService storageService,
            FileRepository fileRepository, UserRepository userRepository, FriendshipRepository friendshipRepository
    ) {
        this.chatRepository = chatRepository;
        this.memberRepository = memberRepository;
        this.gameRepository = gameRepository;
        this.chatService = chatService;
        this.inviteRepository = inviteRepository;
        this.storageService = storageService;
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }

    public List<ChatEntity> getPublicTeams() {
        return chatRepository.findByIsPublic(true, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public List<ChatEntity> getPrivateTeams(String uid) {
        List<MemberEntity> members = memberRepository.findTeamsByUid(uid);
        return members.stream().map(MemberEntity::getChat).toList();
    }

    public ChatEntity getTeam(String uid, Long teamId) {
        if (!chatService.isMember(uid, teamId)) throw new UnauthorizedException("Unauthorized");
        return chatRepository.findById(teamId).orElseThrow(() -> new TeamNotFoundException("Team not found"));
    }

    @Transactional
    public ChatEntity addTeam(String uid, AddTeamRequest request) {
        ChatEntity team = new ChatEntity();
        team.setName(request.name());
        team.setIsTeam(true);
        team.setIsPublic(request.isPublic());

        if (request.gameId() != null) {
            GameEntity game = gameRepository.findById(request.gameId()).orElseThrow(() -> new GameNotFoundException("Game not found"));
            team.setGame(game);
        }
        team.setCreatedAt(Instant.now());
        chatRepository.save(team);

        List<FriendshipEntity> friendships = friendshipRepository.findByFromUserOrToUser(uid, uid).orElseThrow(() -> new UserNotFoundException("Friends not found"));
        List<String> friends = friendships
                .stream()
                .map((friendshipEntity -> friendshipEntity.getFromUser().equals(uid) ? friendshipEntity.getToUser() : friendshipEntity.getFromUser())).toList();

        for (String member_id : request.members()) {
            if (friends.contains(member_id) || member_id.equals(uid)) {
                UserEntity user = userRepository.findByUid(member_id).orElseThrow(() -> new UserNotFoundException("User not found"));
                MemberEntity member = new MemberEntity();
                member.setChat(team);
                member.setMember(user);
                member.setCreatedAt(Instant.now());
                memberRepository.save(member);
            }
        }

        return team;
    }

    @Transactional
    public ChatEntity editTeam(String uid, EditTeamRequest request) {
        if (!chatService.isMember(uid, request.teamId())) throw new UnauthorizedException("Unauthorozed");

        ChatEntity team = chatRepository.findById(request.teamId()).orElseThrow(() -> new TeamNotFoundException("Team not found"));
        team.setName(request.newName());

        if (request.gameId() != null) {
            GameEntity game = gameRepository.findById(request.gameId()).orElseThrow(() -> new GameNotFoundException("Game not found"));
            team.setGame(game);
        }

        chatRepository.save(team);

        for (String userId : request.addedMembers()) {
            UserEntity user = userRepository.findByUid(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
            MemberEntity member = new MemberEntity();
            member.setChat(team);
            member.setMember(user);
            member.setCreatedAt(Instant.now());
            memberRepository.save(member);
        }

        for (String userId : request.removedMembers()) {
            memberRepository.deleteByChatIdAndMemberId(team.getId(), userId);
        }

        return team;
    }

    @Transactional
    public void deleteTeam(String uid, Long teamId) {
        if (!chatService.isMember(uid, teamId)) throw new UnauthorizedException("Unauthorozed");
        if (chatRepository.countById(teamId) != 1) throw new TooManyMembersException("Too many members of team to delete");

        ChatEntity team = chatRepository.findById(teamId).orElseThrow(() -> new TeamNotFoundException("Team not found"));

        memberRepository.deleteByChatId(teamId);
        chatRepository.delete(team);
        if (team.getIcon() != null) {
            storageService.delete(team.getIcon().getStorageKey());
        }
    }

    @Transactional
    public void addMembers(String uid, List<String> memberIds, Long teamId) {
        if (!chatService.isMember(uid, teamId)) throw new UnauthorizedException("Unauthorozed");
        ChatEntity team = getTeam(uid, teamId);

        for (String memberId : memberIds) {
            if (memberId == uid) continue;
            UserEntity user = userRepository.findByUid(memberId).orElseThrow(() -> new UserNotFoundException("User not found"));
            MemberEntity member = new MemberEntity();
            member.setChat(team);
            member.setMember(user);
            member.setCreatedAt(Instant.now());
            memberRepository.save(member);
        }

    }

    public String getInviteLink(String uid, Long teamId) {
        if (!chatRepository.existsById(teamId)) throw new TeamNotFoundException("Team not found");
        if (!chatService.isMember(uid, teamId)) throw new UnauthorizedException("Unauthorozed");

        int number = (int) (Math.random() * 1000000);
        InviteEntity invite = inviteRepository.findByTeamId(teamId).orElse(new InviteEntity());
        invite.setNumber(number);
        invite.setTeamId(teamId);
        invite.setExpiredAt(Instant.now().plus(1, ChronoUnit.HOURS));
        inviteRepository.save(invite);

        return "https://192.168.0.71:8080/"+teamId+"/invitation/"+number+"/accept";
    }

    public ChatEntity acceptInvitation(String uid, Long teamId, Integer number) {
        System.out.println(1);
        InviteEntity invite = inviteRepository.findByTeamIdAndNumber(teamId, number).orElseThrow(() -> new InviteNotFoundException("Invite not found"));
        System.out.println(2);
        if (invite.getExpiredAt().isBefore(Instant.now())) throw new InviteExpiredException("Invite expired");

        System.out.println(3);
        ChatEntity team = chatRepository.findById(teamId).orElseThrow(() -> new TeamNotFoundException("Team not found"));
        UserEntity user = userRepository.findByUid(uid).orElseThrow(() -> new UserNotFoundException("User not found"));
        MemberEntity member = new MemberEntity();
        member.setChat(team);
        member.setMember(user);
        member.setCreatedAt(Instant.now());
        memberRepository.save(member);
        System.out.println(4);

        return team;
    }

    public void uploadIcon(String uid, Long teamId, MultipartFile file) {
        if (!chatService.isMember(uid, teamId)) throw new UnauthorizedException("Unauthorized");

        UUID id = UUID.randomUUID();
        String key = "team_icons/"+id;

        storageService.upload(file, key);

        try {
            FileEntity fileEntity = new FileEntity();
            fileEntity.setId(UUID.randomUUID());
            fileEntity.setUploaderId(uid);
            fileEntity.setStorageKey(key);
            fileEntity.setOriginalFilename(file.getOriginalFilename());
            fileEntity.setSize(file.getSize());
            fileEntity.setContentType(file.getContentType());
            fileEntity.setUploadedAt(Instant.now());
            fileRepository.save(fileEntity);

            ChatEntity team = chatRepository.findById(teamId).orElseThrow(() -> new TeamNotFoundException("Team not found"));
            team.setIcon(fileEntity);
            chatRepository.save(team);
            System.out.println("Team icon uploaded");
        } catch (Exception e) {
            storageService.delete(key);
            throw e;
        }
    }

    public String getIconUrl(String uid, Long teamId) {
        ChatEntity team = chatRepository.findById(teamId).orElseThrow(() -> new TeamNotFoundException("Team not found"));
        if (!team.isPublic() && !chatService.isMember(uid, teamId)) throw new UnauthorizedException("Unauthorized");
        if (team.getIcon() == null) throw new IconNotFoundException("Team doesn't have an icon");

        String url = storageService.getPresignedUrl(team.getIcon().getStorageKey());
        return url;
    }
}
