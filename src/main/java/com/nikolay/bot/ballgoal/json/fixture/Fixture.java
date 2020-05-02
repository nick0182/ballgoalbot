package com.nikolay.bot.ballgoal.json.fixture;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.nikolay.bot.ballgoal.json.deserializer.LocalDateTimeDeserializer;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Fixture {

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime event_date;

    private int fixture_id;

    private int goalsHomeTeam;

    private int goalsAwayTeam;

    private String status;

    private Team homeTeam;

    private Team awayTeam;

    private int elapsed;
}
