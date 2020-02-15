package com.nikolay.bot.ballgoal.command.impl


import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import spock.lang.Specification

class ZenitCommandTest extends Specification {

    def "generate text message"() {
        given: "zenit text command"
        ReplyKeyboard keyboard = Mock()
        def command = new ZenitCommand(keyboard)

        when: "generate message"
        def message = command.generateMessage()

        then: "message contains keyboard and text"
        verifyAll(message) {
            text == "Choose your timezone"
            replyMarkup == keyboard
        }
    }

}
