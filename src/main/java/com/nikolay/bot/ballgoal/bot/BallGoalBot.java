package com.nikolay.bot.ballgoal.bot;

import com.nikolay.bot.ballgoal.cache.Cache;
import com.nikolay.bot.ballgoal.constants.Commands;
import org.springframework.core.env.Environment;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

public abstract class BallGoalBot extends TelegramLongPollingBot {

    private final Environment env;

    private final Cache<String> leagueCache;

    public BallGoalBot(Environment env, Cache<String> leagueCache) {
        this.env = env;
        this.leagueCache = leagueCache;
    }

    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        String command = message.getText();
        long chatId = message.getChatId();
        try {
            switch (command) {
                case Commands.INFO:
                    executeResult(getInfoMessage(), chatId);
                    break;
                case Commands.ZENIT:
                    executeResult(getZenitMessage(), chatId);
                    break;
                case Commands.ZENIT_JERUSALEM:
                    executeResult(getJerusalemMessage(), chatId);
                    break;
                case Commands.ZENIT_SAINT_PETERSBURG:
                    executeResult(getSaintPetersburgMessage(), chatId);
                    break;
                case Commands.STANDING:
                    executeResult(getLeaguePhoto(), chatId);
                    break;
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void executeResult(SendMessage message, long chatId) throws TelegramApiException {
        message.setChatId(chatId);
        execute(message);
    }

    private void executeResult(SendPhoto photo, long chatId) throws TelegramApiException {
        boolean isNewPhoto = isPhotoIdURL(photo.getPhoto().getAttachName());
        photo.setChatId(chatId);
        Message file = execute(photo);
        if (isNewPhoto) {
            leagueCache.setCache(file.getPhoto().get(0).getFileId());
        }
    }

    private boolean isPhotoIdURL(String photoId) {
        try {
            new URL(photoId);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public String getBotUsername() {
        return Objects.requireNonNull(env.getProperty("BOT_NAME"));
    }

    public String getBotToken() {
        return Objects.requireNonNull(env.getProperty("BOT_TOKEN"));
    }

    protected abstract SendMessage getInfoMessage();

    protected abstract SendMessage getZenitMessage();

    protected abstract SendMessage getJerusalemMessage();

    protected abstract SendMessage getSaintPetersburgMessage();

    protected abstract SendPhoto getLeaguePhoto();
}
