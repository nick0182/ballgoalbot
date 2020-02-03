package bot;

import cache.CachedMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import constants.Command;
import json.Fixture;
import json.Result;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.URL;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static constants.Emojis.*;

public abstract class BallGoalBot extends TelegramLongPollingBot {

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

    private static final Logger LOG = LoggerFactory.getLogger(BallGoalBot.class);

    private static final int THRESHOLD_MINUTES = 15;

    private LocalTime lastAPITriggerTime;

    private AtomicReference<CachedMessage> cachedMessageReference = new AtomicReference<>();

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
        lastAPITriggerTime = LocalTime.now(ZoneId.systemDefault()).minusMinutes(THRESHOLD_MINUTES);
        cachedMessageReference.set(new CachedMessage("", "", "", ""));
    }

    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        String command = message.getText();
        LOG.info("Command received: {}", command);
        long chatId = message.getChatId();
        switch (command) {
            case Command.ZENIT:
                executeTimezoneMessage(chatId);
                break;
            case Command.TIMEZONE_JERUSALEM:
                executeResultMessage(chatId, apiTimezoneJerusalem);
                break;
            case Command.TIMEZONE_SAINT_PETERSBURG:
                executeResultMessage(chatId, apiTimezoneMoscow);
                break;
        }
    }

    private void executeTimezoneMessage(long chatId) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setText("Choose your timezone");
            sendMessage.setReplyMarkup(getTimezoneKeyboardBean());
            sendMessage.setChatId(chatId);
            execute(sendMessage);
        } catch (TelegramApiException ex) {
            ex.printStackTrace();
        }
    }

    private void executeResultMessage(long chatId, String timezone) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setReplyMarkup(getRemoveKeyboardBean());
        sendMessage.setChatId(chatId);
        LocalTime now = LocalTime.now(ZoneId.systemDefault());
        int threshold = now.minusMinutes(THRESHOLD_MINUTES).compareTo(lastAPITriggerTime);

        if (threshold > 0) {
            lastAPITriggerTime = now;
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(() -> {
                String resultText;
                try {
                    String json = makeApiCall(timezone);
                    Result result = getObjectMapperBean().readValue(json, Result.class);
                    Fixture fixture = result.getApi().getFixtures().get(0);
                    String homeTeam = fixture.getHomeTeam().getTeam_name();
                    String awayTeam = fixture.getAwayTeam().getTeam_name();
                    ZonedDateTime event = fixture.getEventDate();
                    String eventDate = getDateString(event);
                    String eventTime = getTimeString(event, fixture.getStatus());
                    cachedMessageReference.set(new CachedMessage(homeTeam, awayTeam, eventDate, eventTime));
                    resultText = createResultText(homeTeam, awayTeam, eventDate, eventTime);
                    sendMessage.setText(resultText);
                } catch (IOException e) {
                    e.printStackTrace();
                    sendMessage.setText("Server error. Please try again later");
                }
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            });
            executorService.shutdown();
        } else {
            CachedMessage cachedMessage = cachedMessageReference.get();
            String resultText = createResultText(cachedMessage.getHomeTeam(), cachedMessage.getAwayTeam(),
                    cachedMessage.getEventDate(), cachedMessage.getEventTime());
            sendMessage.setText(resultText);
            try {
                execute(sendMessage);
            } catch (TelegramApiException ex) {
                ex.printStackTrace();
            }
        }

    }

    private String createResultText(String homeTeam, String awayTeam, String eventDate, String eventTime) {
        return EMOJI_HOME_TEAM + " " + homeTeam + "\n" + EMOJI_AWAY_TEAM + " " + awayTeam
                + "\n" + EMOJI_DATE + " " + eventDate + "\n" + EMOJI_TIME + " " + eventTime;
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

    protected abstract ReplyKeyboard getTimezoneKeyboardBean();

    protected abstract ReplyKeyboard getRemoveKeyboardBean();

    protected abstract ObjectMapper getObjectMapperBean();

}
