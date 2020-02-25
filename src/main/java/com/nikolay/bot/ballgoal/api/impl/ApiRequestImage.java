package com.nikolay.bot.ballgoal.api.impl;

import com.nikolay.bot.ballgoal.api.ApiRequest;
import okhttp3.*;

import java.io.IOException;
import java.net.URL;
import java.time.ZoneId;
import java.util.Objects;

public class ApiRequestImage implements ApiRequest {

    private String host;

    private String userId;

    private String key;

    private HttpUrl resourceTablesWidget;

    @Override
    public String call(String resource, ZoneId zoneId) throws IOException {
        resourceTablesWidget = resourceTablesWidget.newBuilder()
                .addEncodedQueryParameter("timezone", zoneId.getId())
                .build();
        OkHttpClient client = new OkHttpClient();
        URL url = new URL("https", host, resource);
        String credential = Credentials.basic(userId, key);
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("html", "<h1>table_image</>")
                .addFormDataPart("url", resourceTablesWidget.toString())
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

    public void setHost(String host) {
        this.host = host;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setResourceTablesWidget(String resourceTablesWidget) {
        this.resourceTablesWidget = HttpUrl.get(resourceTablesWidget);
    }
}
