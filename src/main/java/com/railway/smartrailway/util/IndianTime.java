package com.railway.smartrailway.util;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Date helper that always uses Indian Standard Time.
 * The server may run in UTC, so LocalDate.now() can give
 * the wrong date for several hours every night in India.
 */
public final class IndianTime {

    public static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private IndianTime() {
    }

    public static LocalDate today() {
        return LocalDate.now(IST);
    }
}