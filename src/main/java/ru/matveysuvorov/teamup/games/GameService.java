package ru.matveysuvorov.teamup.games;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameService {

    private final GameRepository gameRepository;

    public GameService (
            GameRepository gameRepository
    ) {
        this.gameRepository = gameRepository;
    }

    public List<GameEntity> getGames() {
        return gameRepository.findAll();
    }


}
