package com.nikolay.bot.ballgoal.api.impl;

import com.nikolay.bot.ballgoal.api.ApiRequest;
import okhttp3.*;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class ApiRequestImage implements ApiRequest {

    private final Environment env;

    private final String imageResource;

    public ApiRequestImage(Environment env, String imageResource) {
        this.env = env;
        this.imageResource = imageResource;
    }

    @Override
    public String call(String resource) throws IOException {
        String host = Objects.requireNonNull(env.getProperty("API_IMAGE_HOST"));
        String user = Objects.requireNonNull(env.getProperty("API_IMAGE_USER"));
        String key = Objects.requireNonNull(env.getProperty("API_IMAGE_KEY"));

        OkHttpClient client = new OkHttpClient();
        URL url = new URL("https", host, resource);
        String credential = Credentials.basic(user, key);
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("html", "<h1>table_image</>")
                .addFormDataPart("url", imageResource)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "text/html")
                .addHeader("Authorization", credential)
                .build();
        Response response = client.newCall(request).execute();
        int statusCode = response.code();
        if (statusCode == 200) {
            return Objects.requireNonNull(response.body()).string();
        } else {
            throw new IllegalArgumentException("resource not found");
        }
    }
}
