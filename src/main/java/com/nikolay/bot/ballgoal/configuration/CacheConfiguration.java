package com.nikolay.bot.ballgoal.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikolay.bot.ballgoal.api.ApiRequest;
import com.nikolay.bot.ballgoal.api.impl.ApiRequestFixture;
import com.nikolay.bot.ballgoal.cache.Cache;
import com.nikolay.bot.ballgoal.cache.CacheHandler;
import com.nikolay.bot.ballgoal.cache.ZenitTimeCache;
import com.nikolay.bot.ballgoal.cache.trigger.BlockingTrigger;
import com.nikolay.bot.ballgoal.cache.trigger.TriggerDurationTransformer;
import com.nikolay.bot.ballgoal.json.fixture.Fixture;
import com.nikolay.bot.ballgoal.transformer.api.impl.LastMatchDayFixtureTransformer;
import com.nikolay.bot.ballgoal.transformer.api.impl.TableTransformer;
import com.nikolay.bot.ballgoal.transformer.api.impl.ZenitFixtureTransformer;
import com.nikolay.bot.ballgoal.transformer.cache.ZenitCacheTransformer;
import com.nikolay.bot.ballgoal.transformer.timestamp.TimestampTransformer;
import com.nikolay.bot.ballgoal.transformer.timestamp.impl.LeagueTimestampTransformer;
import com.nikolay.bot.ballgoal.transformer.timestamp.impl.ZenitTimestampTransformer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.PollerSpec;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.messaging.MessageHandler;
import org.springframework.scheduling.Trigger;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class CacheConfiguration {

    @Value("${resource.zenit.apiResourceNextFixture}")
    private String apiResourceNextFixture;

    @Value("${resource.zenit.apiResourceFixturesInPlay}")
    private String apiResourceFixturesInPlay;

    @Value("${resource.zenit.teamId}")
    private int teamId;

    @Value("${resource.league.apiResourceNextLeagueFixture}")
    private String apiResourceNextLeagueFixture;

    @Value("${resource.league.apiResourceLeagueRoundDates}")
    private String apiResourceLeagueRoundDates;

    @Value("${resource.league.apiResourceLeagueFixturesInPlay}")
    private String apiResourceLeagueFixturesInPlay;

    private final Environment env;

    public CacheConfiguration(Environment env) {
        this.env = env;
    }

    // <-----------Cache flows---------------->

    @Bean
    public IntegrationFlow zenitFlow(
            @Qualifier(value = "zenitCachePollerSpec") PollerSpec zenitCachePollerSpec,
            GenericTransformer<Object, Fixture> fetchFreshZenitFixture,
            TimestampTransformer zenitTimestampTransformer,
            GenericTransformer<LocalDateTime, Duration> zenitTriggerDurationTransformer,
            ExecutorService cachedExecutor,
            @Qualifier(value = "zenitTriggerHandler") MessageHandler zenitTriggerHandler,
            GenericTransformer<Fixture, ZenitTimeCache> zenitCacheTransformer,
            CacheHandler<ZenitTimeCache> zenitCacheHandler) {
        return IntegrationFlows
                .from(Object::new, spec -> spec.poller(zenitCachePollerSpec))
                .transform(fetchFreshZenitFixture)
                .publishSubscribeChannel(cachedExecutor, subFlow -> subFlow
                        .subscribe(flow -> flow
                                .transform(zenitTimestampTransformer)
                                .transform(zenitTriggerDurationTransformer)
                                .handle(zenitTriggerHandler))
                        .subscribe(flow -> flow
                                .transform(zenitCacheTransformer)
                                .handle(zenitCacheHandler)))
                .get();
    }

    @Bean
    public IntegrationFlow leagueFlow(
            @Qualifier(value = "leagueCachePollerSpec") PollerSpec leagueCachePollerSpec,
            GenericTransformer<Object, Fixture> fetchLastMatchDayFixture,
            TimestampTransformer leagueTimestampTransformer,
            GenericTransformer<LocalDateTime, Duration> leagueTriggerDurationTransformer,
            ExecutorService cachedExecutor,
            @Qualifier(value = "leagueTriggerHandler") MessageHandler leagueTriggerHandler,
            GenericTransformer<Object, InputFile> tableTransformer,
            CacheHandler<InputFile> leagueCacheHandler) {
        return IntegrationFlows
                .from(Object::new, spec -> spec.poller(leagueCachePollerSpec))
                .publishSubscribeChannel(cachedExecutor, subFlow -> subFlow
                        .subscribe(flow -> flow
                                .transform(fetchLastMatchDayFixture)
                                .transform(leagueTimestampTransformer)
                                .transform(leagueTriggerDurationTransformer)
                                .handle(leagueTriggerHandler))
                        .subscribe(flow -> flow
                                .transform(tableTransformer)
                                .handle(leagueCacheHandler)))
                .get();
    }

    // <-----------Triggers---------------->

    @Bean
    public Trigger zenitTrigger() {
        return new BlockingTrigger();
    }

    @Bean
    public Trigger leagueTrigger() {
        return new BlockingTrigger();
    }

    // <-----------Pollers specs---------------->

    @Bean
    public PollerSpec zenitCachePollerSpec(Trigger zenitTrigger) {
        return Pollers.trigger(zenitTrigger);
    }

    @Bean
    public PollerSpec leagueCachePollerSpec(Trigger leagueTrigger) {
        return Pollers.trigger(leagueTrigger);
    }

    // <-----------Thread pools---------------->

    @Bean
    public ExecutorService cachedExecutor() {
        return Executors.newCachedThreadPool();
    }

    // <-----------Transformers---------------->

    @Bean
    @Profile(value = "!manual-test")
    public GenericTransformer<Object, Fixture> fetchFreshZenitFixture(ApiRequest apiRequestFixture,
                                                                      ObjectMapper objectMapper) {
        return new ZenitFixtureTransformer(apiRequestFixture, objectMapper,
                apiResourceNextFixture, apiResourceFixturesInPlay, teamId);
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
    @Profile(value = "!manual-test")
    public GenericTransformer<Object, Fixture> fetchLastMatchDayFixture(ApiRequest apiRequestFixture,
                                                                        ObjectMapper objectMapper) {
        return new LastMatchDayFixtureTransformer(apiRequestFixture, objectMapper,
                apiResourceNextLeagueFixture, apiResourceLeagueRoundDates, apiResourceLeagueFixturesInPlay);
    }

    @Bean
    public TimestampTransformer leagueTimestampTransformer() {
        return new LeagueTimestampTransformer();
    }

    @Bean
    public GenericTransformer<LocalDateTime, Duration> zenitTriggerDurationTransformer() {
        return new TriggerDurationTransformer();
    }

    @Bean
    public GenericTransformer<LocalDateTime, Duration> leagueTriggerDurationTransformer() {
        return new TriggerDurationTransformer();
    }

    // <-----------Handlers---------------->

    @Bean
    public CacheHandler<ZenitTimeCache> zenitCacheHandler(Cache<ZenitTimeCache> zenitCache) {
        return new CacheHandler<>(zenitCache);
    }

    @Bean
    public CacheHandler<InputFile> leagueCacheHandler(Cache<InputFile> leagueCache) {
        return new CacheHandler<>(leagueCache);
    }

    @Bean
    public MessageHandler zenitTriggerHandler(Trigger zenitTrigger) {
        return (MessageHandler) zenitTrigger;
    }

    @Bean
    public MessageHandler leagueTriggerHandler(Trigger leagueTrigger) {
        return (MessageHandler) leagueTrigger;
    }

    @Bean
    public GenericTransformer<Object, InputFile> fetchFreshTable() {
        return new TableTransformer(env.getProperty("api.image.resource"));
    }

    // <-----------Apis---------------->

    @Bean
    public ApiRequest apiRequestFixture(Environment env) {
        return new ApiRequestFixture(env);
    }

    // <-----------Other---------------->

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }
}
