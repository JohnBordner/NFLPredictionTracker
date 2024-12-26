package com.johnbordner.NFLhypoBet.repository;
import com.johnbordner.NFLhypoBet.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface GameRepository extends JpaRepository<Game, Long> {

    Game findBygameID(String gameID);

    boolean existsByGameID(String gameID);

    List<Game> findByGameWeekAndSeasonTypeAndSeason(String gameWeek, String seasonType, String season);

    List<Game> findByGameDate(String gameDate);
}