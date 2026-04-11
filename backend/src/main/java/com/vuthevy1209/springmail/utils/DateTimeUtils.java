package com.vuthevy1209.springmail.utils;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class DateTimeUtils {

    public static String formatTimestamp(long timestampMs) {
        Instant instant = Instant.ofEpochMilli(timestampMs);
        LocalDate today = LocalDate.now();
        LocalDate msgDate = instant
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        if (msgDate.equals(today)) {
            return DateTimeFormatter
                    .ofPattern("HH:mm")
                    .withZone(ZoneId.systemDefault())
                    .format(instant);
        } else if (msgDate.getYear() == today.getYear()) {
            return DateTimeFormatter
                    .ofPattern("d/M")
                    .withZone(ZoneId.systemDefault())
                    .format(instant);
        } else {
            return DateTimeFormatter
                    .ofPattern("d/M/yyyy")
                    .withZone(ZoneId.systemDefault())
                    .format(instant);
        }
    }
}
