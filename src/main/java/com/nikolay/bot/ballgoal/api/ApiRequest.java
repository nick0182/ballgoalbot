package com.nikolay.bot.ballgoal.api;

import java.io.IOException;
import java.time.ZoneId;

public interface ApiRequest {

    String call(String resource) throws IOException;

}
