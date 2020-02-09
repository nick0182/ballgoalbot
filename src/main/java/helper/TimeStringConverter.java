package helper;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeStringConverter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE d MMMM u");

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private String messageTimeToBeDefined;

    private String apiTimezoneMoscow;

    private String apiTimezoneJerusalem;

    public TimeStringConverter(String messageTimeToBeDefined, String apiTimezoneMoscow, String apiTimezoneJerusalem) {
        this.messageTimeToBeDefined = messageTimeToBeDefined;
        this.apiTimezoneMoscow = apiTimezoneMoscow;
        this.apiTimezoneJerusalem = apiTimezoneJerusalem;
    }

    public String getDateString(ZonedDateTime date) {
        return date.format(DATE_FORMATTER);
    }

    public String getTimeString(ZonedDateTime date, String status) {
        if (status.equals(messageTimeToBeDefined)) {
            return messageTimeToBeDefined;
        } else {
            return date.toLocalTime().format(TIME_FORMATTER);
        }
    }

    public String getZonedTimeString(ZonedDateTime eventDateTime, String status, String timezone) {
        if (status.equals(messageTimeToBeDefined)) {
            return messageTimeToBeDefined;
        } else if (timezone.equals(apiTimezoneJerusalem)) {
            return eventDateTime.withZoneSameInstant(ZoneId.of("GMT+2")).toLocalTime().format(TIME_FORMATTER);
        } else if (timezone.equals(apiTimezoneMoscow)) {
            return eventDateTime.withZoneSameInstant(ZoneId.of("GMT+3")).toLocalTime().format(TIME_FORMATTER);
        }
        throw new RuntimeException("Timezone error");
    }

}
