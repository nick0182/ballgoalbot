package com.nikolay.bot.ballgoal.bot;

import com.nikolay.bot.ballgoal.command.Command;
import com.nikolay.bot.ballgoal.constants.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.MalformedURLException;
import java.net.URL;

// TODO: add Lombok
// TODO: add Spring Integration flow
public abstract class BallGoalBot extends TelegramLongPollingBot {

    private String name;

    private String token;

    private static final Logger LOG = LoggerFactory.getLogger(BallGoalBot.class);

    //TODO: test it only with Spring context (Integration test)
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        String command = message.getText();
        LOG.debug("Command received: {}", command);
        long chatId = message.getChatId();
        try {
            switch (command) {
                case Commands.ZENIT:
                    executeMessage(getZenitCommand(), chatId);
                    break;
                case Commands.ZENIT_JERUSALEM:
                    executeMessage(getZenitJerusalemCommand(), chatId);
                    break;
                case Commands.ZENIT_SAINT_PETERSBURG:
                    executeMessage(getZenitSaintPetersburgCommand(), chatId);
                    break;
                case Commands.LEAGUE_STANDING:
                    executeMessage(getLeagueStandingCommand(), chatId);
                    break;
                case Commands.LEAGUE_STANDING_JERUSALEM:
                    executePhoto(getLeagueStandingJerusalemCommand(), chatId);
                    break;
                case Commands.LEAGUE_STANDING_SAINT_PETERSBURG:
                    executePhoto(getLeagueStandingSaintPetersburgCommand(), chatId);
                    break;
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void executeMessage(Command<SendMessage> command, long chatId) throws TelegramApiException {
        SendMessage messageResult = command.getResult();
        messageResult.setChatId(chatId);
        execute(messageResult);
    }

    private void executePhoto(Command<SendPhoto> command, long chatId) throws TelegramApiException {
        SendPhoto photoResult = command.getResult();
        photoResult.setChatId(chatId);
        boolean isNewPhoto = isPhotoIdURL(photoResult.getPhoto().getAttachName());
        if (isNewPhoto) {
            Message file = execute(photoResult);
            photoResult.setPhoto(file.getPhoto().get(0).getFileId());
        } else {
            execute(photoResult);
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

    public void setName(String name) {
        this.name = name;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getBotUsername() {
        return name;
    }

    public String getBotToken() {
        return token;
    }

    protected abstract Command<SendMessage> getZenitCommand();

    protected abstract Command<SendMessage> getZenitJerusalemCommand();

    protected abstract Command<SendMessage> getZenitSaintPetersburgCommand();

    protected abstract Command<SendMessage> getLeagueStandingCommand();

    protected abstract Command<SendPhoto> getLeagueStandingJerusalemCommand();

    protected abstract Command<SendPhoto> getLeagueStandingSaintPetersburgCommand();

}
