package com.nikolay.bot.ballgoal.command.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikolay.bot.ballgoal.api.ApiRequest;
import com.nikolay.bot.ballgoal.command.Command;
import com.nikolay.bot.ballgoal.constants.Formatter;
import com.nikolay.bot.ballgoal.json.fixture.Fixture;
import com.nikolay.bot.ballgoal.json.fixture.ResultFixture;
import com.nikolay.bot.ballgoal.properties.ResourceProperties;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.io.IOException;
import java.time.Duration;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static com.nikolay.bot.ballgoal.constants.Emojis.*;

public class ZenitTimezoneCommand implements Command<SendMessage> {

    private ResourceProperties resourceProperties;

    private ReplyKeyboard keyboard;

    private ApiRequest apiRequest;

    private ObjectMapper objectMapper;

    private SendMessage cachedResult;

    private ZoneId zoneId;

    private int fixtureId;

    private ZonedDateTime refreshDate;

    public ZenitTimezoneCommand(ResourceProperties resourceProperties, ReplyKeyboard keyboard,
                                ApiRequest apiRequest, ObjectMapper objectMapper) {
        this.resourceProperties = resourceProperties;
        this.keyboard = keyboard;
        this.apiRequest = apiRequest;
        this.objectMapper = objectMapper;
    }

    private void init() throws IOException {
        Fixture fixture = fetchFixtureInPlay().orElse(fetchNextFixture());
        fixtureId = fixture.getFixture_id();
        refreshDate = ZonedDateTime.now(zoneId);
    }

    private Optional<Fixture> fetchFixtureInPlay() throws IOException {
        ResultFixture result = callApi(resourceProperties.getApiResourceFixturesInPlay());
        return result.getApi().getFixtures().stream().filter(this::isTeamPlayingNow).findAny();
    }

    private Fixture fetchNextFixture() throws IOException {
        ResultFixture result = callApi(resourceProperties.getApiResourceNextFixture());
        return result.getApi().getFixtures().get(0);
    }

    private Fixture fetchFixture() throws IOException {
        ResultFixture result = callApi(resourceProperties.getApiResourceFixture() + fixtureId);
        return result.getApi().getFixtures().get(0);
    }

    private boolean isTeamPlayingNow(Fixture fixture) {
        int teamId = resourceProperties.getTeamId();
        int homeTeamId = fixture.getHomeTeam().getTeam_id();
        int awayTeamId = fixture.getAwayTeam().getTeam_id();
        return homeTeamId == teamId || awayTeamId == teamId;
    }

    private ResultFixture callApi(String resource) throws IOException {
        String json = apiRequest.call(resource, zoneId);
        return objectMapper.readValue(json, ResultFixture.class);
    }

    private String createResultText(String homeTeam, String awayTeam, ZonedDateTime eventDateTime) {
        return EMOJI_HOME_TEAM + " " + homeTeam +
                "\n" + EMOJI_AWAY_TEAM + " " + awayTeam +
                "\n" + EMOJI_DATE + " " + eventDateTime.format(Formatter.DATE_FORMATTER) +
                "\n" + EMOJI_TIME + " " + eventDateTime.toLocalTime().format(Formatter.TIME_FORMATTER);
    }

    private String createResultText(String homeTeam, String awayTeam, String status) {
        return EMOJI_HOME_TEAM + " " + homeTeam +
                "\n" + EMOJI_AWAY_TEAM + " " + awayTeam +
                "\n" + EMOJI_STATUS + " " + status;
    }

    private String createResultText(String homeTeam, String awayTeam, int currentMinute, String score) {
        return EMOJI_HOME_TEAM + " " + homeTeam +
                "\n" + EMOJI_AWAY_TEAM + " " + awayTeam +
                "\n" + EMOJI_MINUTE + " " + currentMinute +
                "\n" + EMOJI_BALL + " " + score;
    }

    private String createResultText(String homeTeam, String awayTeam, String status, String score) {
        return EMOJI_HOME_TEAM + " " + homeTeam +
                "\n" + EMOJI_AWAY_TEAM + " " + awayTeam +
                "\n" + EMOJI_STATUS + " " + status +
                "\n" + EMOJI_BALL + " " + score;
    }

    private String createScore(int goalsHomeTeam, int goalsAwayTeam) {
        return goalsHomeTeam + ":" + goalsAwayTeam;
    }

    private SendMessage generateNewCache(String text) {
        cachedResult = new SendMessage();
        cachedResult.setReplyMarkup(keyboard);
        cachedResult.setText(text);
        return cachedResult;
    }

    public SendMessage getResult() {
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        if (now.compareTo(refreshDate) > 0) {
            try {
                Fixture fixture = fetchFixture();
                defineData(fixture);
                String text = createText(fixture);
                return generateNewCache(text);
            } catch (IOException ex) {
                SendMessage errorMessage = new SendMessage();
                errorMessage.setReplyMarkup(keyboard);
                errorMessage.setText("Error occurred");
                return cachedResult;
            }
        } else {
            return cachedResult;
        }
    }

    private void defineData(Fixture fixture) throws IOException {
        switch (fixture.getStatus()) {
            case Status.TO_BE_DEFINED:
                refreshDate = ZonedDateTime.now(zoneId).plusMinutes(30);
                break;
            case Status.NOT_STARTED:
                refreshDate = calculateDate(fixture.getEventDate());
                break;
            case Status.FIRST_HALF:
            case Status.SECOND_HALF:
            case Status.EXTRA_TIME:
            case Status.PENALTIES:
            case Status.BREAK_TIME:
            case Status.HALFTIME:
            case Status.SUSPENDED:
            case Status.INTERRUPTED:
                refreshDate = ZonedDateTime.now(zoneId).plusMinutes(2);
                break;
            case Status.FINISHED:
            case Status.FINISHED_AFTER_EXTRA_TIME:
            case Status.FINISHED_AFTER_PENALTY:
            case Status.POSTPONED:
            case Status.CANCELLED:
            case Status.ABANDONED:
            case Status.TECHNICAL_LOSS:
            case Status.WALK_OVER:
                refreshDate = ZonedDateTime.now(zoneId).plusMinutes(30);
                fixtureId = fetchNextFixture().getFixture_id();
                break;
        }
    }

    private ZonedDateTime calculateDate(ZonedDateTime eventDate) {
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        Period period = Period.between(now.toLocalDate(), eventDate.toLocalDate());
        if (period.getDays() > 7) {
            return now.plusDays(1);
        } else if (period.getDays() > 3) {
            return now.plusHours(12);
        } else if (period.getDays() > 1){
            return now.plusHours(3);
        } else {
            Duration duration = Duration.between(now.toLocalTime(), eventDate.toLocalTime());
            if (duration.getSeconds() > 7200) {
                return now.plusMinutes(30);
            } else {
                return eventDate.plusMinutes(2);
            }
        }
    }

    private String createText(Fixture fixture) {
        String status = fixture.getStatus();
        String homeTeam = fixture.getHomeTeam().getTeam_name();
        String awayTeam = fixture.getAwayTeam().getTeam_name();
        int goalsHomeTeam = fixture.getGoalsHomeTeam();
        int goalsAwayTeam = fixture.getGoalsAwayTeam();
        switch (status) {
            case Status.TO_BE_DEFINED:
            case Status.POSTPONED:
            case Status.CANCELLED:
            case Status.ABANDONED:
            case Status.TECHNICAL_LOSS:
            case Status.WALK_OVER:
                return createResultText(homeTeam, awayTeam, status);
            case Status.NOT_STARTED:
                return createResultText(homeTeam, awayTeam, fixture.getEventDate());
            case Status.FIRST_HALF:
            case Status.SECOND_HALF:
            case Status.EXTRA_TIME:
            case Status.PENALTIES:
            case Status.BREAK_TIME:
            case Status.HALFTIME:
            case Status.SUSPENDED:
            case Status.INTERRUPTED:
                int currentMinute = fixture.getElapsed();
                return createResultText(homeTeam, awayTeam, currentMinute, createScore(goalsHomeTeam, goalsAwayTeam));
            case Status.FINISHED:
            case Status.FINISHED_AFTER_EXTRA_TIME:
            case Status.FINISHED_AFTER_PENALTY:
                return createResultText(homeTeam, awayTeam, status, createScore(goalsHomeTeam, goalsAwayTeam));
            default:
                throw new RuntimeException("Not a valid status");
        }
    }

    public void setZoneId(String zoneId) {
        this.zoneId = ZoneId.of(zoneId);
    }

    private static class Status {

        private static final String TO_BE_DEFINED = "Time To Be Defined";
        private static final String NOT_STARTED = "Not Started";
        private static final String FIRST_HALF = "First Half";
        private static final String HALFTIME = "Halftime";
        private static final String SECOND_HALF = "Second Half";
        private static final String EXTRA_TIME = "Extra Time";
        private static final String PENALTIES = "Penalty In Progress";
        private static final String FINISHED = "Match Finished";
        private static final String FINISHED_AFTER_EXTRA_TIME = "Match Finished After Extra Time";
        private static final String FINISHED_AFTER_PENALTY = "Match Finished After Penalty";
        private static final String BREAK_TIME = "Break Time";
        private static final String SUSPENDED = "Match Suspended";
        private static final String INTERRUPTED = "Match Interrupted";
        private static final String POSTPONED = "Match Postponed";
        private static final String CANCELLED = "Match Cancelled";
        private static final String ABANDONED = "Match Abandoned";
        private static final String TECHNICAL_LOSS = "Technical Loss";
        private static final String WALK_OVER = "WalkOver";
    }
}
