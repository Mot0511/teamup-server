package ru.matveysuvorov.teamup.teams.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.matveysuvorov.teamup.teams.entities.InviteEntity;

import java.util.Optional;

public interface InviteRepository extends JpaRepository<InviteEntity, Integer> {

    Optional<InviteEntity> findByTeamId(Long teamId);
    Optional<InviteEntity> findByTeamIdAndNumber(Long teamId, Integer number);

}
