package com.aws.codestar.projecttemplates.configuration;

import bot.BallGoalBot;
import com.aws.codestar.projecttemplates.controller.HelloWorldController;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

@Configuration
@ComponentScan({ "com.aws.codestar.projecttemplates.configuration" })
@PropertySource("classpath:application.properties")
public class ApplicationConfig {

    @Value("${HelloWorld.SiteName}")
    private String siteName;

    @Value("${message.time.tbd}")
    private String messageTimeToBeDefined;

    @Value("${api.timezone.moscow}")
    private String apiTimezoneMoscow;

    @Value("${api.timezone.jerusalem}")
    private String apiTimezoneJerusalem;

    @Value("${api.zenit.id}")
    private String apiZenitId;

    @Value("${api.host}")
    private String apiHost;

    @Value("${api.key}")
    private String apiKey;

    @Value("${telegram.bot.name}")
    private String telegramBotName;

    @Value("${telegram.bot.token}")
    private String telegramBotToken;

    @Bean
    public HelloWorldController helloWorld() {
        return new HelloWorldController(this.siteName);
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeHolderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public InitializingBean initTelegram() {
        return ApiContextInitializer::init;
    }

    @Bean
    @DependsOn(value = "initTelegram")
    public TelegramBotsApi registerBot() throws TelegramApiRequestException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        telegramBotsApi.registerBot(ballGoalBot());
        return telegramBotsApi;
    }

    @Bean
    public BallGoalBot ballGoalBot() {
        return new BallGoalBot(
                messageTimeToBeDefined,
                apiTimezoneMoscow,
                apiTimezoneJerusalem,
                apiZenitId,
                apiHost,
                apiKey,
                telegramBotName,
                telegramBotToken
        );
    }

}
