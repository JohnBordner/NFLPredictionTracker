package com.johnbordner.NFLhypoBet.service;

import com.johnbordner.NFLhypoBet.model.*;
import com.johnbordner.NFLhypoBet.repository.PredictionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PredictionService {
    @Autowired
    private PredictionRepository predictionRepository;

    public List<Prediction> getPredictionsForUser(User user) {
        return predictionRepository.findByUser(user);
    }

    public Prediction savePrediction(Prediction prediction) {
        return predictionRepository.save(prediction);
    }

    public void evaluatePrediction(Prediction prediction, Game game) {
        // Determine the actual winner of the game
        String actualWinner = game.getHomeScore() > game.getAwayScore() ? game.getHome() : game.getAway();

        prediction.setActualWinner(game.getHomeScore() > game.getAwayScore() ? game.getHome() : game.getAway());


        // Compare the predicted winner with the actual winner
        if (actualWinner != null && !actualWinner.isEmpty()) {
            // Compare the predicted winner with the actual winner
            prediction.setCorrect(prediction.getPredictedWinner().equals(actualWinner));
            predictionRepository.save(prediction);
        }
    }

    public Prediction getPredictionByUserAndGame(User user, Game game) {
        return predictionRepository.findByUserAndGame(user, game);
    }

}
