package com.aws.codestar.projecttemplates.configuration;

import bot.BallGoalBot;
import com.aws.codestar.projecttemplates.controller.HelloWorldController;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import constants.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.Collections;

@Configuration
@ComponentScan({ "com.aws.codestar.projecttemplates.configuration" })
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

    @Bean
    public HelloWorldController helloWorld() {
        return new HelloWorldController(this.siteName);
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
                messageTimeToBeDefined,
                apiTimezoneMoscow,
                apiTimezoneJerusalem,
                apiZenitId,
                apiHost,
                apiKey,
                telegramBotName,
                telegramBotToken
        ) {
            @Override
            protected ReplyKeyboard getTimezoneKeyboardBean() {
                return timezoneKeyboard();
            }

            @Override
            protected ReplyKeyboard getRemoveKeyboardBean() {
                return removeKeyboard();
            }

            @Override
            protected ObjectMapper getObjectMapperBean() {
                return objectMapper();
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

}
