package cache;

public class CachedMessage {

    private String eventDate;

    private String eventTime;

    private String homeTeam;

    private String awayTeam;

    public CachedMessage(String homeTeam, String awayTeam, String eventDate, String eventTime) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
    }

    public String getEventDate() {
        return eventDate;
    }

    public String getEventTime() {
        return eventTime;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }
}
