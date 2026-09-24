package ru.matveysuvorov.teamup.voice;

import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import io.livekit.server.VideoGrant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class VoiceService {

    @Value("${livekit.api-key}")
    private String apiKey;

    @Value("${livekit.api-secret}")
    private String apiSecret;

    public String getLivekitToken(String uid, Long teamId) {
        AccessToken token = new AccessToken(apiKey, apiSecret);

        token.setIdentity(uid);

        token.addGrants(
                new RoomJoin(true),
                new RoomName(teamId.toString())
        );

        return token.toJwt();
    }

}
    