package com.nikolay.bot.ballgoal.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikolay.bot.ballgoal.api.ApiRequest;
import com.nikolay.bot.ballgoal.api.impl.ApiRequestFixture;
import com.nikolay.bot.ballgoal.api.impl.ApiRequestImage;
import com.nikolay.bot.ballgoal.bot.BallGoalBot;
import com.nikolay.bot.ballgoal.cache.Cache;
import com.nikolay.bot.ballgoal.cache.CacheHandler;
import com.nikolay.bot.ballgoal.cache.ZenitTimeCache;
import com.nikolay.bot.ballgoal.cache.timestamp.Timestamp;
import com.nikolay.bot.ballgoal.cache.timestamp.TimestampHandler;
import com.nikolay.bot.ballgoal.cache.updater.CacheUpdater;
import com.nikolay.bot.ballgoal.cache.updater.LeagueCacheUpdater;
import com.nikolay.bot.ballgoal.cache.utils.CacheOffsetAppender;
import com.nikolay.bot.ballgoal.gateway.CacheGateway;
import com.nikolay.bot.ballgoal.json.fixture.Fixture;
import com.nikolay.bot.ballgoal.json.table.ResultTable;
import com.nikolay.bot.ballgoal.properties.ResourceProperties;
import com.nikolay.bot.ballgoal.transformer.api.ApiTransformer;
import com.nikolay.bot.ballgoal.transformer.api.impl.LastMatchDayFixtureTransformer;
import com.nikolay.bot.ballgoal.transformer.api.impl.TableTransformer;
import com.nikolay.bot.ballgoal.transformer.api.impl.ZenitFixtureTransformer;
import com.nikolay.bot.ballgoal.transformer.cache.LeagueCacheTransformer;
import com.nikolay.bot.ballgoal.transformer.cache.ZenitCacheTransformer;
import com.nikolay.bot.ballgoal.transformer.timestamp.TimestampTransformer;
import com.nikolay.bot.ballgoal.transformer.timestamp.impl.LeagueTimestampTransformer;
import com.nikolay.bot.ballgoal.transformer.timestamp.impl.ZenitTimestampTransformer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.gateway.GatewayProxyFactoryBean;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.messaging.MessageChannel;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.Executors;

@SpringBootApplication
public class BallGoalBotApplication {

    @Value("${text.info}")
    private String textInfo;

    @Value("${text.timezone}")
    private String textTimezone;

    @Value("${api.image.resource}")
    private String imageResource;

    @Value("${offset.saint-petersburg}")
    private String saintPetersburg;

    @Value("${offset.jerusalem}")
    private String jerusalem;

    @Value("${command.info}")
    private String commandInfo;

    @Value("${command.zenit}")
    private String commandZenit;

    @Value("${command.timezone.jerusalem}")
    private String commandZenitJerusalem;

    @Value("${command.timezone.saintPetersburg}")
    private String commandZenitSaintPetersburg;

    @Value("${command.league}")
    private String commandLeague;

    private final Environment env;

    public BallGoalBotApplication(Environment env) {
        this.env = env;
    }

    public static void main(String[] args) {
        SpringApplication.run(BallGoalBotApplication.class, args);
    }

    @Bean
    public InitializingBean initTelegram() {
        return ApiContextInitializer::init;
    }

    @Bean
    @DependsOn(value = "initTelegram")
    public void registerBot() throws TelegramApiRequestException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        telegramBotsApi.registerBot(ballGoalBot());
    }

    @Bean
    public TelegramLongPollingBot ballGoalBot() {
        return new BallGoalBot(env, zenitCacheUpdater(), leagueCacheUpdater()) {
            @Override
            protected String getCommandInfo() {
                return commandInfo;
            }

            @Override
            protected String getCommandZenit() {
                return commandZenit;
            }

            @Override
            protected String getCommandZenitJerusalem() {
                return commandZenitJerusalem;
            }

            @Override
            protected String getCommandZenitSaintPetersburg() {
                return commandZenitSaintPetersburg;
            }

            @Override
            protected String getCommandLeague() {
                return commandLeague;
            }

            @Override
            protected SendMessage getInfoMessage() {
                return infoMessage();
            }

            @Override
            protected SendMessage getZenitMessage() {
                return zenitMessage();
            }

            @Override
            protected SendMessage getJerusalemMessage() {
                return jerusalemMessage();
            }

            @Override
            protected SendMessage getSaintPetersburgMessage() {
                return saintPetersburgMessage();
            }

            @Override
            protected SendPhoto getLeaguePhoto() {
                return leaguePhoto();
            }

            @Override
            protected SendMessage getTryAgainMessage() {
                return tryAgainMessage();
            }
        };
    }

    // <-----------Gateways---------------->

    @Bean
    public GatewayProxyFactoryBean zenitCacheGateway() {
        GatewayProxyFactoryBean gateway = new GatewayProxyFactoryBean(CacheGateway.class);
        gateway.setDefaultRequestChannel(zenitExecutorChannel());
        return gateway;
    }

    @Bean
    public GatewayProxyFactoryBean leagueCacheGateway() {
        GatewayProxyFactoryBean gateway = new GatewayProxyFactoryBean(CacheGateway.class);
        gateway.setDefaultRequestChannel(leagueExecutorChannel());
        return gateway;
    }

    // <-----------Responses---------------->

    @Bean
    @Scope("prototype")
    public SendMessage infoMessage() {
        SendMessage message = new SendMessage();
        message.setReplyMarkup(removeKeyboard());
        message.setText(textInfo);
        return message;
    }

    @Bean
    @Scope("prototype")
    public SendMessage zenitMessage() {
        SendMessage message = new SendMessage();
        message.setReplyMarkup(timezoneKeyboard());
        message.setText(textTimezone);
        return message;
    }

    @Bean
    @Scope("prototype")
    public SendMessage jerusalemMessage() {
        SendMessage message = new SendMessage();
        message.setReplyMarkup(removeKeyboard());
        message.setText(getOffsetCacheText(ZoneOffset.of(jerusalem)));
        return message;
    }

    @Bean
    @Scope("prototype")
    public SendMessage saintPetersburgMessage() {
        SendMessage message = new SendMessage();
        message.setReplyMarkup(removeKeyboard());
        message.setText(getOffsetCacheText(ZoneOffset.of(saintPetersburg)));
        return message;
    }

    private String getOffsetCacheText(ZoneOffset offset) {
        ZenitTimeCache zenitTimeCache = zenitCache().getCache();
        if (zenitTimeCache != null) {
            String cache = zenitTimeCache.getCache();
            LocalDateTime dateTime = zenitTimeCache.getDateTime();
            return cache + CacheOffsetAppender.appendOffset(dateTime, offset);
        } else {
            return null;
        }
    }

    @Bean
    @Scope("prototype")
    public SendPhoto leaguePhoto() {
        SendPhoto photo = new SendPhoto();
        photo.setReplyMarkup(removeKeyboard());
        photo.setPhoto(leagueCache().getCache());
        return photo;
    }

    @Bean
    @Scope("prototype")
    public SendMessage tryAgainMessage() {
        SendMessage message = new SendMessage();
        message.setReplyMarkup(removeKeyboard());
        message.setText("try again");
        return message;
    }

    // <-----------Flows---------------->

    @Bean
    public IntegrationFlow zenitCacheFlow() {
        return IntegrationFlows
                .from(zenitExecutorChannel())
                .transform(fetchFreshZenitFixture())
                .publishSubscribeChannel(Executors.newCachedThreadPool(), subFlow -> subFlow
                        .subscribe(flow -> flow
                                .transform(zenitTimestampTransformer())
                                .handle(zenitTimestampHandler()))
                        .subscribe(flow -> flow
                                .transform(zenitCacheTransformer())
                                .handle(zenitCacheHandler())))
                .get();
    }

    @Bean
    public IntegrationFlow leagueCacheFlow() {
        return IntegrationFlows
                .from(leagueExecutorChannel())
                .publishSubscribeChannel(Executors.newCachedThreadPool(), subFlow -> subFlow
                        .subscribe(flow -> flow
                                .transform(fetchLastMatchDayFixture())
                                .transform(leagueTimestampTransformer())
                                .handle(leagueTimestampHandler()))
                        .subscribe(flow -> flow
                                .transform(fetchFreshTable())
                                .transform(leagueCacheTransformer())
                                .handle(leagueCacheHandler())))
                .get();
    }

    // <-----------Channels---------------->

    @Bean
    public MessageChannel zenitExecutorChannel() {
        return new ExecutorChannel(Executors.newSingleThreadExecutor());
    }

    @Bean
    public MessageChannel leagueExecutorChannel() {
        return new ExecutorChannel(Executors.newSingleThreadExecutor());
    }

    // <-----------Transformers---------------->

    @Bean
    public ApiTransformer<Fixture> fetchFreshZenitFixture() {
        return new ZenitFixtureTransformer(resourceProperties(), apiRequestFixture(), objectMapper());
    }

    @Bean
    public TimestampTransformer zenitTimestampTransformer() {
        return new ZenitTimestampTransformer();
    }

    @Bean
    public GenericTransformer<Fixture, ZenitTimeCache> zenitCacheTransformer() {
        return new ZenitCacheTransformer();
    }

    @Bean
    public ApiTransformer<Fixture> fetchLastMatchDayFixture() {
        return new LastMatchDayFixtureTransformer(resourceProperties(), apiRequestFixture(), objectMapper());
    }

    @Bean
    public TimestampTransformer leagueTimestampTransformer() {
        return new LeagueTimestampTransformer();
    }

    @Bean
    public ApiTransformer<ResultTable> fetchFreshTable() {
        return new TableTransformer(resourceProperties(), apiRequestImage(), objectMapper());
    }

    @Bean
    public GenericTransformer<ResultTable, String> leagueCacheTransformer() {
        return new LeagueCacheTransformer();
    }

    // <-----------Handlers---------------->

    @Bean
    public TimestampHandler zenitTimestampHandler() {
        return new TimestampHandler(zenitTimestamp());
    }

    @Bean
    public CacheHandler<ZenitTimeCache> zenitCacheHandler() {
        return new CacheHandler<>(zenitCache());
    }

    @Bean
    public TimestampHandler leagueTimestampHandler() {
        return new TimestampHandler(leagueTimestamp());
    }

    @Bean
    public CacheHandler<String> leagueCacheHandler() {
        return new CacheHandler<>(leagueCache());
    }


    // <-----------Cache---------------->

    @Bean
    public Timestamp zenitTimestamp() {
        return new Timestamp();
    }

    @Bean
    public Cache<ZenitTimeCache> zenitCache() {
        return new Cache<>();
    }

    @Bean
    public Timestamp leagueTimestamp() {
        return new Timestamp();
    }

    @Bean
    public Cache<String> leagueCache() {
        return new Cache<>();
    }

    // <-----------Cache updaters---------------->

    @Bean
    public CacheUpdater zenitCacheUpdater() {
        return new CacheUpdater((CacheGateway) Objects.requireNonNull(zenitCacheGateway().getObject()),
                zenitTimestamp());
    }

    @Bean
    public LeagueCacheUpdater leagueCacheUpdater() {
        return new LeagueCacheUpdater((CacheGateway) Objects.requireNonNull(leagueCacheGateway().getObject()),
                leagueTimestamp(), leagueCache());
    }

    // <-----------Keyboards---------------->

    @Bean
    @Lazy
    public ReplyKeyboard timezoneKeyboard() {
        ReplyKeyboardMarkup timezoneKeyboard = new ReplyKeyboardMarkup();
        timezoneKeyboard.setOneTimeKeyboard(true);
        timezoneKeyboard.setSelective(true);
        timezoneKeyboard.setResizeKeyboard(true);
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(new KeyboardButton(commandZenitJerusalem));
        keyboardRow.add(new KeyboardButton(commandZenitSaintPetersburg));
        timezoneKeyboard.setKeyboard(Collections.singletonList(keyboardRow));
        return timezoneKeyboard;
    }

    @Bean
    @Lazy
    public ReplyKeyboard removeKeyboard() {
        return new ReplyKeyboardRemove();
    }

    // <-----------Apis---------------->

    @Bean
    public ApiRequest apiRequestFixture() {
        return new ApiRequestFixture(env);
    }

    @Bean
    public ApiRequest apiRequestImage() {
        return new ApiRequestImage(env, imageResource);
    }

    // <-----------Other---------------->

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    @Bean
    @ConfigurationProperties(prefix = "resource")
    public ResourceProperties resourceProperties() {
        return new ResourceProperties();
    }
}
