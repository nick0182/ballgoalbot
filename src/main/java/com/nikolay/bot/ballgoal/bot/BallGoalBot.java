package com.nikolay.bot.ballgoal.bot;

import com.nikolay.bot.ballgoal.command.ApiCommand;
import com.nikolay.bot.ballgoal.command.TextCommand;
import com.nikolay.bot.ballgoal.constants.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public abstract class BallGoalBot extends TelegramLongPollingBot {

    private String name;

    private String token;

    private String apiResourceTimezoneJerusalem;

    private String apiResourceTimezoneMoscow;

    private static final Logger LOG = LoggerFactory.getLogger(BallGoalBot.class);

    //TODO: test it only with Spring context (Integration test)
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        String command = message.getText();
        LOG.info("Command received: {}", command);
        long chatId = message.getChatId();
        SendMessage sendMessage;
        switch (command) {
            case Command.ZENIT:
                sendMessage = getZenitCommand().generateMessage();
                break;
            case Command.TIMEZONE_JERUSALEM:
                sendMessage = getZenitTimezoneCommand().generateMessage(apiResourceTimezoneJerusalem);
                break;
            case Command.TIMEZONE_SAINT_PETERSBURG:
                sendMessage = getZenitTimezoneCommand().generateMessage(apiResourceTimezoneMoscow);
                break;
            default:
                return;
        }
        sendMessage.setChatId(chatId);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setApiResourceTimezoneJerusalem(String apiResourceTimezoneJerusalem) {
        this.apiResourceTimezoneJerusalem = apiResourceTimezoneJerusalem;
    }

    public void setApiResourceTimezoneMoscow(String apiResourceTimezoneMoscow) {
        this.apiResourceTimezoneMoscow = apiResourceTimezoneMoscow;
    }

    public String getBotUsername() {
        return name;
    }

    public String getBotToken() {
        return token;
    }

    protected abstract TextCommand getZenitCommand();

    protected abstract ApiCommand getZenitTimezoneCommand();

}
