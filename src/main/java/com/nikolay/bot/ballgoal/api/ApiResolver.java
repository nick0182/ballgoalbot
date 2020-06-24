package com.nikolay.bot.ballgoal.api;

import java.io.IOException;

public interface ApiResolver<T> {

    T resolve(String resource) throws IOException;
}
