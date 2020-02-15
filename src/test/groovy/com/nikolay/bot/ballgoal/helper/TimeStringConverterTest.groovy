package com.nikolay.bot.ballgoal.helper

import spock.lang.Specification

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class TimeStringConverterTest extends Specification {

    def "get date string"() {
        given: "ISO offset formatted zoned date-time"
        def date = setUpTestDate()

        when: "get date string"
        def result = TimeStringConverter.getDateString(date)

        then: "string conforms to pattern"
        result == "Saturday 29 February 2020"
    }

    def "get time string"() {
        given: "ISO offset formatted zoned date-time"
        def date = setUpTestDate()

        when: "get time string"
        def result = TimeStringConverter.getTimeString(date)

        then: "string conforms to pattern"
        result == "18:00"
    }

    def "get zoned time string"() {
        given: "ISO offset formatted zoned date-time"
        def date = setUpTestDate()
        def zone = ZoneId.of("GMT+5")

        when: "get zoned time string"
        def result = TimeStringConverter.getZonedTimeString(date, zone)

        then: "string conforms to pattern and zone"
        result == "21:00"
    }

    static def setUpTestDate() {
        def source = "2020-02-29T18:00:00+02:00"
        def date = ZonedDateTime.parse(source, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        return date
    }

}
