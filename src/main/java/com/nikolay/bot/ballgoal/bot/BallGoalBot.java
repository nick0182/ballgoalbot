package com.nikolay.bot.ballgoal.bot;

import com.nikolay.bot.ballgoal.constants.Commands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Objects;

@RequiredArgsConstructor
@Slf4j
public abstract class BallGoalBot extends TelegramLongPollingBot {

    private final Environment env;

    private final SendMessage infoMessage;

    private final SendMessage zenitMessage;

    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        String command = message.getText();
        long chatId = message.getChatId();
        try {
            switch (command) {
                case Commands.INFO:
                    executeResult(infoMessage, chatId);
                    break;
                case Commands.ZENIT:
                    executeResult(zenitMessage, chatId);
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
            log.error("Error executing telegram message: {}", e.getMessage());
        }
    }

    private void executeResult(SendMessage message, long chatId) throws TelegramApiException {
        message.setChatId(chatId);
        execute(message);
    }

    private void executeResult(SendPhoto photoMessage, long chatId) throws TelegramApiException {
        photoMessage.setChatId(chatId);
        InputFile photoFile = photoMessage.getPhoto();
        Message file = execute(photoMessage);
        if (photoFile.isNew()) {
            photoFile.setMedia(file.getPhoto().get(0).getFileId());
        }
    }

    public String getBotUsername() {
        return Objects.requireNonNull(env.getProperty("BOT_NAME"));
    }

    public String getBotToken() {
        return Objects.requireNonNull(env.getProperty("BOT_TOKEN"));
    }

    protected abstract SendMessage getJerusalemMessage();

    protected abstract SendMessage getSaintPetersburgMessage();

    protected abstract SendPhoto getLeaguePhoto();
}
