package com.johnbordner.NFLhypoBet.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor

public class Game {
    @Id
    private String gameID;
    private String seasonType;
    private String home;
    private String away;

    private int homeScore;
    private int awayScore;
    private String gameDate;
    private String gameTime;
    private String gameStatus;
    private String gameWeek;
    private String season;
    private String formattedGameDate;


    //sets the formattedGameDate from the gameDate
    public void setFormattedGameDate(String gameDate) {

            String year = gameDate.substring(0, 4); // First 4 characters for year
            String month = gameDate.substring(4, 6); // Next 2 characters for month
            String day = gameDate.substring(6, 8); // Last 2 characters for day


            this.formattedGameDate = month + "-" + day + "-" + year; // Format to "mmmm-dd-yyyy"

    }
}