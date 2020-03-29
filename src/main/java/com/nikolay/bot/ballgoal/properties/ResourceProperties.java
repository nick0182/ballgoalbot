package com.nikolay.bot.ballgoal.properties;

public class ResourceProperties {

    private String apiResourceNextFixture;

    private String resourceHtmlToImage;

    private String apiResourceNextLeagueFixture;

    private String apiResourceLeagueRoundDates;

    private String apiResourceFixture;

    private String apiResourceFixturesInPlay;

    private String apiResourceLeagueFixturesInPlay;

    private int teamId;

    public String getApiResourceNextFixture() {
        return apiResourceNextFixture;
    }

    public String getResourceHtmlToImage() {
        return resourceHtmlToImage;
    }

    public String getApiResourceNextLeagueFixture() {
        return apiResourceNextLeagueFixture;
    }

    public String getApiResourceLeagueRoundDates() {
        return apiResourceLeagueRoundDates;
    }

    public String getApiResourceFixture() {
        return apiResourceFixture;
    }

    public String getApiResourceFixturesInPlay() {
        return apiResourceFixturesInPlay;
    }

    public String getApiResourceLeagueFixturesInPlay() {
        return apiResourceLeagueFixturesInPlay;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setApiResourceNextFixture(String apiResourceNextFixture) {
        this.apiResourceNextFixture = apiResourceNextFixture;
    }

    public void setResourceHtmlToImage(String resourceHtmlToImage) {
        this.resourceHtmlToImage = resourceHtmlToImage;
    }

    public void setApiResourceNextLeagueFixture(String apiResourceNextLeagueFixture) {
        this.apiResourceNextLeagueFixture = apiResourceNextLeagueFixture;
    }

    public void setApiResourceLeagueRoundDates(String apiResourceLeagueRoundDates) {
        this.apiResourceLeagueRoundDates = apiResourceLeagueRoundDates;
    }

    public void setApiResourceFixture(String apiResourceFixture) {
        this.apiResourceFixture = apiResourceFixture;
    }

    public void setApiResourceFixturesInPlay(String apiResourceFixturesInPlay) {
        this.apiResourceFixturesInPlay = apiResourceFixturesInPlay;
    }

    public void setApiResourceLeagueFixturesInPlay(String apiResourceLeagueFixturesInPlay) {
        this.apiResourceLeagueFixturesInPlay = apiResourceLeagueFixturesInPlay;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }
}
