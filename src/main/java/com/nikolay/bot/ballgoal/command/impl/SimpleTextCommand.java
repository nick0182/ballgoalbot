package com.nikolay.bot.ballgoal.command.impl;

import com.nikolay.bot.ballgoal.command.Command;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

public class SimpleTextCommand implements Command<SendMessage> {

    private String text;

    private ReplyKeyboard keyboard;

    public SimpleTextCommand(ReplyKeyboard keyboard) {
        this.keyboard = keyboard;
    }

    @Override
    public SendMessage getResult() {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(keyboard);
        return sendMessage;
    }

    public void setText(String text) {
        this.text = text;
    }
}
