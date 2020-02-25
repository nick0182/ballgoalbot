package com.nikolay.bot.ballgoal.command;

import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface Command<T extends PartialBotApiMethod<Message>> {

    T getResult();

}
