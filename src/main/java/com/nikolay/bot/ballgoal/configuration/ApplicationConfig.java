package com.nikolay.bot.ballgoal.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikolay.bot.ballgoal.api.ApiRequest;
import com.nikolay.bot.ballgoal.api.impl.ApiRequestFixture;
import com.nikolay.bot.ballgoal.api.impl.ApiRequestImage;
import com.nikolay.bot.ballgoal.bot.BallGoalBot;
import com.nikolay.bot.ballgoal.command.Command;
import com.nikolay.bot.ballgoal.command.impl.LeagueStandingCommand;
import com.nikolay.bot.ballgoal.command.impl.SimpleTextCommand;
import com.nikolay.bot.ballgoal.command.impl.ZenitCommand;
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
            protected Command<SendMessage> getInfoCommand() {
                return infoCommand();
            }

            @Override
            protected Command<SendMessage> getTimezoneCommand() {
                return timezoneCommand();
            }

            @Override
            protected Command<SendMessage> getZenitJerusalemCommand() {
                return zenitJerusalemCommand();
            }

            @Override
            protected Command<SendMessage> getZenitSaintPetersburgCommand() {
                return zenitSaintPetersburgCommand();
            }

            @Override
            protected Command<SendPhoto> getLeagueStandingCommand() {
                return leagueStandingCommand();
            }
        };
    }

    // <-----------Keyboards---------------->

    @Bean
    @Lazy
    public ReplyKeyboard timezoneKeyboard() {
        ReplyKeyboardMarkup timezoneKeyboard = new ReplyKeyboardMarkup();
        timezoneKeyboard.setOneTimeKeyboard(true);
        timezoneKeyboard.setSelective(true);
        timezoneKeyboard.setResizeKeyboard(true);
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(new KeyboardButton(Commands.ZENIT_JERUSALEM));
        keyboardRow.add(new KeyboardButton(Commands.ZENIT_SAINT_PETERSBURG));
        timezoneKeyboard.setKeyboard(Collections.singletonList(keyboardRow));
        return timezoneKeyboard;
    }

    @Bean
    @Lazy
    public ReplyKeyboard removeKeyboard() {
        return new ReplyKeyboardRemove();
    }

    // <-----------Commands---------------->

    @Bean
    @Lazy
    @ConfigurationProperties(prefix = "info")
    public Command<SendMessage> infoCommand() {
        return new SimpleTextCommand(removeKeyboard());
    }

    @Bean
    @Lazy
    @ConfigurationProperties(prefix = "timezone")
    public Command<SendMessage> timezoneCommand() {
        return new SimpleTextCommand(timezoneKeyboard());
    }

    @Bean(initMethod = "init")
    @Lazy
    @ConfigurationProperties(prefix = "jerusalem")
    public Command<SendMessage> zenitJerusalemCommand() {
        return new ZenitCommand(resourceProperties(), removeKeyboard(), apiRequestFixture(), objectMapper());
    }

    @Bean(initMethod = "init")
    @Lazy
    @ConfigurationProperties(prefix = "moscow")
    public Command<SendMessage> zenitSaintPetersburgCommand() {
        return new ZenitCommand(resourceProperties(), removeKeyboard(), apiRequestFixture(), objectMapper());
    }

    @Bean
    @Lazy
    public Command<SendPhoto> leagueStandingCommand() {
        return new LeagueStandingCommand(resourceProperties(), removeKeyboard(),
                apiRequestFixture(), apiRequestImage(), objectMapper());
    }

    // <-----------Requests---------------->

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

    // <-----------Other---------------->

    @Bean
    @Lazy
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    @Bean
    @Lazy
    @ConfigurationProperties(prefix = "resource")
    public ResourceProperties resourceProperties() {
        return new ResourceProperties();
    }
}
