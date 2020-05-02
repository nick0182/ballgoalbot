package com.nikolay.bot.ballgoal.transformer.api.impl;

import gui.ava.html.Html2Image;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.transformer.GenericTransformer;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

@Slf4j
public class TableTransformer implements GenericTransformer<Object, InputFile> {

    private static final int TABLE_IMAGE_SIZE = 40_000;

    private final String imageResource;

    public TableTransformer(String imageResource) {
        this.imageResource = imageResource;
    }

    @Override
    public InputFile transform(Object o) {
        Html2Image html2Image = new Html2Image();
        try {
            html2Image.getParser().load(new URL(imageResource));
            ByteArrayOutputStream ous = new ByteArrayOutputStream(TABLE_IMAGE_SIZE);
            html2Image.getImageRenderer().saveImage(ous, false);
            return new InputFile(new ByteArrayInputStream(ous.toByteArray()), "new table");
        } catch (IOException e) {
            log.error("error fetching result from api", e);
            throw new RuntimeException("Cache was not set");
        }
    }
}
