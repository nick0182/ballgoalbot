package com.nikolay.bot.ballgoal.api.impl;

import com.nikolay.bot.ballgoal.api.ApiRequest;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class ApiRequestFootball implements ApiRequest {

    private final String apiHost;

    private final String apiKey;

    public ApiRequestFootball(String apiHost, String apiKey) {
        this.apiHost = apiHost;
        this.apiKey = apiKey;
    }

    @Override
    public String call(String resource) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        URL url = new URL("https", apiHost, resource);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("x-rapidapi-host", apiHost)
                .addHeader("x-rapidapi-key", apiKey)
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
