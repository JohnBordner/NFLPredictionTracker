package com.johnbordner.NFLhypoBet.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.johnbordner.NFLhypoBet.model.Game;
import com.johnbordner.NFLhypoBet.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RapidApiNflService {
    @Autowired
    private WebClient webClient;


    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GameRepository gameRepository;


    private static final String URL = "rapidapi.com";


    public List<Game> getNflGames(String week, String seasonType, String season) {

        System.out.println("getNFLGames Method has been called!");



        String jsonResponse = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getNFLGamesForWeek")
                        .queryParam("week", week)
                        .queryParam("seasonType", seasonType)
                        .queryParam("season", season)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();  // Blocking call to get the response

        // Process the JSON response and convert it into a List<Game>
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);  // Parse the JSON string
            JsonNode gamesNode = rootNode.get("body");  // Extract the 'body' array


            System.out.println("Games Node: " + gamesNode);
            // convert the body node into a list of Game objects
            List<Game> schedule = objectMapper.convertValue(gamesNode, new TypeReference<List<Game>>() {});

            // check if each game has scores, if not, prepare to fetch them
            Set<String> datesToFetchScores = new HashSet<>();
            for (Game game : schedule) {
                if ((game.getAwayScore() == 0 && game.getHomeScore() == 0) ||
                        (!"Final".equals(game.getGameStatus()) &&
                                !"Final/OT".equals(game.getGameStatus()) &&
                                !"Completed".equals(game.getGameStatus()))) {  // If no scores present
                    datesToFetchScores.add(game.getGameDate());  // Track date for score fetching
                }
            }

            // fetch scores for each missing date and update the schedule
            for (String date : datesToFetchScores) {
                List<Game> gamesWithScores = getScores(date);  // Retrieve scores for this date

                // update schedule games with fetched scores
                for (Game gameWithScore : gamesWithScores) {
                    // locate the corresponding game in the schedule list
                    for (Game scheduledGame : schedule) {
                        if (scheduledGame.getGameID().equals(gameWithScore.getGameID())) {
                            // update the scheduled game with the scores and status
                            scheduledGame.setAwayScore(gameWithScore.getAwayScore());
                            scheduledGame.setHomeScore(gameWithScore.getHomeScore());
                            scheduledGame.setGameStatus(gameWithScore.getGameStatus());
                        }
                    }
                }
            }

            return schedule;  // return the updated schedule with scores

        } catch (Exception e) {
            e.printStackTrace();
            return null;  // return null if theres errors
        }
    }



    public List<Game> getScores(String gameDate) {
        String jsonResponse = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getNFLScoresOnly")
                        .queryParam("gameDate", gameDate)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();  // Blocking call to get the response
        System.out.println("getScores method has been called!");





        try {
            // Parse the JSON response
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode bodyNode = rootNode.get("body");  // Extract the body node

            // Retrieve existing games from the database for the specified date
            List<Game> existingGames = gameRepository.findByGameDate(gameDate);

            // map existing games by gameID for quick access
            Map<String, Game> gameMap = existingGames.stream()
                    .collect(Collectors.toMap(Game::getGameID, game -> game));

            // iterate over each gameID node within body
            if (bodyNode != null) {
                bodyNode.fields().forEachRemaining(entry -> {
                    String gameID = entry.getKey();  // Get the gameID from the API response
                    JsonNode gameNode = entry.getValue();

                    // extract awayPts, homePts, and gameStatus
                    int awayPts = gameNode.get("awayPts").asInt();
                    int homePts = gameNode.get("homePts").asInt();
                    String gameStatus = gameNode.get("gameStatus").asText();

                    // find the matching game in the database
                    Game existingGame = gameMap.get(gameID);

                    if (existingGame != null) {
                        // update the scores of the existing game
                        existingGame.setAwayScore(awayPts);
                        existingGame.setHomeScore(homePts);

                        // update game status if completed
                        if ("Completed".equalsIgnoreCase(gameStatus)) {
                            existingGame.setGameStatus("Final");
                        }
                    }
                });

                // save all updated games back to the database
                gameRepository.saveAll(existingGames);
            }

            return existingGames;  // return the list of games with updated scores

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();  // return an empty list in case of errors
        }
    }


    public void saveGames(List<Game> games) {
        for (Game game : games) {
            // check if the game already exists in the database
            if (!gameRepository.existsByGameID(game.getGameID())) {
                gameRepository.save(game); // save only if it does not exist
            }
        }
    }






}
