package com.johnbordner.NFLhypoBet.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Prediction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private User user;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name = "game_gameid", referencedColumnName = "gameid")
    private Game game;

    private String predictedWinner;
    private String actualWinner;
    private boolean correct;






}
