package bot;

import command.ApiCommand;
import command.TextCommand;
import constants.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public abstract class BallGoalBot extends TelegramLongPollingBot {

    private String apiTimezoneMoscow;

    private String apiTimezoneJerusalem;

    private String apiZenitId;

    private String telegramBotName;

    private String telegramBotToken;

    private static final Logger LOG = LoggerFactory.getLogger(BallGoalBot.class);

    public BallGoalBot(
            String apiTimezoneMoscow,
            String apiTimezoneJerusalem,
            String apiZenitId,
            String telegramBotName,
            String telegramBotToken) {
        this.apiTimezoneMoscow = apiTimezoneMoscow;
        this.apiTimezoneJerusalem = apiTimezoneJerusalem;
        this.apiZenitId = apiZenitId;
        this.telegramBotName = telegramBotName;
        this.telegramBotToken = telegramBotToken;
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
                sendMessage = getZenitTimezoneCommand().generateMessage("/v2/fixtures/team/"
                        + apiZenitId + "/next/1?timezone=" + apiTimezoneJerusalem);
                break;
            case Command.TIMEZONE_SAINT_PETERSBURG:
                sendMessage = getZenitTimezoneCommand().generateMessage("/v2/fixtures/team/"
                        + apiZenitId + "/next/1?timezone=" + apiTimezoneMoscow);
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
        return telegramBotName;
    }

    public String getBotToken() {
        return telegramBotToken;
    }

    protected abstract TextCommand getZenitCommand();

    protected abstract ApiCommand getZenitTimezoneCommand();

}
