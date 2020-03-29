package com.nikolay.bot.ballgoal.api.impl;

import com.nikolay.bot.ballgoal.api.ApiRequest;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.util.Objects;

public class ApiRequestFixture implements ApiRequest {

    private final Environment env;

    public ApiRequestFixture(Environment env) {
        this.env = env;
    }

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
        Response response = okHttpClient.newCall(request).execute();
        int statusCode = response.code();
        if (statusCode == 200) {
            return Objects.requireNonNull(response.body()).string();
        } else {
            throw new IllegalArgumentException("resource not found");
        }
    }
}
