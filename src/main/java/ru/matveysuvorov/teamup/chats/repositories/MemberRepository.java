package ru.matveysuvorov.teamup.chats.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.matveysuvorov.teamup.chats.entities.ChatEntity;
import ru.matveysuvorov.teamup.chats.entities.MemberEntity;

import java.lang.reflect.Member;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, Integer> {

    List<MemberEntity> findByMemberId(String uid);
    Optional<MemberEntity> findByChatAndMemberId(ChatEntity chat, String memberId);

    @Modifying
    @Query("""
DELETE FROM MemberEntity m
WHERE m.chat.id = :chatId
""")
    void deleteByChatId(@Param("chatId") Integer chatId);

    @Modifying
    @Query("""
DELETE FROM MemberEntity m
WHERE m.chat.id = :chatId
AND m.memberId = :memberId
""")
    void deleteByChatIdAndMemberId(@Param("chatId") Integer chatId, @Param("memberId") String memberId);

    @Query("""
SELECT m FROM MemberEntity m
WHERE m.chat.isTeam = true
AND m.memberId = :uid
""")
    List<MemberEntity> findTeamsByUid(@Param("uid") String uid);
}