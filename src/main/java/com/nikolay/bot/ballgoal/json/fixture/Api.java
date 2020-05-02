package com.nikolay.bot.ballgoal.json.fixture;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class Api {

    private List<Fixture> fixtures;
}
