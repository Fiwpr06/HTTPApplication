package com.fiwpr06.httpapplication.common.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateTimeUtil {
    private static final DateTimeFormatter HTTP_DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US)
                    .withZone(ZoneOffset.UTC);

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter TIME_ONLY_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    public static String formatHttpDate(long epochMillis) {
        return HTTP_DATE_FORMAT.format(Instant.ofEpochMilli(epochMillis));
    }

    public static String formatDisplay(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DISPLAY_FORMAT);
    }

    public static String formatTimeOnly(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(TIME_ONLY_FORMAT);
    }
}
