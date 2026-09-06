package ru.matveysuvorov.teamup.users;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.matveysuvorov.teamup.users.dto.FriendshipRequest;
import ru.matveysuvorov.teamup.users.dto.CreateUserRequest;
import ru.matveysuvorov.teamup.users.entities.FriendshipEntity;
import ru.matveysuvorov.teamup.users.entities.UserEntity;

import java.util.List;

@RestController()
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(
            UserService userService
    ) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserEntity me(Authentication auth) {
        return userService.getUser(auth.getName());
    }

    @GetMapping("/")
    public List<UserEntity> getUsers(
            @RequestParam Integer page,
            @RequestParam Integer limit
    ) {
        return userService.getUsers(page, limit);
    }

    @PostMapping("/")
    public UserEntity create(
            @RequestBody CreateUserRequest user,
            Authentication auth
    ) {
        return userService.create(auth.getName(), user);
    }

    @PutMapping("/")
    public UserEntity edit(
            @RequestBody CreateUserRequest user,
            Authentication auth
    ) {
        return userService.edit(auth.getName(), user);
    }

    @GetMapping("/friends/{uid}")
    public List<FriendshipEntity> getFriends(
            @PathVariable String uid
    ) {
        return userService.getFriends(uid);
    }

    @PostMapping("/friends")
    public void addFriend(
            @RequestBody FriendshipRequest request
    ) {
        userService.addFriend(request.fromUser(), request.toUser());
    }

    @PutMapping("/friends")
    public void allowFriendRequest(
            @RequestBody FriendshipRequest request
    ) {
        userService.allowFriendRequest(request.fromUser(), request.toUser());
    }

    @DeleteMapping("/friends")
    public void deleteFriendship(
            @RequestBody FriendshipRequest request
    ) {
        userService.deleteFriendship(request.fromUser(), request.toUser());
    }

    @GetMapping("/status/{uid}")
    public Boolean getOnlineStatus(
            @PathVariable String uid
    ) {
        return userService.getOnlineStatus(uid);
    }

    @MessageMapping("/user/status/{status}")
    public void setOnlineStatus(
            @DestinationVariable Boolean status,
            Authentication auth
    ) {
        userService.setOnlineStatus(auth.getName(), status);
    }

    @PostMapping("/avatars")
    public void uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Authentication auth
    ) {
        userService.uploadAvatar(auth.getName(), file);
    }

    @GetMapping("/{uid}/avatar")
    public String getAvatar(
            @PathVariable String uid,
            Authentication auth
    ) {
        return userService.getAvatarUrl(uid);
    }

    @DeleteMapping("/avatars")
    public void deleteAvatar(
            Authentication auth
    ) {
        userService.deleteAvatar(auth.getName());
    }

}