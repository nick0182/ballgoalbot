package com.nikolay.bot.ballgoal.json.fixture;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Team {

    private int team_id;

    private String team_name;

    public int getTeam_id() {
        return team_id;
    }

    public String getTeam_name() {
        return team_name;
    }
}
