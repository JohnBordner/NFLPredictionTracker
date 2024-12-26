package com.johnbordner.NFLhypoBet.service;

import com.johnbordner.NFLhypoBet.model.Game;
import com.johnbordner.NFLhypoBet.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class GameService {
    @Autowired
    private GameRepository gameRepository;

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    public Game saveGame(Game game) {
        System.out.println("Saving GameID: " + game.getGameID() +
                ", AwayScore: " + game.getAwayScore() +
                ", HomeScore: " + game.getHomeScore() +
                ", Status: " + game.getGameStatus());



        return gameRepository.save(game);
    }

    public Game getGameById(String gameID) {
        return gameRepository.findBygameID(gameID);
    }

    public boolean existsByGameID(String gameID) {
        return gameRepository.existsByGameID(gameID);
    }

    public List<Game> getGameByWeekTypeSeason(String gameWeek, String seasonType, String season) {

        System.out.println("getGameByWeekTypeSeason method called!");

        System.out.println("Querying database with parameters:");
        System.out.println("gameWeek: " + gameWeek);
        System.out.println("seasonType: " + seasonType);
        System.out.println("season: " + season);

        String formattedWeek = "Week " + gameWeek;
        String formattedType = seasonType + " Season";

        return gameRepository.findByGameWeekAndSeasonTypeAndSeason(formattedWeek,formattedType, season);

    }



    public String normalizeWeek(String week) {
        return week.replace("Week ", "").trim();
    }

    public String normalizeSeasonType(String seasonType) {
        return seasonType.replace("Season", "").trim();
    }

}

