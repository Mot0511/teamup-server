package ru.matveysuvorov.teamup.notifications;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.matveysuvorov.teamup.notifications.services.FcmService;

@RestController
@RequestMapping("/fcm")
public class FcmController {

    private final FcmService fcmService;

    public FcmController(FcmService fcmService) {
        this.fcmService = fcmService;
    }

    @PostMapping("/token")
    public void registerToken(
            Authentication auth,
            @RequestParam("fcm_token") String fcmToken
    ) {
        fcmService.registerToken(auth.getName(), fcmToken);
    }

    @DeleteMapping("/token")
    public void removeToken(
            @RequestParam("fcm_token") String fcmToken
    ) {
        fcmService.removeToken(fcmToken);
    }
}
