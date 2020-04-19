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
import com.nikolay.bot.ballgoal.cache.trigger.TriggerHandler;
import com.nikolay.bot.ballgoal.cache.utils.CacheOffsetAppender;
import com.nikolay.bot.ballgoal.constants.Commands;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.PollerSpec;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.integration.util.DynamicPeriodicTrigger;
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.concurrent.Executors;

@SpringBootApplication
public class BallGoalBotApplication {

    @Value("${text.info}")
    private String textInfo;

    @Value("${text.timezone}")
    private String textTimezone;

    @Value("${offset.jerusalem}")
    private String jerusalem;

    @Value("${offset.saint-petersburg}")
    private String saintPetersburg;

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
        return new BallGoalBot(env, leagueCache()) {
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
        };
    }

    // <-----------Responses---------------->

    @Bean
    public SendMessage infoMessage() {
        SendMessage message = new SendMessage();
        message.setReplyMarkup(removeKeyboard());
        message.setText(textInfo);
        return message;
    }

    @Bean
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

    // <-----------Cache flows---------------->

    @Bean
    public IntegrationFlow zenitFlow(
            @Qualifier(value = "zenitCachePollerSpec") PollerSpec zenitCachePollerSpec,
            TriggerHandler zenitTriggerHandler) {
        return IntegrationFlows
                .from(Object::new, spec -> spec.poller(zenitCachePollerSpec))
                .transform(fetchFreshZenitFixture())
                .publishSubscribeChannel(Executors.newCachedThreadPool(), subFlow -> subFlow
                        .subscribe(flow -> flow
                                .transform(zenitTimestampTransformer())
                                .handle(zenitTriggerHandler))
                        .subscribe(flow -> flow
                                .transform(zenitCacheTransformer())
                                .handle(zenitCacheHandler())))
                .get();
    }

    @Bean
    public IntegrationFlow leagueFlow(
            @Qualifier(value = "leagueCachePollerSpec") PollerSpec leagueCachePollerSpec,
            TriggerHandler leagueTriggerHandler) {
        return IntegrationFlows
                .from(Object::new, spec -> spec.poller(leagueCachePollerSpec))
                .publishSubscribeChannel(Executors.newCachedThreadPool(), subFlow -> subFlow
                        .subscribe(flow -> flow
                                .transform(fetchLastMatchDayFixture())
                                .transform(leagueTimestampTransformer())
                                .handle(leagueTriggerHandler))
                        .subscribe(flow -> flow
                                .transform(fetchFreshTable())
                                .transform(leagueCacheTransformer())
                                .handle(leagueCacheHandler())))
                .get();
    }

    // <-----------Triggers---------------->

    /**
     * Trigger for refreshing zenit fixture cache
     *
     * @return DynamicPeriodicTrigger bean with some long delay (will be overridden after new cache is fetched)
     */
    @Bean
    public DynamicPeriodicTrigger zenitCacheTrigger() {
        return new DynamicPeriodicTrigger(Duration.of(365, ChronoUnit.DAYS));
    }

    /**
     * Trigger for refreshing league standing cache
     *
     * @return DynamicPeriodicTrigger bean with some long delay (will be overridden after new cache is fetched)
     */
    @Bean
    public DynamicPeriodicTrigger leagueCacheTrigger() {
        return new DynamicPeriodicTrigger(Duration.of(365, ChronoUnit.DAYS));
    }

    // <-----------Pollers specs---------------->

    @Bean
    public PollerSpec zenitCachePollerSpec(DynamicPeriodicTrigger zenitCacheTrigger) {
        return Pollers.trigger(zenitCacheTrigger);
    }

    @Bean
    public PollerSpec leagueCachePollerSpec(DynamicPeriodicTrigger leagueCacheTrigger) {
        return Pollers.trigger(leagueCacheTrigger);
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
    public CacheHandler<ZenitTimeCache> zenitCacheHandler() {
        return new CacheHandler<>(zenitCache());
    }

    @Bean
    public TriggerHandler zenitTriggerHandler(DynamicPeriodicTrigger zenitCacheTrigger) {
        return new TriggerHandler(zenitCacheTrigger, "zenit");
    }

    @Bean
    public CacheHandler<String> leagueCacheHandler() {
        return new CacheHandler<>(leagueCache());
    }

    @Bean
    public TriggerHandler leagueTriggerHandler(DynamicPeriodicTrigger leagueCacheTrigger) {
        return new TriggerHandler(leagueCacheTrigger, "league");
    }

    // <-----------Cache---------------->

    @Bean
    public Cache<ZenitTimeCache> zenitCache() {
        return new Cache<>();
    }

    @Bean
    public Cache<String> leagueCache() {
        return new Cache<>();
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
        keyboardRow.add(new KeyboardButton(Commands.ZENIT_JERUSALEM));
        keyboardRow.add(new KeyboardButton(Commands.ZENIT_SAINT_PETERSBURG));
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
        return new ApiRequestImage(env);
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
