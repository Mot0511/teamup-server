package ru.matveysuvorov.teamup.realtime;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import ru.matveysuvorov.teamup.realtime.interceptors.ChatMemberInterceptor;
import ru.matveysuvorov.teamup.realtime.interceptors.MessageInterceptor;
import ru.matveysuvorov.teamup.realtime.interceptors.RealtimeAuthInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class RealtimeConfig implements WebSocketMessageBrokerConfigurer {

    private final RealtimeAuthInterceptor realtimeAuthInterceptor;
    private final ChatMemberInterceptor chatMemberInterceptor;
    private final MessageInterceptor memberInterceptor;

    public RealtimeConfig(
            RealtimeAuthInterceptor realtimeAuthInterceptor, ChatMemberInterceptor chatMemberInterceptor, MessageInterceptor memberInterceptor
    ) {
        this.realtimeAuthInterceptor = realtimeAuthInterceptor;
        this.chatMemberInterceptor = chatMemberInterceptor;
        this.memberInterceptor = memberInterceptor;
    }

    @Override
    public void configureClientInboundChannel(
            ChannelRegistration registration
    ) {
        registration.interceptors(realtimeAuthInterceptor);
        registration.interceptors(chatMemberInterceptor);
        registration.interceptors(memberInterceptor);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/realtime");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

}
