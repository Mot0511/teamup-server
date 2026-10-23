package ru.matveysuvorov.teamup.users;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.matveysuvorov.teamup.auth.entities.AccountEntity;
import ru.matveysuvorov.teamup.auth.repositories.AccountRepository;
import ru.matveysuvorov.teamup.games.GameEntity;
import ru.matveysuvorov.teamup.games.GameRepository;
import ru.matveysuvorov.teamup.games.exceptions.GameNotFoundException;
import ru.matveysuvorov.teamup.notifications.services.FcmService;
import ru.matveysuvorov.teamup.storage.FileEntity;
import ru.matveysuvorov.teamup.storage.FileRepository;
import ru.matveysuvorov.teamup.storage.exceptions.FileNotFoundException;
import ru.matveysuvorov.teamup.storage.services.StorageService;
import ru.matveysuvorov.teamup.users.dto.CreateUserRequest;
import ru.matveysuvorov.teamup.users.entities.FriendshipEntity;
import ru.matveysuvorov.teamup.users.entities.UserEntity;
import ru.matveysuvorov.teamup.users.exceptions.AvatarNotExists;
import ru.matveysuvorov.teamup.users.exceptions.UserNotFoundException;
import ru.matveysuvorov.teamup.users.repositories.FriendshipRepository;
import ru.matveysuvorov.teamup.users.repositories.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final FriendshipRepository friendshipRepository;
    private final GameRepository gameRepository;
    private final FileRepository fileRepository;
    private final StorageService storageService;

    private final FcmService fcmService;

    public UserService(
            UserRepository userRepository,
            AccountRepository accountRepository,
            FriendshipRepository friendshipRepository, GameRepository gameRepository, FileRepository fileRepository, StorageService storageService, FcmService fcmService
    ) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.friendshipRepository = friendshipRepository;
        this.gameRepository = gameRepository;
        this.fileRepository = fileRepository;
        this.storageService = storageService;
        this.fcmService = fcmService;
    }

    public UserEntity getUser(String uid) {
        UserEntity user = userRepository.findByUid(uid).orElseThrow(() -> new UserNotFoundException("User not found"));
        System.out.println(user);
        return user;
    };

    public UserEntity create(String uid, CreateUserRequest userdata) {
        AccountEntity account = accountRepository.findById(UUID.fromString(uid)).orElseThrow(() -> new BadCredentialsException("Account not found"));
        if (userRepository.existsByUid(uid)) {
            throw new BadCredentialsException("User already exists");
        }

        if (userRepository.existsByUsername(userdata.username())) {
            throw new BadCredentialsException("Username already exists");
        }
        UserEntity user = new UserEntity();
        user.setUid(account.getId().toString());
        user.setEmail(account.getEmail());
        user.setUsername(userdata.username());
        user.setDescription(userdata.description());
        user.setAge(userdata.age());
        user.setGender(userdata.gender());

        if (userdata.game() != null) {
            GameEntity game = gameRepository.findById(userdata.game()).orElseThrow(() -> new GameNotFoundException("Game not found"));
            user.setFavouriteGame(game);
        }
        System.out.println("5");

        user.setOnline(false);

        userRepository.save(user);
        return user;
    }

    public UserEntity edit(String uid, CreateUserRequest userdata) {
        System.out.println(uid);
        UserEntity user = userRepository.findByUid(uid).orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setUsername(userdata.username());
        user.setDescription(userdata.description());
        user.setGender(userdata.gender());
        user.setAge(userdata.age());
        GameEntity game = gameRepository.findById(userdata.game()).orElseThrow(() -> new GameNotFoundException("Game not found"));
        user.setFavouriteGame(game);

        userRepository.save(user);
        return user;
    }

    public List<FriendshipEntity> getFriends(String uid) {
        if (!accountRepository.existsById(UUID.fromString(uid))) {
            throw new UserNotFoundException("Account not found");
        }

        List<FriendshipEntity> friendships = friendshipRepository.findByFromUserOrToUser(uid, uid).orElseThrow(() -> new BadCredentialsException("Friendships not found"));
        return friendships;
    }

    public void addFriend(String fromUserId, String toUserId) {
        UserEntity fromUser = userRepository.findByUid(fromUserId)
                .orElseThrow(() -> new UserNotFoundException("User"+fromUserId+" not found"));

        UserEntity toUser = userRepository.findByUid(toUserId)
                .orElseThrow(() -> new UserNotFoundException("User"+toUserId+" not found"));

        FriendshipEntity friendship = new FriendshipEntity();
        friendship.setFromUser(fromUserId);
        friendship.setToUser(toUserId);
        friendship.setStatus(false);
        friendshipRepository.save(friendship);

        fcmService.sendFriendRequestNotification(toUser, fromUser);
    }

    public void allowFriendRequest(String fromUser, String toUser) {
        if (!userRepository.existsByUid(fromUser) || !userRepository.existsByUid(toUser)) {
            throw new UserNotFoundException("Users not found");
        }

        FriendshipEntity friendship = friendshipRepository
                .findByFromUserAndToUser(fromUser, toUser)
                .orElseThrow(() -> new BadCredentialsException("Friendship not found"));

        friendship.setStatus(true);
        friendshipRepository.save(friendship);
    }

    @Transactional
    public void deleteFriendship(String fromUser, String toUser) {
        if (!userRepository.existsByUid(fromUser) || !userRepository.existsByUid(toUser)) {
            throw new UserNotFoundException("Users not found");
        }

        friendshipRepository.deleteByFromUserAndToUser(fromUser, toUser);
    }

    public Boolean getOnlineStatus(String uid) {
        UserEntity user = userRepository.findByUid(uid).orElseThrow(() -> new UserNotFoundException("User not found"));

        return user.isOnline();
    }

    public void setOnlineStatus(String uid, Boolean status) {
        UserEntity user = userRepository.findByUid(uid).orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setOnline(status);
        userRepository.save(user);
    }

    public List<UserEntity> getUsers(Integer page, Integer limit) {
        return userRepository.findAll(PageRequest.of(page, limit)).getContent();
    }

    public void uploadAvatar(String uid, MultipartFile file) {
        UserEntity user = userRepository.findByUid(uid).orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getAvatar() == null) {
            UUID id = UUID.randomUUID();
            String key = "avatars/"+id;

            storageService.upload(file, key);

            try {
                FileEntity fileEntity = new FileEntity();
                fileEntity.setId(id);
                fileEntity.setStorageKey(key);
                fileEntity.setOriginalFilename(file.getOriginalFilename());
                fileEntity.setContentType(file.getContentType());
                fileEntity.setUploaderId(uid);
                fileEntity.setUploadedAt(Instant.now());
                fileEntity.setSize(file.getSize());

                fileRepository.save(fileEntity);
                user.setAvatar(id);
                userRepository.save(user);

            } catch (Exception e) {
                storageService.delete(key);
                throw e;
            }

            return;
        }

        FileEntity fileEntity = fileRepository.findById(user.getAvatar()).orElseThrow(() -> new FileNotFoundException("Avatar not found"));
        String key = fileEntity.getStorageKey();
        storageService.upload(file, key);
    }

    public String getAvatarUrl(String uid) {
        UserEntity user = userRepository.findByUid(uid).orElseThrow(() -> new UserNotFoundException("User not found"));
        FileEntity fileEntity = fileRepository.findById(user.getAvatar()).orElseThrow(() -> new AvatarNotExists("User doesn't have avatar"));
        return storageService.getPresignedUrl(fileEntity.getStorageKey());
    }

    public void deleteAvatar(String uid) {
        FileEntity fileEntity = fileRepository.findByUploaderId(uid).orElseThrow(() -> new AvatarNotExists("User doesn't have avatar"));
        fileRepository.delete(fileEntity);
        storageService.delete(fileEntity.getStorageKey());
    }

    public boolean isUsernameExists(String username) {
        Optional<UserEntity> user = userRepository.findByUsername(username);
        return user.isPresent();
    }
}
