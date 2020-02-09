package command.impl;

import command.TextCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

public class ZenitCommand implements TextCommand {

    private ReplyKeyboard keyboard;

    public ZenitCommand(ReplyKeyboard keyboard) {
        this.keyboard = keyboard;
    }

    @Override
    public SendMessage generateMessage() {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Choose your timezone");
        sendMessage.setReplyMarkup(keyboard);
        return sendMessage;
    }

}
