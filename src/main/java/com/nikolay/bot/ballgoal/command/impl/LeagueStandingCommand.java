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
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LeagueStandingCommand implements Command<SendPhoto> {

    private ResourceProperties resourceProperties;

    private ReplyKeyboard keyboard;

    private ApiRequest apiRequestFixture;

    private ApiRequest apiRequestImage;

    private ObjectMapper objectMapper;

    private SendPhoto cachedResult;

    private TreeSet<ZonedDateTime> refreshDates = new TreeSet<>();

    private ZoneId zoneId = ZoneId.of("Europe/London");

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
            refreshDate = refreshDates.first();
        } catch (NoSuchElementException ex) {
            setupNewRefreshDates();
            return fetchNewResult();
        }
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        if (now.compareTo(refreshDate) > 0) {
            refreshDates.remove(refreshDate);
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

    private void setupNewRefreshDates() {
        try {
            String round = fetchRound();
            String resource = appendRoundToResource(resourceProperties.getApiResourceLeagueRoundDates(), round);
            String json = apiRequestFixture.call(resource, zoneId);
            ResultFixture fixtureResult = objectMapper.readValue(json, ResultFixture.class);
            List<Fixture> fixtures = fixtureResult.getApi().getFixtures();
            ZonedDateTime now = ZonedDateTime.now(zoneId);
            Collection<ZonedDateTime> dates= fixtures.stream()
                    .map(Fixture::getEventDate)
                    .filter(eventDate -> eventDate.compareTo(now) > 0)
                    .collect(Collectors
                            .groupingBy(ZonedDateTime::getDayOfMonth, Collectors
                                    .reducing(now, date -> date.plusMinutes(130), BinaryOperator
                                            .maxBy(Comparator
                                                    .comparing(Function.identity())))))
                    .values();
            assert !dates.isEmpty();
            refreshDates = new TreeSet<>(dates);
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
