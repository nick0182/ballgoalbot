package com.nikolay.bot.ballgoal.command.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikolay.bot.ballgoal.api.ApiRequest;
import com.nikolay.bot.ballgoal.command.Command;
import com.nikolay.bot.ballgoal.json.fixture.Fixture;
import com.nikolay.bot.ballgoal.json.fixture.ResultFixture;
import com.nikolay.bot.ballgoal.json.table.ResultTable;
import com.nikolay.bot.ballgoal.properties.ResourceProperties;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class LeagueStandingCommand implements Command<SendPhoto> {

    private ResourceProperties resourceProperties;

    private ReplyKeyboard keyboard;

    private ApiRequest apiRequestFixture;

    private ApiRequest apiRequestImage;

    private ObjectMapper objectMapper;

    private CommandData data = new CommandData();

    private SendPhoto cachedResult;

    private ZoneId zoneId;

    public LeagueStandingCommand(ResourceProperties resourceProperties,
                                 ReplyKeyboard keyboard,
                                 ApiRequest apiRequestFixture,
                                 ApiRequest apiRequestImage,
                                 ObjectMapper objectMapper) {
        this.resourceProperties = resourceProperties;
        this.keyboard = keyboard;
        this.apiRequestFixture = apiRequestFixture;
        this.apiRequestImage = apiRequestImage;
        this.objectMapper = objectMapper;
    }

    @Override
    public SendPhoto getResult() {
        ZonedDateTime refreshDate;
        try {
            refreshDate = data.fetchNextRefreshDate();
        } catch (NoSuchElementException ex) {
            data.setupNewRefreshDates();
            return fetchNewResult();
        }
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        if (now.compareTo(refreshDate) > 0) {
            data.removeRefreshDate(refreshDate);
            return fetchNewResult();
        } else {
            return cachedResult;
        }
    }

    private SendPhoto fetchNewResult() {
        try {
            String json = apiRequestImage.call(resourceProperties.getResourceHtmlToImage(), zoneId);
            ResultTable tableResult = objectMapper.readValue(json, ResultTable.class);
            return createResult(tableResult.getUrl());
        } catch (IOException e) {
            throw new RuntimeException("Problem occurred while obtaining table photo", e);
        }
    }

    private SendPhoto createResult(String url) {
        SendPhoto result = new SendPhoto();
        result.setReplyMarkup(keyboard);
        result.setPhoto(url);
        return (cachedResult = result);
    }

    public void setZoneId(String zoneId) {
        this.zoneId = ZoneId.of(zoneId);
    }

    // FIXME: retrieve field to parent class, no need for inner class as parent class is able to modify the field directly anyway
    private class CommandData {

        private TreeSet<ZonedDateTime> refreshDates = new TreeSet<>();

        private ZonedDateTime fetchNextRefreshDate() {
            return refreshDates.first();
        }

        private void removeRefreshDate(ZonedDateTime date) {
            refreshDates.remove(date);
        }

        private void setupNewRefreshDates() {
            try {
                String round = fetchRound();
                String resource = appendRoundToResource(resourceProperties.getApiResourceLeagueRoundDates(), round);
                String json = apiRequestFixture.call(resource, zoneId);
                ResultFixture fixtureResult = objectMapper.readValue(json, ResultFixture.class);
                ZonedDateTime now = ZonedDateTime.now(zoneId);
                List<Fixture> fixtures = fixtureResult.getApi().getFixtures();
                refreshDates = fixtures.stream()
                        .map(Fixture::getEventDate)
                        .map(event -> event.plusMinutes(115))
                        .filter(eventDate -> eventDate.compareTo(now) > 0)
                        .collect(Collectors.toCollection(TreeSet::new));
                assert !refreshDates.isEmpty();
            } catch (IOException e) {
                throw new RuntimeException("Problem occurred while obtaining round dates", e);
            }
        }

        private String fetchRound() throws IOException {
            String json = apiRequestFixture.call(resourceProperties.getApiResourceLeagueFixture(), zoneId);
            ResultFixture fixtureResult = objectMapper.readValue(json, ResultFixture.class);
            return fixtureResult.getApi().getFixtures().get(0).getRound();
        }

        private String appendRoundToResource(String resource, String round) {
            String preparedRoundString = round.replace(" ", "_");
            return resource + preparedRoundString;
        }
    }
}
