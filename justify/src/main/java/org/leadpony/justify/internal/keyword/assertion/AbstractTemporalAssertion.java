package org.leadpony.justify.internal.keyword.assertion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.json.JsonValue;

/**
 * Abstract implementation of date assertions.
 *
 * @author JoaoCamposFrom94
 */
public abstract class AbstractTemporalAssertion extends AbstractStringAssertion {
    protected final String date;

    protected AbstractTemporalAssertion(JsonValue json, final String date) {
        super(json);
        this.date = date;
    }

    /**
     * By convention, when no Time is specified, we assume the start of the day.
     * By convention, when no timezone is specified, we assume the system default.
     * @param date being parsed.
     * @return the date parsed.
     * @throws {@link DateTimeParseException}
     */
    protected static ZonedDateTime parseDate(String date) {
        try {
            LocalDate parse = LocalDate.parse(date);
            LocalDateTime localDateTime = parse.atStartOfDay();
            return localDateTime.atZone(ZoneId.systemDefault());
        } catch (DateTimeParseException e) {
            try {
                LocalDateTime parse = LocalDateTime.parse(date);
                return parse.atZone(ZoneId.systemDefault());
            } catch (DateTimeParseException e1) {
                try {
                    return ZonedDateTime.parse(date);
                } catch (DateTimeParseException e2) {
                    try {
                        LocalTime localTime = LocalTime.parse(date);
                        LocalDateTime localDateTime = localTime.atDate(LocalDate.now());
                        return localDateTime.atZone(ZoneId.systemDefault());
                    } catch (DateTimeParseException e3) {
                        LocalTime localTime = LocalTime.parse(date, DateTimeFormatter.ISO_OFFSET_TIME);
                        LocalDateTime localDateTime = localTime.atDate(LocalDate.now());
                        return localDateTime.atZone(ZoneId.systemDefault());
                    }
                }
            }
        }
    }
}

