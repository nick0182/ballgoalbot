package com.nikolay.bot.ballgoal.properties;

public class ResourceProperties {

    private String apiResourceNextFixture;

    private String resourceHtmlToImage;

    private String apiResourceLeagueFixture;

    private String apiResourceLeagueRoundDates;

    private String apiResourceFixture;

    private String apiResourceFixturesInPlay;

    private int teamId;

    public String getApiResourceNextFixture() {
        return apiResourceNextFixture;
    }

    public String getResourceHtmlToImage() {
        return resourceHtmlToImage;
    }

    public String getApiResourceLeagueFixture() {
        return apiResourceLeagueFixture;
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

    public int getTeamId() {
        return teamId;
    }

    public void setApiResourceNextFixture(String apiResourceNextFixture) {
        this.apiResourceNextFixture = apiResourceNextFixture;
    }

    public void setResourceHtmlToImage(String resourceHtmlToImage) {
        this.resourceHtmlToImage = resourceHtmlToImage;
    }

    public void setApiResourceLeagueFixture(String apiResourceLeagueFixture) {
        this.apiResourceLeagueFixture = apiResourceLeagueFixture;
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

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }
}
