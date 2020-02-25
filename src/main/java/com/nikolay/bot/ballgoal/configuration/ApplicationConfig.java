package com.nikolay.bot.ballgoal.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikolay.bot.ballgoal.api.ApiRequest;
import com.nikolay.bot.ballgoal.api.impl.ApiRequestFixture;
import com.nikolay.bot.ballgoal.api.impl.ApiRequestImage;
import com.nikolay.bot.ballgoal.bot.BallGoalBot;
import com.nikolay.bot.ballgoal.cache.ZenitCache;
import com.nikolay.bot.ballgoal.command.Command;
import com.nikolay.bot.ballgoal.command.impl.LeagueStandingCommand;
import com.nikolay.bot.ballgoal.command.impl.ZenitCommand;
import com.nikolay.bot.ballgoal.command.impl.ZenitTimezoneCommand;
import com.nikolay.bot.ballgoal.constants.Commands;
import com.nikolay.bot.ballgoal.properties.ResourceProperties;
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
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
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
            protected Command<SendMessage> getZenitCommand() {
                return zenitCommand();
            }

            @Override
            protected Command<SendMessage> getZenitTimezoneJerusalemCommand() {
                return zenitTimezoneJerusalemCommand();
            }

            @Override
            public Command<SendMessage> getZenitTimezoneMoscowCommand() {
                return zenitTimezoneMoscowCommand();
            }

            @Override
            protected Command<SendPhoto> getLeagueStandingCommand() {
                return leagueStandingCommand();
            }
        };
    }

    @Bean
    @Lazy
    @ConfigurationProperties(prefix = "resource")
    public ResourceProperties resourceProperties() {
        return new ResourceProperties();
    }

    @Bean
    @Lazy
    public ReplyKeyboard timezoneKeyboard() {
        ReplyKeyboardMarkup timezoneKeyboard = new ReplyKeyboardMarkup();
        timezoneKeyboard.setOneTimeKeyboard(true);
        timezoneKeyboard.setSelective(true);
        timezoneKeyboard.setResizeKeyboard(true);
        KeyboardRow keyboardRow = new KeyboardRow();
        KeyboardButton firstKeyboardButton = new KeyboardButton(Commands.TIMEZONE_JERUSALEM);
        KeyboardButton secondKeyboardButton = new KeyboardButton(Commands.TIMEZONE_SAINT_PETERSBURG);
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
    public Command<SendMessage> zenitCommand() {
        return new ZenitCommand(timezoneKeyboard());
    }

    @Bean(initMethod = "init")
    @Lazy
    @ConfigurationProperties(prefix = "jerusalem")
    public Command<SendMessage> zenitTimezoneJerusalemCommand() {
        return new ZenitTimezoneCommand(resourceProperties(), removeKeyboard(), apiRequestFixture(), objectMapper());
    }

    @Bean(initMethod = "init")
    @Lazy
    @ConfigurationProperties(prefix = "moscow")
    public Command<SendMessage> zenitTimezoneMoscowCommand() {
        return new ZenitTimezoneCommand(resourceProperties(), removeKeyboard(), apiRequestFixture(), objectMapper());
    }

    @Bean
    @Lazy
    @ConfigurationProperties(prefix = "jerusalem")
    public Command<SendPhoto> leagueStandingCommand() {
        return new LeagueStandingCommand(resourceProperties(), removeKeyboard(),
                apiRequestFixture(), apiRequestImage(), objectMapper());
    }

    @Bean
    @Lazy
    public ZenitCache zenitCache() {
        return new ZenitCache(15);
    }

    @Bean
    @Lazy
    @ConfigurationProperties(prefix = "api.fixture")
    public ApiRequest apiRequestFixture() {
        return new ApiRequestFixture();
    }

    @Bean
    @Lazy
    @ConfigurationProperties(prefix = "api.image")
    public ApiRequest apiRequestImage() {
        return new ApiRequestImage();
    }

}
