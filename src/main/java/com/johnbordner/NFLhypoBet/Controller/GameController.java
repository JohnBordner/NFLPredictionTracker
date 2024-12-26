package com.johnbordner.NFLhypoBet.Controller;

import com.johnbordner.NFLhypoBet.model.Game;
// import com.johnbordner.NFLhypoBet.service.GameService;
import com.johnbordner.NFLhypoBet.service.GameService;
import com.johnbordner.NFLhypoBet.service.RapidApiNflService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class GameController {


    @Autowired
    private GameService gameService;



    @Autowired
    private RapidApiNflService rapidApiNflService;



    @GetMapping("/nfl")
    public String getNflGames(@RequestParam("gameWeek")String gameWeek, @RequestParam("seasonType") String seasonType,
                              @RequestParam("season") String season, Model model) {

        List <Game> games= gameService.getGameByWeekTypeSeason(gameWeek, seasonType, season);



        if (games == null || games.isEmpty()) {



            System.out.println("GAMES NOT FOUND");
            games = rapidApiNflService.getNflGames(gameWeek, seasonType, season);

            for (Game game : games) {
                game.setFormattedGameDate(game.getGameDate());
            }

            if (!games.isEmpty()) {
                saveGamesToDatabase(games);

            }
        }




        Set<String> datesCalled = new HashSet<>();  // Store dates for which getScores has been called

        for (Game game : games) {
            // Check if the game score is 0-0 or the status is not final/completed
            if ((game.getAwayScore() == 0 && game.getHomeScore() == 0) ||
                    (!"Final".equals(game.getGameStatus()) &&
                            !"Final/OT".equals(game.getGameStatus()) &&
                            !"Completed".equals(game.getGameStatus()))) {

                // Only call getScores if this game date has not been called yet
                if (!datesCalled.contains(game.getGameDate())) {
                    datesCalled.add(game.getGameDate());  // Mark date as called

                    // Retrieve scores for the game date

                    System.out.println("Calling getNflGames with params: week=" + gameWeek +
                            ", seasonType=" + seasonType + ", season=" + season);


                    List<Game> updatedGamesWithScores = rapidApiNflService.getScores(game.getGameDate());


                    // Update scores in database
                    updateGameScores(games, updatedGamesWithScores);
                }
            }
        }


        model.addAttribute("games", games);
        model.addAttribute("gameWeek", gameWeek);
        model.addAttribute("seasonType", seasonType);
        model.addAttribute("season", season);

        return "nfl";
    }


    private void updateGameScores(List<Game> games, List<Game> updatedGamesWithScores) {
        for (Game updatedGame : updatedGamesWithScores) {
            for (Game game : games) {
                if (game.getGameID().equals(updatedGame.getGameID())) {
                    game.setAwayScore(updatedGame.getAwayScore());
                    game.setHomeScore(updatedGame.getHomeScore());
                    game.setGameStatus(updatedGame.getGameStatus());
                    gameService.saveGame(game);
                }
            }
        }
    }



    private void saveGamesToDatabase(List<Game> games) {
        for (Game game : games) {
            // Check if the game already exists
            if (!gameService.existsByGameID(game.getGameID())) {
                gameService.saveGame(game);
            }
        }
    }




}