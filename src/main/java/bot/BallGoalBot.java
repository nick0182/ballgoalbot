package bot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import constants.Command;
import json.Fixture;
import json.Result;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Objects;

public class BallGoalBot extends TelegramLongPollingBot {

    private static final String TIMEZONE_MOSCOW_API = "Europe/Moscow";

    private static final String TIMEZONE_JERUSALEM_API = "Asia/Jerusalem";

    public static final String TIME_TO_BE_DEFINED = "Time to be defined";

    private static final int ZENIT_API_ID = 596;

    private static final String API_HOST = "api-football-v1.p.rapidapi.com";

    private static final String API_KEY = "1cccd3131bmshed7ddf66e006ec5p168f9fjsn3ab66e62ad85";

    private static final String BOT_NAME = "ballgoalbot";

    private static final String BOT_TOKEN = "929924919:AAGhxkyh6SEG21m7PGG9JmM81y9onmNncFE";

    private static final String EMOJI_HOME_TEAM =
            new String(new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x8F, (byte) 0xA0}, StandardCharsets.UTF_8);

    private static final String EMOJI_AWAY_TEAM =
            new String(new byte[]{(byte) 0xE2, (byte) 0x9C, (byte) 0x88}, StandardCharsets.UTF_8);

    private static final String EMOJI_DATE =
            new String(new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x93, (byte) 0x85}, StandardCharsets.UTF_8);

    private static final String EMOJI_TIME =
            new String(new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x95, (byte) 0xA3}, StandardCharsets.UTF_8);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE d LLL u");

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public void onUpdateReceived(Update update) {
        String command = update.getMessage().getText();
        SendMessage sendMessage;
        try {
            switch (command) {
                case Command.ZENIT:
                    sendMessage = new SendMessage();
                    sendMessage.setText("Choose your timezone");
                    sendMessage.setReplyMarkup(setupTimezoneKeyboard());
                    sendMessage.setChatId(update.getMessage().getChatId());
                    execute(sendMessage);
                    break;
                case Command.TIMEZONE_JERUSALEM:
                    sendMessage = new SendMessage();
                    setupMessage(sendMessage, TIMEZONE_JERUSALEM_API);
                    sendMessage.setChatId(update.getMessage().getChatId());
                    execute(sendMessage);
                    break;
                case Command.TIMEZONE_SAINT_PETERSBURG:
                    sendMessage = new SendMessage();
                    setupMessage(sendMessage, TIMEZONE_MOSCOW_API);
                    sendMessage.setChatId(update.getMessage().getChatId());
                    execute(sendMessage);
                    break;
            }
        } catch (IOException | TelegramApiException ex) {
            ex.printStackTrace();
        }

    }

    private void setupMessage(SendMessage sendMessage, String timezone) throws IOException {
        String jsonString = makeApiCall(timezone);
        Result result = convertJson(jsonString);
        Fixture fixture = result.getApi().getFixtures().get(0);
        String homeTeam = fixture.getHomeTeam().getTeam_name();
        String awayTeam = fixture.getAwayTeam().getTeam_name();
        ZonedDateTime eventDate = fixture.getEventDate();
        String date = getDateString(eventDate);
        String time = getTimeString(eventDate, fixture.getStatus());
        sendMessage.setText(EMOJI_HOME_TEAM + " " + homeTeam + "\n" + EMOJI_AWAY_TEAM + " " + awayTeam
                + "\n" + EMOJI_DATE + " " + date + "\n" + EMOJI_TIME + " " + time);
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove());
    }

    private ReplyKeyboard setupTimezoneKeyboard() {
        ReplyKeyboardMarkup timezoneKeyboard = new ReplyKeyboardMarkup();
        timezoneKeyboard.setOneTimeKeyboard(true);
        timezoneKeyboard.setSelective(true);
        timezoneKeyboard.setResizeKeyboard(true);
        KeyboardRow keyboardRow = new KeyboardRow();
        KeyboardButton firstKeyboardButton = new KeyboardButton(Command.TIMEZONE_JERUSALEM);
        KeyboardButton secondKeyboardButton = new KeyboardButton(Command.TIMEZONE_SAINT_PETERSBURG);
        keyboardRow.add(firstKeyboardButton);
        keyboardRow.add(secondKeyboardButton);
        timezoneKeyboard.setKeyboard(Collections.singletonList(keyboardRow));
        return timezoneKeyboard;
    }

    private String makeApiCall(String timezone) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        String resource = "/v2/fixtures/team/" + ZENIT_API_ID + "/next/1?timezone=" + timezone;
        URL url = new URL("https", API_HOST, resource);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("x-rapidapi-host", API_HOST)
                .addHeader("x-rapidapi-key", API_KEY)
                .build();

        Response response = okHttpClient.newCall(request).execute();
        return Objects.requireNonNull(response.body()).string();
    }

    private Result convertJson(String jsonString) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(jsonString, Result.class);
    }

    private String getDateString(ZonedDateTime date) {
        return date.format(DATE_FORMATTER);
    }

    private String getTimeString(ZonedDateTime date, String status) {
        if (status.equals(TIME_TO_BE_DEFINED)) {
            return TIME_TO_BE_DEFINED;
        } else {
            return date.toLocalTime().format(TIME_FORMATTER);
        }
    }

    public String getBotUsername() {
        return BOT_NAME;
    }

    public String getBotToken() {
        return BOT_TOKEN;
    }
}
