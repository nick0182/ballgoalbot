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

    private String botName;

    private String botToken;

    private String timezoneJerusalemResource;

    private String timezoneMoscowResource;

    private static final Logger LOG = LoggerFactory.getLogger(BallGoalBot.class);

    public BallGoalBot(String botName, String botToken,
                       String timezoneJerusalemResource, String timezoneMoscowResource) {
        this.botName = botName;
        this.botToken = botToken;
        this.timezoneJerusalemResource = timezoneJerusalemResource;
        this.timezoneMoscowResource = timezoneMoscowResource;
    }

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
                sendMessage = getZenitTimezoneCommand().generateMessage(timezoneJerusalemResource);
                break;
            case Command.TIMEZONE_SAINT_PETERSBURG:
                sendMessage = getZenitTimezoneCommand().generateMessage(timezoneMoscowResource);
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

    public String getBotUsername() {
        return botName;
    }

    public String getBotToken() {
        return botToken;
    }

    protected abstract TextCommand getZenitCommand();

    protected abstract ApiCommand getZenitTimezoneCommand();

}
