package ru.matveysuvorov.teamup.teams;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.matveysuvorov.teamup.auth.exceptions.UnauthorizedException;
import ru.matveysuvorov.teamup.chats.ChatService;
import ru.matveysuvorov.teamup.chats.entities.ChatEntity;
import ru.matveysuvorov.teamup.chats.entities.MemberEntity;
import ru.matveysuvorov.teamup.chats.repositories.ChatRepository;
import ru.matveysuvorov.teamup.chats.repositories.MemberRepository;
import ru.matveysuvorov.teamup.storage.FileEntity;
import ru.matveysuvorov.teamup.storage.FileRepository;
import ru.matveysuvorov.teamup.storage.services.StorageService;
import ru.matveysuvorov.teamup.teams.dto.AddTeamRequest;
import ru.matveysuvorov.teamup.teams.dto.EditTeamRequest;
import ru.matveysuvorov.teamup.teams.entities.InviteEntity;
import ru.matveysuvorov.teamup.teams.exceptions.*;
import ru.matveysuvorov.teamup.teams.repositories.InviteRepository;

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
    private final ChatService chatService;
    private final InviteRepository inviteRepository;
    private final StorageService storageService;
    private final FileRepository fileRepository;

    public TeamService(
            ChatRepository chatRepository,
            MemberRepository memberRepository,
            ChatService chatService,
            InviteRepository inviteRepository,
            StorageService storageService,
            FileRepository fileRepository
    ) {
        this.chatRepository = chatRepository;
        this.memberRepository = memberRepository;
        this.chatService = chatService;
        this.inviteRepository = inviteRepository;
        this.storageService = storageService;
        this.fileRepository = fileRepository;
    }

    public List<ChatEntity> getPublicTeams() {
        return chatRepository.findByIsPublic(true);
    }

    public List<ChatEntity> getPrivateTeams(String uid) {
        List<MemberEntity> members = memberRepository.findTeamsByUid(uid);
        return members.stream().map(MemberEntity::getChat).toList();
    }

    public ChatEntity getTeam(String uid, Integer teamId) {
        if (!chatService.isMember(uid, teamId)) throw new UnauthorizedException("Unauthorized");
        return chatRepository.findById(teamId).orElseThrow(() -> new TeamNotFoundException("Team not found"));
    }

    @Transactional
    public ChatEntity addTeam(String uid, AddTeamRequest request) {
        ChatEntity team = new ChatEntity();
        team.setName(request.name());
        team.setIsTeam(true);
        team.setIsPublic(request.isPublic());
        team.setCreatedAt(Instant.now());
        chatRepository.save(team);

        MemberEntity member = new MemberEntity();
        member.setChat(team);
        member.setMemberId(uid);
        member.setCreatedAt(Instant.now());
        memberRepository.save(member);

        return team;
    }

    @Transactional
    public void editTeam(String uid, EditTeamRequest request) {
        if (!chatService.isMember(uid, request.team_id())) throw new UnauthorizedException("Unauthorozed");

        ChatEntity team = chatRepository.findById(request.team_id()).orElseThrow(() -> new TeamNotFoundException("Team not found"));
        team.setName(request.newName());
        chatRepository.save(team);

        for (String userId : request.addedMembers()) {
            MemberEntity member = new MemberEntity();
            member.setChat(team);
            member.setMemberId(userId);
            member.setCreatedAt(Instant.now());
            memberRepository.save(member);
        }

        for (String userId : request.removedMembers()) {
            memberRepository.deleteByChatIdAndMemberId(team.getId(), userId);
        }
    }

    @Transactional
    public void deleteTeam(String uid, Integer teamId) {
        if (!chatService.isMember(uid, teamId)) throw new UnauthorizedException("Unauthorozed");
        if (chatRepository.countById(teamId) != 1) throw new TooManyMembersException("Too many members of team to delete");

        ChatEntity team = chatRepository.findById(teamId).orElseThrow(() -> new TeamNotFoundException("Team not found"));
        chatRepository.delete(team);

        MemberEntity member = memberRepository.findByChatAndMemberId(team, uid).orElseThrow(() -> new MemberNotFoundException("Member not found"));
        memberRepository.delete(member);
    }

    @Transactional
    public void addMembers(String uid, List<String> memberIds, Integer teamId) {
        if (!chatService.isMember(uid, teamId)) throw new UnauthorizedException("Unauthorozed");
        ChatEntity team = getTeam(uid, teamId);

        for (String memberId : memberIds) {
            if (memberId == uid) continue;
            MemberEntity member = new MemberEntity();
            member.setChat(team);
            member.setMemberId(memberId);
            member.setCreatedAt(Instant.now());
            memberRepository.save(member);
        }

    }

    public String getInviteLink(String uid, Integer teamId) {
        if (!chatService.isMember(uid, teamId)) throw new UnauthorizedException("Unauthorozed");
        if (!chatRepository.existsById(teamId)) throw new TeamNotFoundException("Team not found");

        int number = (int) (Math.random() * 1000000);
        InviteEntity invite = inviteRepository.findByTeamId(teamId).orElse(new InviteEntity());
        invite.setNumber(number);
        invite.setTeamId(teamId);
        invite.setExpiredAt(Instant.now().plus(1, ChronoUnit.HOURS));
        inviteRepository.save(invite);

        return "https://teamupp.ru/invite/"+teamId+"/"+number;
    }

    public void acceptInvitation(String uid, Integer teamId, Integer number) {
        InviteEntity invite = inviteRepository.findByTeamIdAndNumber(teamId, number).orElseThrow(() -> new InviteNotFoundException("Invite not found"));
        if (invite.getExpiredAt().isBefore(Instant.now())) throw new InviteExpiredException("Invite expired");
        ChatEntity team = chatRepository.findById(teamId).orElseThrow(() -> new TeamNotFoundException("Team not found"));
        MemberEntity member = new MemberEntity();
        member.setChat(team);
        member.setMemberId(uid);
        member.setCreatedAt(Instant.now());
        memberRepository.save(member);
    }

    public void uploadIcon(String uid, Integer teamId, MultipartFile file) {
        if (!chatService.isMember(uid, teamId)) throw new UnauthorizedException("Unauthorized");

        UUID id = UUID.randomUUID();
        String key = "team_icons/"+id;

        storageService.upload(file, key);

        try {
            FileEntity fileEntity = new FileEntity();
            fileEntity.setUploaderId(uid);
            fileEntity.setStorageKey(key);
            fileEntity.setOriginalFilename(file.getOriginalFilename());
            fileEntity.setSize(file.getSize());
            fileEntity.setContentType(file.getContentType());
            fileEntity.setUploadedAt(Instant.now());
            fileRepository.save(fileEntity);
        } catch (Exception e) {
            storageService.delete(key);
        }
    }

    public String getIconUrl(String uid, Integer teamId) {
        ChatEntity team = chatRepository.findById(teamId).orElseThrow(() -> new TeamNotFoundException("Team not found"));
        if (!team.isPublic() && !chatService.isMember(uid, teamId)) throw new UnauthorizedException("Unauthorized");
        if (team.getIcon() == null) throw new IconNotFoundException("Team doesn't have an icon");

        return storageService.getPresignedUrl(team.getIcon().getStorageKey());
    }
}
