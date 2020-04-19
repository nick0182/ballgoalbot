package com.nikolay.bot.ballgoal.api;

import org.springframework.core.env.Environment;

import java.io.IOException;

public abstract class ApiRequest {

    protected Environment env;

    public ApiRequest(Environment env) {
        this.env = env;
    }

    public abstract String call(String resource) throws IOException;
}
