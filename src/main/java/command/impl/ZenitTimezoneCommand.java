package command.impl;

import api.ApiRequest;
import cache.CachedMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import command.ApiCommand;
import helper.TimeStringConverter;
import json.Fixture;
import json.Result;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ZenitTimezoneCommand implements ApiCommand {

    private ReplyKeyboard keyboard;

    private ApiRequest apiRequest;

    private final int cacheThresholdMinutes;

    private LocalTime lastApiTriggerTime;

    private ObjectMapper objectMapper;

    private TimeStringConverter timeStringConverter;

    private CachedMessage cachedMessage = null;

    public static final String EMOJI_HOME_TEAM =
            new String(new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x8F, (byte) 0xA0}, StandardCharsets.UTF_8);

    public static final String EMOJI_AWAY_TEAM =
            new String(new byte[]{(byte) 0xE2, (byte) 0x9C, (byte) 0x88}, StandardCharsets.UTF_8);

    public static final String EMOJI_DATE =
            new String(new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x93, (byte) 0x85}, StandardCharsets.UTF_8);

    public static final String EMOJI_TIME =
            new String(new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x95, (byte) 0xA3}, StandardCharsets.UTF_8);


    public ZenitTimezoneCommand(ReplyKeyboard keyboard, ApiRequest apiRequest, int cacheThresholdMinutes,
                                ObjectMapper objectMapper, TimeStringConverter timeStringConverter) {
        this.keyboard = keyboard;
        this.apiRequest = apiRequest;
        this.cacheThresholdMinutes = cacheThresholdMinutes;
        this.objectMapper = objectMapper;
        this.timeStringConverter = timeStringConverter;
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
            String resultText;
            try {
                String json = apiRequest.call(resource);
                Result result = objectMapper.readValue(json, Result.class);
                Fixture fixture = result.getApi().getFixtures().get(0);
                String homeTeam = fixture.getHomeTeam().getTeam_name();
                String awayTeam = fixture.getAwayTeam().getTeam_name();
                ZonedDateTime eventDateTime = fixture.getEventDate();
                String status = fixture.getStatus();
                String eventDate = timeStringConverter.getDateString(eventDateTime);
                String eventTime = timeStringConverter.getTimeString(eventDateTime, status);
                cachedMessage = new CachedMessage(homeTeam, awayTeam, eventDateTime, status);
                resultText = createResultText(homeTeam, awayTeam, eventDate, eventTime);
                sendMessage.setText(resultText);
            } catch (IOException e) {
                e.printStackTrace();
                sendMessage.setText("Server error. Please try again later");
            }
        } else {
            String status = cachedMessage.getStatus();
            String timezone = extractTimezoneFromResource(resource);
            String homeTeam = cachedMessage.getHomeTeam();
            String awayTeam = cachedMessage.getAwayTeam();
            ZonedDateTime eventDateTime = cachedMessage.getEventDateTime();
            String eventDate = timeStringConverter.getDateString(eventDateTime);
            String eventTime = timeStringConverter.getZonedTimeString(eventDateTime, status, timezone);
            String resultText = createResultText(homeTeam, awayTeam, eventDate, eventTime);
            sendMessage.setText(resultText);
        }
        return sendMessage;
    }

    private String createResultText(String homeTeam, String awayTeam, String eventDate, String eventTime) {
        return EMOJI_HOME_TEAM + " " + homeTeam + "\n" + EMOJI_AWAY_TEAM + " " + awayTeam
                + "\n" + EMOJI_DATE + " " + eventDate + "\n" + EMOJI_TIME + " " + eventTime;
    }

    private String extractTimezoneFromResource(String resource) {
        return resource.split("timezone=")[1];
    }

}
