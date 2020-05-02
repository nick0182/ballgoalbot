package com.nikolay.bot.ballgoal.configuration;

import com.nikolay.bot.ballgoal.bot.BallGoalBot;
import com.nikolay.bot.ballgoal.cache.Cache;
import com.nikolay.bot.ballgoal.cache.ZenitTimeCache;
import com.nikolay.bot.ballgoal.cache.utils.CacheOffsetAppender;
import com.nikolay.bot.ballgoal.constants.Commands;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;

@SpringBootApplication
public class BallGoalBotApplication {

    @Value("${text.info}")
    private String textInfo;

    @Value("${text.timezone}")
    private String textTimezone;

    @Value("${offset.jerusalem}")
    private String jerusalem;

    @Value("${offset.saint-petersburg}")
    private String saintPetersburg;

    private final Environment env;

    public BallGoalBotApplication(Environment env) {
        this.env = env;
    }

    public static void main(String[] args) {
        SpringApplication.run(BallGoalBotApplication.class, args);
    }

    @Bean
    public InitializingBean initTelegram() {
        return ApiContextInitializer::init;
    }

    @Bean
    @DependsOn(value = "initTelegram")
    public void registerBot() throws TelegramApiRequestException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        telegramBotsApi.registerBot(ballGoalBot());
    }

    @Bean
    public TelegramLongPollingBot ballGoalBot() {
        return new BallGoalBot(env, infoMessage(), zenitMessage()) {
            @Override
            protected SendMessage getJerusalemMessage() {
                return jerusalemMessage();
            }

            @Override
            protected SendMessage getSaintPetersburgMessage() {
                return saintPetersburgMessage();
            }

            @Override
            protected SendPhoto getLeaguePhoto() {
                return leaguePhoto();
            }
        };
    }

    // <-----------Responses---------------->

    @Bean
    public SendMessage infoMessage() {
        SendMessage message = new SendMessage();
        message.setReplyMarkup(removeKeyboard());
        message.setText(textInfo);
        return message;
    }

    @Bean
    public SendMessage zenitMessage() {
        SendMessage message = new SendMessage();
        message.setReplyMarkup(timezoneKeyboard());
        message.setText(textTimezone);
        return message;
    }

    @Bean
    @Scope("prototype")
    public SendMessage jerusalemMessage() {
        SendMessage message = new SendMessage();
        message.setReplyMarkup(removeKeyboard());
        message.setText(getOffsetCacheText(ZoneOffset.of(jerusalem)));
        return message;
    }

    @Bean
    @Scope("prototype")
    public SendMessage saintPetersburgMessage() {
        SendMessage message = new SendMessage();
        message.setReplyMarkup(removeKeyboard());
        message.setText(getOffsetCacheText(ZoneOffset.of(saintPetersburg)));
        return message;
    }

    private String getOffsetCacheText(ZoneOffset offset) {
        ZenitTimeCache zenitTimeCache = zenitCache().getCache();
        if (zenitTimeCache != null) {
            String cache = zenitTimeCache.getCache();
            LocalDateTime dateTime = zenitTimeCache.getDateTime();
            return cache + CacheOffsetAppender.appendOffset(dateTime, offset);
        } else {
            return null;
        }
    }

    @Bean
    @Scope("prototype")
    public SendPhoto leaguePhoto() {
        SendPhoto photo = new SendPhoto();
        photo.setReplyMarkup(removeKeyboard());
        photo.setPhoto(leagueCache().getCache());
        return photo;
    }

    // <-----------Cache---------------->

    @Bean
    public Cache<ZenitTimeCache> zenitCache() {
        return new Cache<>();
    }

    @Bean
    public Cache<InputFile> leagueCache() {
        return new Cache<>();
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
}
