package ru.matveysuvorov.teamup.search;

import jakarta.mail.search.SearchException;
import org.apache.catalina.User;
import org.springframework.expression.spel.ast.OpAnd;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import ru.matveysuvorov.teamup.chats.ChatService;
import ru.matveysuvorov.teamup.chats.entities.ChatEntity;
import ru.matveysuvorov.teamup.messages.enums.MessageEvent;
import ru.matveysuvorov.teamup.notifications.services.FcmService;
import ru.matveysuvorov.teamup.search.dto.*;
import ru.matveysuvorov.teamup.search.enums.SearchEvent;
import ru.matveysuvorov.teamup.search.enums.SearchResult;
import ru.matveysuvorov.teamup.search.exceptions.SearchNotFoundException;
import ru.matveysuvorov.teamup.search.models.Search;
import ru.matveysuvorov.teamup.teams.TeamService;
import ru.matveysuvorov.teamup.teams.dto.AddTeamRequest;
import ru.matveysuvorov.teamup.users.UserService;
import ru.matveysuvorov.teamup.users.entities.UserEntity;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class SearchService {

    private final Map<Long, Map<Integer, Map<Integer, Map<String, Search>>>> tree = new HashMap<>();
    private final Map<UUID, Search> searches = new HashMap<>();

    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;
    private final TeamService teamService;
    private final FcmService fcmService;

    public SearchService(UserService userService, SimpMessagingTemplate messagingTemplate, TeamService teamService, FcmService fcmService) {
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
        this.teamService = teamService;
        this.fcmService = fcmService;
    }

    public SearchResponse getSearch(String uid, SearchRequest request) {
        Map<Integer, Map<Integer, Map<String, Search>>> gameBranch = tree.get(request.game());
        if (gameBranch == null) return createSearch(uid, request);
        Map<Integer, Map<String, Search>> ageBranch = gameBranch.get(request.age());
        if (ageBranch == null) return createSearch(uid, request);
        Map<String, Search> teamSizeBranch = ageBranch.get(request.size());
        if (teamSizeBranch == null) return createSearch(uid, request);
        Search search = teamSizeBranch.get(request.gender());
        if (search == null) return createSearch(uid, request);


        UserEntity user = userService.getUser(uid);
        search.addUser(user);

        broadcast(search.getId(), SearchEvent.NEW_USER, user);

        if (search.getUsers().size() == search.getSize()) {
            ChatEntity team = teamService.addTeam(
                    uid,
                    new AddTeamRequest(
                            "Быстрая команда",
                            true,
                            request.game()
                    )
            );
            teamService.addMembers(uid, search.getUsers().stream().map(UserEntity::getUid).toList(), team.getId());
            broadcast(search.getId(), SearchEvent.FORMED, team);

            List<UserEntity> memberUids = search.getUsers();
            fcmService.sendSearchCompleteNotification(memberUids);

            return new SearchFormedResponse(team);
        }

        return new SearchFoundResponse(search);
    }

    private SearchResponse createSearch(String uid, SearchRequest request) {
        System.out.println("Creating search");
        Search search = new Search();
        search.setAge(request.age());
        search.setGame(request.game());
        search.setSize(request.size());
        search.setGender(request.gender());

        UserEntity user = userService.getUser(uid);
        search.addUser(user);

        searches.put(search.getId(), search);

        Map<Integer, Map<Integer, Map<String, Search>>> ageBranch =
                tree.computeIfAbsent(request.game(), k -> new HashMap<>());

        Map<Integer, Map<String, Search>> teamSizeBranch =
                ageBranch.computeIfAbsent(request.age(), k -> new HashMap<>());

        Map<String, Search> genderBranch =
                teamSizeBranch.computeIfAbsent(request.size(), k -> new HashMap<>());

        genderBranch.put(request.gender(), search);


        return new SearchCreatedResponse(search);
    }

    public void leaveSearch(String uid, UUID searchId) {
        Search search = searches.get(searchId);
        if (search == null) throw new SearchNotFoundException("Search not found");
        search.removeUser(uid);

        broadcast(search.getId(), SearchEvent.LEAVE_USER, uid);
        if (search.getUsers().isEmpty()) {
            searches.remove(searchId);
            tree.get(search.getGame()).get(search.getAge()).get(search.getSize()).remove(search.getGender());
        }
    }

    private void broadcast(UUID id, SearchEvent event, @Nullable Object payload) {
        messagingTemplate.convertAndSend(
                "/topic/searches/"+id,
                new SearchMessage(
                        event,
                        payload
                )
        );
    }

}
