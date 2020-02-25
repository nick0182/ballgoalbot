package com.nikolay.bot.ballgoal.json.fixture;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Api {

    private List<Fixture> fixtures;

    public List<Fixture> getFixtures() {
        return fixtures;
    }
}
