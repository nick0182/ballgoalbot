package command;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface TextCommand {

    SendMessage generateMessage();

}
