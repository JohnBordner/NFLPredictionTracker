package com.johnbordner.NFLhypoBet.repository;

import com.johnbordner.NFLhypoBet.model.Game;
import com.johnbordner.NFLhypoBet.model.Prediction;
import com.johnbordner.NFLhypoBet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PredictionRepository extends JpaRepository<Prediction, Long> {
    List<Prediction> findByUser(User user);


    Prediction findByUserAndGame(User user, Game game);


}


