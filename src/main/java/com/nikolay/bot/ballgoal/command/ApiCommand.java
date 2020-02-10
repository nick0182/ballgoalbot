package com.nikolay.bot.ballgoal.command;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface ApiCommand {

    SendMessage generateMessage(String resource);

}
