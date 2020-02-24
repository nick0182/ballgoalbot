package com.nikolay.bot.ballgoal.command.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikolay.bot.ballgoal.api.ApiRequest;
import com.nikolay.bot.ballgoal.cache.CachedMessage;
import com.nikolay.bot.ballgoal.command.ApiCommand;
import com.nikolay.bot.ballgoal.helper.TimeStringConverter;
import com.nikolay.bot.ballgoal.helper.ZoneGetter;
import com.nikolay.bot.ballgoal.json.Fixture;
import com.nikolay.bot.ballgoal.json.Result;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.io.IOException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static com.nikolay.bot.ballgoal.constants.Emoji.*;

public class ZenitTimezoneCommand implements ApiCommand {

    private ReplyKeyboard keyboard;

    private ApiRequest apiRequest;

    private int cacheThresholdMinutes;

    private LocalTime lastApiTriggerTime;

    private ObjectMapper objectMapper;

    private String messageTimeNotDefined;

    private CachedMessage cachedMessage = null;

    public ZenitTimezoneCommand(ReplyKeyboard keyboard, ApiRequest apiRequest,
                                ObjectMapper objectMapper, int cacheThresholdMinutes, String messageTimeNotDefined) {
        this.keyboard = keyboard;
        this.apiRequest = apiRequest;
        this.objectMapper = objectMapper;
        this.cacheThresholdMinutes = cacheThresholdMinutes;
        this.messageTimeNotDefined = messageTimeNotDefined;
        lastApiTriggerTime = LocalTime.now(ZoneId.systemDefault()).minusMinutes(cacheThresholdMinutes);
    }

    @Override
    public SendMessage generateMessage(String resource) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setReplyMarkup(keyboard);
        LocalTime now = LocalTime.now(ZoneId.systemDefault());
        int threshold = now.minusMinutes(cacheThresholdMinutes).compareTo(lastApiTriggerTime);

        if (threshold > 0) {
            lastApiTriggerTime = now;
            try {
                String json = apiRequest.call(resource);
                Result result = objectMapper.readValue(json, Result.class);
                Fixture fixture = result.getApi().getFixtures().get(0);
                String homeTeam = fixture.getHomeTeam().getTeam_name();
                String awayTeam = fixture.getAwayTeam().getTeam_name();
                ZonedDateTime eventDateTime = fixture.getEventDate();
                String eventDate = TimeStringConverter.getDateString(eventDateTime);
                String status = fixture.getStatus();
                String eventTime;
                // TODO: add new statuses (i.e "in progress")
                if (status.equals(messageTimeNotDefined)) {
                    eventTime = messageTimeNotDefined;
                } else {
                    eventTime = TimeStringConverter.getTimeString(eventDateTime);
                }
                cachedMessage = new CachedMessage(homeTeam, awayTeam, eventDateTime, status);
                String resultText = createResultText(homeTeam, awayTeam, eventDate, eventTime);
                sendMessage.setText(resultText);
            } catch (IOException e) {
                e.printStackTrace();
                sendMessage.setText("Server error. Please try again later");
            }
        } else {
            String homeTeam = cachedMessage.getHomeTeam();
            String awayTeam = cachedMessage.getAwayTeam();
            ZonedDateTime eventDateTime = cachedMessage.getEventDateTime();
            String eventDate = TimeStringConverter.getDateString(eventDateTime);
            String status = cachedMessage.getStatus();
            String eventTime;
            if (status.equals(messageTimeNotDefined)) {
                eventTime = messageTimeNotDefined;
            } else {
                ZoneId zone = ZoneGetter.getFromResource(resource);
                eventTime = TimeStringConverter.getZonedTimeString(eventDateTime, zone);
            }
            String resultText = createResultText(homeTeam, awayTeam, eventDate, eventTime);
            sendMessage.setText(resultText);
        }
        return sendMessage;
    }

    private String createResultText(String homeTeam, String awayTeam, String eventDate, String eventTime) {
        return EMOJI_HOME_TEAM + " " + homeTeam + "\n" + EMOJI_AWAY_TEAM + " " + awayTeam
                + "\n" + EMOJI_DATE + " " + eventDate + "\n" + EMOJI_TIME + " " + eventTime;
    }

}
