package com.nikolay.bot.ballgoal.api.impl;

import com.nikolay.bot.ballgoal.api.ApiRequest;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class ApiRequestFootball implements ApiRequest {

    private String host;

    private String key;

    @Override
    public String call(String resource) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        URL url = new URL("https", host, resource);
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

    public void setHost(String host) {
        this.host = host;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
