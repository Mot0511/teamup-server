package ru.matveysuvorov.teamup.stats;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import retrofit2.http.Path;
import ru.matveysuvorov.teamup.stats.dto.Stats;

@RestController
@RequestMapping("/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/{period}")
    public Stats getStats(
            Authentication auth,
            @PathVariable Long period
    ) {
        return statsService.getStats(auth.getName(), period);
    }

    @GetMapping("/")
    public Stats getStats(
            Authentication auth
    ) {
        return statsService.getStats(auth.getName());
    }

}
