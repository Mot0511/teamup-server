package ru.matveysuvorov.teamup.games;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameRepository extends JpaRepository<GameEntity, Integer> {

    Optional<GameEntity> findById(Long id);

}
