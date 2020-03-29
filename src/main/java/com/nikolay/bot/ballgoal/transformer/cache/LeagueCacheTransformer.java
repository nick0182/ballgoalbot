package com.nikolay.bot.ballgoal.transformer.cache;

import com.nikolay.bot.ballgoal.json.table.ResultTable;
import org.springframework.integration.transformer.GenericTransformer;

public class LeagueCacheTransformer implements GenericTransformer<ResultTable, String> {

    @Override
    public String transform(ResultTable table) {
        return table.getUrl();
    }
}
