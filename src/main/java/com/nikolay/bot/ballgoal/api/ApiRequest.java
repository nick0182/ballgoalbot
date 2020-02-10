package com.nikolay.bot.ballgoal.api;

import java.io.IOException;

public interface ApiRequest {

    String call(String resource) throws IOException;

}
