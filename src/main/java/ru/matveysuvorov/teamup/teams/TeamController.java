package ru.matveysuvorov.teamup.teams;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.matveysuvorov.teamup.chats.ChatService;
import ru.matveysuvorov.teamup.chats.entities.ChatEntity;
import ru.matveysuvorov.teamup.teams.dto.AddTeamRequest;
import ru.matveysuvorov.teamup.teams.dto.EditTeamRequest;
import ru.matveysuvorov.teamup.users.entities.UserEntity;

import java.util.List;

@RestController
@RequestMapping("/teams")
public class TeamController {

    private final TeamService teamService;
    private final ChatService chatService;

    public TeamController(TeamService teamService, ChatService chatService) {
        this.teamService = teamService;
        this.chatService = chatService;
    }

    @GetMapping("/public")
    public List<ChatEntity> getPublicTeams() {
        return teamService.getPublicTeams();
    }

    @GetMapping("/")
    public List<ChatEntity> getPrivateTeams(
            Authentication auth
    ) {
        return teamService.getPrivateTeams(auth.getName());
     }

    @PostMapping("/")
    public ChatEntity addTeam(
            Authentication auth,
            @RequestBody AddTeamRequest request
    ) {
        return teamService.addTeam(auth.getName(), request);
    }

    @PutMapping("/")
    public ChatEntity editTeam(
            Authentication auth,
            @RequestBody EditTeamRequest request
    ) {
        return teamService.editTeam(auth.getName(), request);
    }

    @DeleteMapping("/{teamId}")
    public void deleteTeam(
            Authentication auth,
            @PathVariable Long teamId
    ) {
        teamService.deleteTeam(auth.getName(), teamId);
    }

    @PostMapping("/{teamId}/icon")
    public void uploadIcon(
            Authentication auth,
            @PathVariable Long teamId,
            @RequestParam("file") MultipartFile file
    ) {
        teamService.uploadIcon(auth.getName(), teamId, file);
    }

    @GetMapping("/{teamId}/icon")
    public String getIconUrl(
            Authentication auth,
            @PathVariable Long teamId
    ) {
        return teamService.getIconUrl(auth.getName(), teamId);
    }


}
