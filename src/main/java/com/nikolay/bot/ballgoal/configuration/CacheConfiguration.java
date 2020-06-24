package com.nikolay.bot.ballgoal.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikolay.bot.ballgoal.api.ApiRequest;
import com.nikolay.bot.ballgoal.api.ApiResolver;
import com.nikolay.bot.ballgoal.api.impl.ApiRequestFixture;
import com.nikolay.bot.ballgoal.api.impl.FixtureApiResolver;
import com.nikolay.bot.ballgoal.cache.Cache;
import com.nikolay.bot.ballgoal.cache.CacheHandler;
import com.nikolay.bot.ballgoal.cache.TriggerCacheHandler;
import com.nikolay.bot.ballgoal.cache.ZenitTimeCache;
import com.nikolay.bot.ballgoal.cache.trigger.BlockingTrigger;
import com.nikolay.bot.ballgoal.cache.trigger.TriggerDurationTransformer;
import com.nikolay.bot.ballgoal.cache.trigger.barrier.Barrier;
import com.nikolay.bot.ballgoal.cache.trigger.barrier.impl.TriggerBarrier;
import com.nikolay.bot.ballgoal.cache.utils.ZenitPairCache;
import com.nikolay.bot.ballgoal.json.fixture.Fixture;
import com.nikolay.bot.ballgoal.json.fixture.ResultFixture;
import com.nikolay.bot.ballgoal.transformer.api.impl.LastMatchDayFixtureTransformer;
import com.nikolay.bot.ballgoal.transformer.api.impl.TableTransformer;
import com.nikolay.bot.ballgoal.transformer.api.impl.ZenitFixtureTransformer;
import com.nikolay.bot.ballgoal.transformer.cache.ZenitCacheTransformer;
import com.nikolay.bot.ballgoal.transformer.cache.ZenitInPlayTransformer;
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
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

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
            Supplier<ZenitPairCache> zenitFlowSupplier,
            @Qualifier(value = "zenitCachePollerSpec") PollerSpec zenitCachePollerSpec,
            GenericTransformer<ZenitPairCache, Fixture> fetchFreshZenitFixture,
            GenericTransformer<Fixture, Boolean> zenitInPlayTransformer,
            MessageHandler zenitInPlayHandler,
            TimestampTransformer zenitTimestampTransformer,
            GenericTransformer<LocalDateTime, Duration> zenitTriggerDurationTransformer,
            ExecutorService cachedExecutor,
            MessageHandler zenitTriggerHandler,
            GenericTransformer<Fixture, ZenitTimeCache> zenitCacheTransformer,
            CacheHandler<ZenitTimeCache> zenitCacheHandler) {
        return IntegrationFlows
                .from(zenitFlowSupplier, spec -> spec.poller(zenitCachePollerSpec))
                .transform(fetchFreshZenitFixture)
                .publishSubscribeChannel(cachedExecutor, subFlow -> subFlow
                        .subscribe(flow -> flow
                                .transform(zenitInPlayTransformer)
                                .handle(zenitInPlayHandler))
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
            Supplier<Object> leagueFlowSupplier,
            @Qualifier(value = "leagueCachePollerSpec") PollerSpec leagueCachePollerSpec,
            GenericTransformer<Object, Fixture> fetchLastMatchDayFixture,
            TimestampTransformer leagueTimestampTransformer,
            GenericTransformer<LocalDateTime, Duration> leagueTriggerDurationTransformer,
            ExecutorService cachedExecutor,
            MessageHandler leagueTriggerHandler,
            GenericTransformer<Object, InputFile> tableTransformer,
            CacheHandler<InputFile> leagueCacheHandler) {
        return IntegrationFlows
                .from(leagueFlowSupplier, spec -> spec.poller(leagueCachePollerSpec))
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
    public Trigger zenitTrigger(Barrier zenitBarrier, Cache<Duration> zenitTriggerCache) {
        return new BlockingTrigger(zenitBarrier, zenitTriggerCache);
    }

    @Bean
    public Trigger leagueTrigger(Barrier leagueBarrier, Cache<Duration> leagueTriggerCache) {
        return new BlockingTrigger(leagueBarrier, leagueTriggerCache);
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
    public GenericTransformer<ZenitPairCache, Fixture> fetchFreshZenitFixture(
            ApiResolver<ResultFixture> apiResolver) {
        return new ZenitFixtureTransformer(apiResolver, apiResourceNextFixture, apiResourceFixturesInPlay, teamId);
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
    public GenericTransformer<Object, Fixture> fetchLastMatchDayFixture(ApiResolver<ResultFixture> apiResolver) {
        return new LastMatchDayFixtureTransformer(apiResolver, apiResourceNextLeagueFixture,
                apiResourceLeagueRoundDates, apiResourceLeagueFixturesInPlay);
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

    @Bean
    public GenericTransformer<Fixture, Boolean> zenitInPlayTransformer() {
        return new ZenitInPlayTransformer();
    }

    @Bean
    public GenericTransformer<Object, InputFile> fetchFreshTable() {
        return new TableTransformer(env.getProperty("api.image.resource"));
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
    public CacheHandler<Duration> zenitTriggerHandler(Cache<Duration> zenitTriggerCache, Barrier zenitBarrier) {
        return new TriggerCacheHandler(zenitTriggerCache, zenitBarrier);
    }

    @Bean
    public CacheHandler<Duration> leagueTriggerHandler(Cache<Duration> leagueTriggerCache, Barrier leagueBarrier) {
        return new TriggerCacheHandler(leagueTriggerCache, leagueBarrier);
    }

    @Bean
    public CacheHandler<Boolean> zenitInPlayHandler(Cache<Boolean> zenitInPlayCache) {
        return new CacheHandler<>(zenitInPlayCache);
    }

    // <-----------Cache---------------->

    @Bean
    public Cache<Duration> zenitTriggerCache() {
        return new Cache<>();
    }

    @Bean
    public Cache<Duration> leagueTriggerCache() {
        return new Cache<>();
    }

    @Bean
    public Cache<Boolean> zenitInPlayCache() {
        return new Cache<>();
    }

    // <-----------Flow suppliers---------------->

    @Bean
    public Supplier<ZenitPairCache> zenitFlowSupplier(Cache<Boolean> zenitInPlayCache,
                                                      Cache<Duration> zenitTriggerCache) {
        boolean zenitInPlay = Optional.ofNullable(zenitInPlayCache.getCache()).orElse(false);
        Duration zenitTrigger = Optional.ofNullable(zenitTriggerCache.getCache()).orElse(Duration.ofHours(1));
        return () -> new ZenitPairCache(zenitInPlay, zenitTrigger);
    }

    @Bean
    public Supplier<Object> leagueFlowSupplier() {
        return Object::new;
    }

    // <-----------Barriers---------------->

    @Bean
    public Barrier zenitBarrier() {
        return new TriggerBarrier();
    }

    @Bean
    public Barrier leagueBarrier() {
        return new TriggerBarrier();
    }

    // <-----------Api---------------->

    @Bean
    public ApiRequest apiRequestFixture(Environment env) {
        return new ApiRequestFixture(env);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    @Bean
    public ApiResolver<ResultFixture> fixtureApiResolver(ApiRequest apiRequestFixture, ObjectMapper objectMapper) {
        return new FixtureApiResolver(apiRequestFixture, objectMapper);
    }
}
