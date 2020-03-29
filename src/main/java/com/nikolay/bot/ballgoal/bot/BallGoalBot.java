package com.nikolay.bot.ballgoal.bot;

import com.nikolay.bot.ballgoal.cache.updater.CacheUpdater;
import com.nikolay.bot.ballgoal.cache.updater.LeagueCacheUpdater;
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

    private final CacheUpdater zenitCacheUpdater;

    private final LeagueCacheUpdater leagueCacheUpdater;

    public BallGoalBot(Environment env, CacheUpdater zenitCacheUpdater, LeagueCacheUpdater leagueCacheUpdater) {
        this.env = env;
        this.zenitCacheUpdater = zenitCacheUpdater;
        this.leagueCacheUpdater = leagueCacheUpdater;
    }

    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        String command = message.getText();
        long chatId = message.getChatId();
        try {
            if (command.equals(getCommandInfo())) {
                executeResult(getInfoMessage(), chatId);
            } else if (command.equals(getCommandZenit())) {
                executeResult(getZenitMessage(), chatId);
            } else if (command.equals(getCommandZenitJerusalem())) {
                zenitCacheUpdater.updateCache();
                executeResult(getJerusalemMessage(), chatId);
            } else if (command.equals(getCommandZenitSaintPetersburg())) {
                zenitCacheUpdater.updateCache();
                executeResult(getSaintPetersburgMessage(), chatId);
            } else if (command.equals(getCommandLeague())) {
                leagueCacheUpdater.updateCache();
                executeResult(getLeaguePhoto(), chatId);
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void executeResult(SendMessage message, long chatId) throws TelegramApiException {
        if (message.getText() != null) {
            message.setChatId(chatId);
            execute(message);
        } else {
            executeTryAgainResult(chatId);
        }
    }

    private void executeResult(SendPhoto photo, long chatId) throws TelegramApiException {
        if (photo.getPhoto().getAttachName() != null) {
            photo.setChatId(chatId);
            boolean isNewPhoto = isPhotoIdURL(photo.getPhoto().getAttachName());
            if (isNewPhoto) {
                Message file = execute(photo);
                leagueCacheUpdater.setTelegramFileCache(file);
            } else {
                execute(photo);
            }
        } else {
            executeTryAgainResult(chatId);
        }
    }

    private void executeTryAgainResult(long chatId) throws TelegramApiException {
        SendMessage tryAgainMessage = getTryAgainMessage();
        tryAgainMessage.setChatId(chatId);
        execute(tryAgainMessage);
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

    protected abstract String getCommandInfo();

    protected abstract String getCommandZenit();

    protected abstract String getCommandZenitJerusalem();

    protected abstract String getCommandZenitSaintPetersburg();

    protected abstract String getCommandLeague();

    protected abstract SendMessage getInfoMessage();

    protected abstract SendMessage getZenitMessage();

    protected abstract SendMessage getJerusalemMessage();

    protected abstract SendMessage getSaintPetersburgMessage();

    protected abstract SendPhoto getLeaguePhoto();

    protected abstract SendMessage getTryAgainMessage();
}
