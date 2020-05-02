package com.nikolay.bot.ballgoal.json.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import spock.lang.Specification

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.format.DateTimeParseException

class LocalDateTimeDeserializerSpec extends Specification {

    StdDeserializer<LocalDateTime> deserializer = new LocalDateTimeDeserializer()

    def "should throw exception when no date-time string"() {
        given: "string with no date-time"
        JsonParser parser = Mock()
        parser.readValueAs(String.class) >> "no date-time here"
        DeserializationContext context = Mock()

        when: "deserialize string"
        deserializer.deserialize(parser, context)

        then: "IOException is thrown"
        thrown(DateTimeParseException)
    }

    def "should throw exception when date-time with no offset string"() {
        given: "date-time with no offset string"
        JsonParser parser = Mock()
        parser.readValueAs(String.class) >> "2020-04-24T18:00:00"
        DeserializationContext context = Mock()

        when: "deserialize string"
        deserializer.deserialize(parser, context)

        then: "IOException is thrown"
        thrown(DateTimeParseException)
    }

    def "should deserialize when valid date-time with offset string"() {
        given: "valid date-time with offset string"
        JsonParser parser = Mock()
        parser.readValueAs(String.class) >> "2020-04-24T18:00:00+01:00"
        DeserializationContext context = Mock()

        when: "deserialize string"
        def result = deserializer.deserialize(parser, context)

        then: "valid LocalDateTime"
        result == LocalDateTime.of(
                LocalDate.of(2020, Month.APRIL, 24),
                LocalTime.of(18, 0))
    }
}
