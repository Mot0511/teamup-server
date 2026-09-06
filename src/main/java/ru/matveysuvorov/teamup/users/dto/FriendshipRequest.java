package ru.matveysuvorov.teamup.users.dto;

public record FriendshipRequest(
        String fromUser,
        String toUser
) {
}
