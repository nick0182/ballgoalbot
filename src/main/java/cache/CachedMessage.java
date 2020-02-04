package cache;

import java.time.ZonedDateTime;

public class CachedMessage {

    private String homeTeam;

    private String awayTeam;

    private ZonedDateTime eventDateTime;

    private String status;

    public CachedMessage(String homeTeam, String awayTeam, ZonedDateTime eventDateTime, String status) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.eventDateTime = eventDateTime;
        this.status = status;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public ZonedDateTime getEventDateTime() {
        return eventDateTime;
    }

    public String getStatus() {
        return status;
    }
}
