package ru.matveysuvorov.teamup.voice;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/voice")
public class VoiceController {

    private final VoiceService voiceSerivce;

    public VoiceController(VoiceService voiceSerivce) {
        this.voiceSerivce = voiceSerivce;
    }

    @GetMapping("/{teamId}/livekit-token")
    public String getToken(
            Authentication auth,
            @PathVariable Long teamId
    ) {
        return voiceSerivce.getLivekitToken(auth.getName(), teamId);
    }

}