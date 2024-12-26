
package com.johnbordner.NFLhypoBet.Controller;

import com.johnbordner.NFLhypoBet.service.PredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.johnbordner.NFLhypoBet.model.*;
import com.johnbordner.NFLhypoBet.service.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
public class PredictionController {
    @Autowired
    private PredictionService predictionService;

    @Autowired
    private RapidApiNflService rapidApiNflService;

    @Autowired
    private UserService userService;

    @Autowired
    private GameService gameService;


    @PostMapping("/index")
    public String submitPredictions(@RequestParam Map<String, String> allParams,
                                    @RequestParam("gameWeek") String gameWeek,
                                    @RequestParam("seasonType") String seasonType,
                                    @RequestParam("season") String season,
                                    Principal principal) {
        User user = userService.findUserByUsername(principal.getName());

        // Loop through the game predictions submitted
        for (String key : allParams.keySet()) {
            if (key.startsWith("predictedWinner")) { // Check if it's a prediction key
                String predictedWinner = allParams.get(key);
                String gameID = key.replace("predictedWinner", ""); // Extract gameId from the key

                Game game = gameService.getGameById(gameID);

                Prediction existingPrediction = predictionService.getPredictionByUserAndGame(user, game);

                if (existingPrediction != null) {
                    // Update existing prediction
                    existingPrediction.setPredictedWinner(predictedWinner);
                    predictionService.savePrediction(existingPrediction);
                } else {
                    // Create a new prediction
                    Prediction prediction = new Prediction();
                    prediction.setUser(user);
                    prediction.setGame(game);
                    prediction.setPredictedWinner(predictedWinner);
                    predictionService.savePrediction(prediction);
                }
            }
        }

        return "redirect:/nfl?gameWeek=" + gameWeek + "&seasonType=" + seasonType + "&season=" + season;
    }



    //unused, similar method in user controller for now, may move it over
    @GetMapping("/user/account")
    public String showPredictions(Model model, Principal principal) {
        User user = userService.findUserByUsername(principal.getName());
        List<Prediction> predictions = predictionService.getPredictionsForUser(user);

        for (Prediction prediction : predictions) {
            Game game = prediction.getGame();

            if ("Final".equalsIgnoreCase(game.getGameStatus()) ||
                    "Final/OT".equalsIgnoreCase(game.getGameStatus()) ||
                    "Completed".equalsIgnoreCase(game.getGameStatus())) {

                predictionService.evaluatePrediction(prediction, game);
            }

        }
        model.addAttribute("predictions", predictions);
        return "predictions";

    }



}

