package com.nikolay.bot.ballgoal.configuration;

import com.nikolay.bot.ballgoal.cache.utils.ZenitPairCache;
import com.nikolay.bot.ballgoal.json.fixture.Fixture;
import com.nikolay.bot.ballgoal.transformer.api.mock.MockApiTransformer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.transformer.GenericTransformer;

@Configuration
@Profile(value = "manual-test")
public class ManualTestProfileConfiguration {

    @Bean
    public GenericTransformer<ZenitPairCache, Fixture> fetchFreshZenitFixture() {
        return new MockApiTransformer<>();
    }

    @Bean
    public GenericTransformer<Object, Fixture> fetchLastMatchDayFixture() {
        return new MockApiTransformer<>();
    }
}
