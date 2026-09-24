package ru.matveysuvorov.teamup.games;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/")
    public List<GameEntity> getGames() {
        return gameService.getGames();
    }

    @GetMapping("/{gameId}/url")
    public String getGameCoverUrl(
            @PathVariable Long gameId
    ) {
        return gameService.getGameCoverUrl(gameId);
    }
}
