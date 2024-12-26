package com.johnbordner.NFLhypoBet.Controller;
import com.johnbordner.NFLhypoBet.model.Game;
import com.johnbordner.NFLhypoBet.model.Prediction;
import com.johnbordner.NFLhypoBet.model.User;
import com.johnbordner.NFLhypoBet.service.GameService;
import com.johnbordner.NFLhypoBet.service.PredictionService;
import com.johnbordner.NFLhypoBet.service.RapidApiNflService;
import com.johnbordner.NFLhypoBet.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class UserController {

    @Autowired
    private PredictionService predictionService;

    @Autowired
    private UserService userService;

    @Autowired
    private RapidApiNflService rapidApiNflService;

    @Autowired
    private GameService gameService;

    @GetMapping("/account")
    public String accountPage(Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findUserByUsername(username);

        List<Prediction> userPredictions = predictionService.getPredictionsForUser(user);

        Set<String> weeksToUpdate = new HashSet<>();
        Map<String, String[]> seasonInfoMap = new HashMap<>();
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        for (Prediction prediction : userPredictions) {
            Game game = prediction.getGame();


            if (!"Final".equalsIgnoreCase(game.getGameStatus()) &&
                    !"Final/OT".equalsIgnoreCase(game.getGameStatus()) &&
                    !"Completed".equalsIgnoreCase(game.getGameStatus())) {

                LocalDate gameDate = LocalDate.parse(game.getGameDate(), formatter);
                if (gameDate.isBefore(currentDate)){

                    String normalizedWeek = gameService.normalizeWeek(game.getGameWeek());
                    String normalizedSeasonType = gameService.normalizeSeasonType(game.getSeasonType());
                    weeksToUpdate.add(normalizedWeek);
                    seasonInfoMap.put(normalizedWeek, new String[]{normalizedSeasonType, game.getSeason()});
                }
            }
        }

        for (String gameWeek : weeksToUpdate) {
            String[] seasonInfo = seasonInfoMap.get(gameWeek);
            String seasonType = seasonInfo[0];
            String season = seasonInfo[1];


            System.out.println("Calling getNflGames with params: week=" + gameWeek +
                    ", seasonType=" + seasonType + ", season=" + season);


            List<Game> updatedGames = rapidApiNflService.getNflGames(gameWeek, seasonType, season);

            for (Game updatedGame : updatedGames) {
                updatedGame.setFormattedGameDate(updatedGame.getGameDate());
                gameService.saveGame(updatedGame);
            }
        }

        int W_count = 0;
        int L_count = 0;

        for (Prediction prediction : userPredictions) {
            Game game = prediction.getGame();

            if ("Final".equalsIgnoreCase(game.getGameStatus()) ||
                    "Final/OT".equalsIgnoreCase(game.getGameStatus()) ||
                    "Completed".equalsIgnoreCase(game.getGameStatus())) {
                predictionService.evaluatePrediction(prediction, game);

                if (prediction.isCorrect()) {
                    W_count++;
                } else {
                    L_count++;
                }
            }
        }

        user.setTotalWins(W_count);
        user.setTotalLosses(L_count);

        userService.saveUser(user);


        double w_l_ratio = (L_count == 0) ? W_count : (double) W_count / L_count;

        model.addAttribute("predictions", userPredictions);
        model.addAttribute("wins", W_count);
        model.addAttribute("losses", L_count);
        model.addAttribute("winLossRatio", w_l_ratio);

        return "account";
    }

    @GetMapping("/users")
    public String listUsers(@RequestParam(defaultValue = "0") int page, Model model) {
        int pageSize = 10; // Number of users per page
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "totalWins"));
        Page<User> usersPage = userService.getUsersPage(pageable);

        model.addAttribute("users", usersPage.getContent());
        model.addAttribute("totalPages", usersPage.getTotalPages());
        model.addAttribute("currentPage", page);

        return "userList";
    }




}




