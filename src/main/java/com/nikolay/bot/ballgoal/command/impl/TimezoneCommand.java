package com.nikolay.bot.ballgoal.command.impl;

import com.nikolay.bot.ballgoal.command.Command;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

public class TimezoneCommand implements Command<SendMessage> {

    private ReplyKeyboard keyboard;

    public TimezoneCommand(ReplyKeyboard keyboard) {
        this.keyboard = keyboard;
    }

    @Override
    public SendMessage getResult() {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Choose your timezone");
        sendMessage.setReplyMarkup(keyboard);
        return sendMessage;
    }
}
