package com.aws.codestar.projecttemplates.configuration;

import api.ApiRequest;
import bot.BallGoalBot;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import command.ApiCommand;
import command.TextCommand;
import command.impl.ZenitCommand;
import command.impl.ZenitTimezoneCommand;
import constants.Command;
import helper.TimeStringConverter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.net.URL;
import java.util.Collections;
import java.util.Objects;

@SpringBootApplication
@PropertySource("classpath:application.properties")
public class ApplicationConfig {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationConfig.class);

    @Value("${HelloWorld.SiteName}")
    private String siteName;

    @Value("${message.time.tbd}")
    private String messageTimeToBeDefined;

    @Value("${api.timezone.moscow}")
    private String apiTimezoneMoscow;

    @Value("${api.timezone.jerusalem}")
    private String apiTimezoneJerusalem;

    @Value("${api.zenit.id}")
    private String apiZenitId;

    @Value("${api.host}")
    private String apiHost;

    @Value("${api.key}")
    private String apiKey;

    @Value("${telegram.bot.name}")
    private String telegramBotName;

    @Value("${telegram.bot.token}")
    private String telegramBotToken;

    @Value("${api.cache.threshold.minutes}")
    private String apiCacheThresholdMinutes;

    public static void main(String[] args) {
        SpringApplication.run(ApplicationConfig.class, args);
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeHolderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public InitializingBean initTelegram() {
        return ApiContextInitializer::init;
    }

    @Bean
    @DependsOn(value = "initTelegram")
    public TelegramBotsApi registerBot() throws TelegramApiRequestException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        telegramBotsApi.registerBot(ballGoalBot());
        LOG.info("Bot registered");
        return telegramBotsApi;
    }

    @Bean
    public BallGoalBot ballGoalBot() {
        return new BallGoalBot(
                apiTimezoneMoscow,
                apiTimezoneJerusalem,
                apiZenitId,
                telegramBotName,
                telegramBotToken
        ) {
            @Override
            protected TextCommand getZenitCommand() {
                return zenitCommand();
            }

            @Override
            protected ApiCommand getZenitTimezoneCommand() {
                return zenitTimezoneCommand();
            }
        };
    }

    @Bean
    @Lazy
    public ReplyKeyboard timezoneKeyboard() {
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

    @Bean
    @Lazy
    public ReplyKeyboard removeKeyboard() {
        return new ReplyKeyboardRemove();
    }

    @Bean
    @Lazy
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    @Bean
    @Lazy
    public TextCommand zenitCommand() {
        return new ZenitCommand(timezoneKeyboard());
    }

    @Bean
    @Lazy
    public ApiCommand zenitTimezoneCommand() {
        return new ZenitTimezoneCommand(removeKeyboard(), apiRequest(),
                Integer.parseInt(apiCacheThresholdMinutes), objectMapper(), timeStringConverter());
    }

    @Bean
    @Lazy
    public ApiRequest apiRequest() {
        return resource -> {
            OkHttpClient okHttpClient = new OkHttpClient();
            URL url = new URL("https", apiHost, resource);
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("x-rapidapi-host", apiHost)
                    .addHeader("x-rapidapi-key", apiKey)
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            return Objects.requireNonNull(response.body()).string();
        };
    }

    @Bean
    @Lazy
    public TimeStringConverter timeStringConverter() {
        return new TimeStringConverter(messageTimeToBeDefined, apiTimezoneMoscow, apiTimezoneJerusalem);
    }

}