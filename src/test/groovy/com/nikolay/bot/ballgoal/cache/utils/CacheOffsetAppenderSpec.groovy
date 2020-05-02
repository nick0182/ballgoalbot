package com.nikolay.bot.ballgoal.cache.utils

import spock.lang.Specification

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.ZoneOffset

class CacheOffsetAppenderSpec extends Specification {

    def "should return empty offset string when null dateTime"() {
        when: "append offset"
        def result = CacheOffsetAppender.appendOffset(null, ZoneOffset.UTC)

        then: "empty offset string"
        result.isEmpty()
    }

    def "should return offset string when valid dateTime and offset"() {
        given: "valid input parameters"
        def dateTime = LocalDateTime.of(LocalDate.of(2020, Month.APRIL, 24), LocalTime.NOON)
        def offset = ZoneOffset.of("+07:00")

        when: "append offset"
        def result = CacheOffsetAppender.appendOffset(dateTime, offset)

        then: "valid offset string"
        verifyAll {
            result.contains("Friday 24 April")
            result.contains("19:00")
        }
    }
}
