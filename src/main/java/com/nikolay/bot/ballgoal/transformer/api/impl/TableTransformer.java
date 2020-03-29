package com.nikolay.bot.ballgoal.transformer.api.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikolay.bot.ballgoal.api.ApiRequest;
import com.nikolay.bot.ballgoal.json.table.ResultTable;
import com.nikolay.bot.ballgoal.properties.ResourceProperties;
import com.nikolay.bot.ballgoal.transformer.api.ApiTransformer;

import java.io.IOException;

public class TableTransformer extends ApiTransformer<ResultTable> {

    public TableTransformer(ResourceProperties resourceProperties,
                            ApiRequest apiRequest, ObjectMapper objectMapper) {
        super(resourceProperties, apiRequest, objectMapper);
    }

    @Override
    public ResultTable transform(Object source) {
        try {
            return callApi(resourceProperties.getResourceHtmlToImage(), ResultTable.class);
        } catch (IOException e) {
            throw wrapToRuntime(e);
        }
    }
}
