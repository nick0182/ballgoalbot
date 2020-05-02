package com.nikolay.bot.ballgoal.configuration;

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
    public GenericTransformer<Object, Fixture> mockApiTransformer() {
        return new MockApiTransformer();
    }

    @Bean
    public GenericTransformer<Object, Fixture> fetchFreshZenitFixture(
            GenericTransformer<Object, Fixture> mockApiTransformer) {
        return mockApiTransformer;
    }

    @Bean
    public GenericTransformer<Object, Fixture> fetchLastMatchDayFixture(
            GenericTransformer<Object, Fixture> mockApiTransformer) {
        return mockApiTransformer;
    }
}
