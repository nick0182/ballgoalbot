package com.nikolay.bot.ballgoal.json.fixture;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Team {

    private int team_id;

    private String team_name;
}
