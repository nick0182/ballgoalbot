package com.nikolay.bot.ballgoal.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikolay.bot.ballgoal.api.ApiRequest;
import com.nikolay.bot.ballgoal.api.impl.ApiRequestFootball;
import com.nikolay.bot.ballgoal.bot.BallGoalBot;
import com.nikolay.bot.ballgoal.command.ApiCommand;
import com.nikolay.bot.ballgoal.command.TextCommand;
import com.nikolay.bot.ballgoal.command.impl.ZenitCommand;
import com.nikolay.bot.ballgoal.command.impl.ZenitTimezoneCommand;
import com.nikolay.bot.ballgoal.constants.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.Collections;

@SpringBootApplication
public class ApplicationConfig {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationConfig.class);

    public static void main(String[] args) {
        SpringApplication.run(ApplicationConfig.class, args);
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
    @ConfigurationProperties(prefix = "bot")
    public TelegramLongPollingBot ballGoalBot() {
        return new BallGoalBot() {
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
        return new ZenitTimezoneCommand(removeKeyboard(), apiRequest(), objectMapper(),
                15, "Time to be defined");
    }

    @Bean
    @Lazy
    @ConfigurationProperties(prefix = "api")
    public ApiRequest apiRequest() {
        return new ApiRequestFootball();
    }

}
