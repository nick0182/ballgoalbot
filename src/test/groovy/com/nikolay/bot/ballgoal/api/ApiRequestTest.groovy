package com.nikolay.bot.ballgoal.api

import com.nikolay.bot.ballgoal.api.impl.ApiRequestFootball
import spock.lang.Specification

class ApiRequestTest extends Specification {

    def request = new ApiRequestFootball("api-football-v1.p.rapidapi.com",
            "1cccd3131bmshed7ddf66e006ec5p168f9fjsn3ab66e62ad85")

    def "should throw exception when non-existing resource"() {
        given: "non-existing resource"
        def resource = "/non/existing/resource"

        when: "api call"
        request.call(resource)

        then: "IllegalArgumentException is thrown"
        thrown(IllegalArgumentException)
    }

    def "api call"() {
        expect: "api yields valid result"
        request.call(resource).contains(str)

        where:
        resource << ["/teams/team/596", "/v2/players/search/Kokorin"]
        str << ["Zenit Saint Petersburg", "Aleksandr Kokorin"]
    }

}
