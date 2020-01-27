package json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.ZonedDateTime;

public class Fixture {

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private ZonedDateTime event_date;

    private String status;

    private Team homeTeam;

    private Team awayTeam;

    public Team getHomeTeam() {
        return homeTeam;
    }

    public Team getAwayTeam() {
        return awayTeam;
    }

    public ZonedDateTime getEventDate() {
        return event_date;
    }

    public String getStatus() {
        return status;
    }
}
