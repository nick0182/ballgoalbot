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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Objects;

import static constants.Emojis.*;

public class BallGoalBot extends TelegramLongPollingBot {

    private String messageTimeToBeDefined;

    private String apiTimezoneMoscow;

    private String apiTimezoneJerusalem;

    private String apiZenitId;

    private String apiHost;

    private String apiKey;

    private String telegramBotName;

    private String telegramBotToken;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE d MMMM u");

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public BallGoalBot(
            String messageTimeToBeDefined,
            String apiTimezoneMoscow,
            String apiTimezoneJerusalem,
            String apiZenitId,
            String apiHost,
            String apiKey,
            String telegramBotName,
            String telegramBotToken) {
        this.messageTimeToBeDefined = messageTimeToBeDefined;
        this.apiTimezoneMoscow = apiTimezoneMoscow;
        this.apiTimezoneJerusalem = apiTimezoneJerusalem;
        this.apiZenitId = apiZenitId;
        this.apiHost = apiHost;
        this.apiKey = apiKey;
        this.telegramBotName = telegramBotName;
        this.telegramBotToken = telegramBotToken;
    }

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
                    setupMessage(sendMessage, apiTimezoneJerusalem);
                    sendMessage.setChatId(update.getMessage().getChatId());
                    execute(sendMessage);
                    break;
                case Command.TIMEZONE_SAINT_PETERSBURG:
                    sendMessage = new SendMessage();
                    setupMessage(sendMessage, apiTimezoneMoscow);
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
        String resource = "/v2/fixtures/team/" + apiZenitId + "/next/1?timezone=" + timezone;
        URL url = new URL("https", apiHost, resource);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("x-rapidapi-host", apiHost)
                .addHeader("x-rapidapi-key", apiKey)
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
        if (status.equals(messageTimeToBeDefined)) {
            return messageTimeToBeDefined;
        } else {
            return date.toLocalTime().format(TIME_FORMATTER);
        }
    }

    public String getBotUsername() {
        return telegramBotName;
    }

    public String getBotToken() {
        return telegramBotToken;
    }
}
