package com.sha.client_service.util;

import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

public final class DateTimeUtils {

    private DateTimeUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final String DEFAULT_DATE_FORMAT = "yyyyMMdd";
    public static final DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)
            .withResolverStyle(ResolverStyle.SMART);
}
