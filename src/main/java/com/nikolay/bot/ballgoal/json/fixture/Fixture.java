package com.nikolay.bot.ballgoal.json.fixture;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalDateTime;

public class Fixture {

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime event_date;

    private int fixture_id;

    private int goalsHomeTeam;

    private int goalsAwayTeam;

    private String status;

    private Team homeTeam;

    private Team awayTeam;

    private String round;

    private int elapsed;

    public int getGoalsHomeTeam() {
        return goalsHomeTeam;
    }

    public int getGoalsAwayTeam() {
        return goalsAwayTeam;
    }

    public Team getHomeTeam() {
        return homeTeam;
    }

    public Team getAwayTeam() {
        return awayTeam;
    }

    public LocalDateTime getEventDate() {
        return event_date;
    }

    public String getStatus() {
        return status;
    }

    public String getRound() {
        return round;
    }

    public int getElapsed() {
        return elapsed;
    }

}
