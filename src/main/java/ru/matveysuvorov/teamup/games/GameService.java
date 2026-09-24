package ru.matveysuvorov.teamup.games;

import org.springframework.stereotype.Service;
import ru.matveysuvorov.teamup.games.exceptions.GameNotFoundException;
import ru.matveysuvorov.teamup.storage.FileEntity;
import ru.matveysuvorov.teamup.storage.FileRepository;
import ru.matveysuvorov.teamup.storage.exceptions.FileNotFoundException;
import ru.matveysuvorov.teamup.storage.services.StorageService;

import java.io.File;
import java.util.List;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final StorageService storageService;
    private final FileRepository fileRepository;

    public GameService (
            GameRepository gameRepository, StorageService storageService, FileRepository fileRepository
    ) {
        this.gameRepository = gameRepository;
        this.storageService = storageService;
        this.fileRepository = fileRepository;
    }

    public List<GameEntity> getGames() {
        return gameRepository.findAll();
    }

    public String getGameCoverUrl(Long gameId) throws NullPointerException {
        GameEntity game = gameRepository.findById(gameId).orElseThrow(() -> new GameNotFoundException("Game not found"));
        FileEntity file = fileRepository.findById(game.getGameCoverId()).orElseThrow(() -> new FileNotFoundException("Game cover not found"));
        return storageService.getPresignedUrl(file.getStorageKey());
    }

}
