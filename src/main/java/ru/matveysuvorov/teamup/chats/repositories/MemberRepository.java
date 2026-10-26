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

    List<MemberEntity> findByMemberUid(String uid);

    List<MemberEntity> findByChatId(Long chatId);

    Optional<MemberEntity> findByChatAndMemberUid(ChatEntity chat, String memberId);

    @Modifying
    @Query("""
DELETE FROM MemberEntity m
WHERE m.chat.id = :chatId
""")
    void deleteByChatId(@Param("chatId") Long chatId);

    @Modifying
    @Query("""
DELETE FROM MemberEntity m
WHERE m.chat.id = :chatId
AND m.member.uid = :memberId
""")
    void deleteByChatIdAndMemberId(@Param("chatId") Long chatId, @Param("memberId") String memberId);

    @Query("""
SELECT m FROM MemberEntity m
WHERE m.chat.isTeam = true
AND m.member.uid = :uid
""")
    List<MemberEntity> findTeamsByUid(@Param("uid") String uid);
    long countByChatId(Long chatId);
}