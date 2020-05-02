package com.nikolay.bot.ballgoal.api.impl;

import com.nikolay.bot.ballgoal.api.ApiRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class ApiRequestFixture implements ApiRequest {

    private final Environment env;

    @Override
    public String call(String resource) throws IOException {
        String host = Objects.requireNonNull(env.getProperty("API_FIXTURE_HOST"));
        String key = Objects.requireNonNull(env.getProperty("API_FIXTURE_KEY"));

        OkHttpClient okHttpClient = new OkHttpClient();
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(host)
                .addPathSegments(resource)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("x-rapidapi-host", host)
                .addHeader("x-rapidapi-key", key)
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            log.debug("Api fixture response code: {}", response.code());
            return Objects.requireNonNull(response.body()).string();
        }
    }
}
