package com.railway.smartrailway.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;

public final class RunDays {

    private RunDays() {
    }

    public static boolean runsOn(String runDays, LocalDate date) {

        if (runDays == null || runDays.isBlank() || date == null) {
            return false;
        }

        String normalized = runDays
                .trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("\\s+", "");

        if (normalized.equals("DAILY")) {
            return true;
        }

        DayOfWeek day = date.getDayOfWeek();

        String dayCode = switch (day) {
            case MONDAY -> "MON";
            case TUESDAY -> "TUE";
            case WEDNESDAY -> "WED";
            case THURSDAY -> "THU";
            case FRIDAY -> "FRI";
            case SATURDAY -> "SAT";
            case SUNDAY -> "SUN";
        };

        Set<String> allowedDays = Set.of(normalized.split(","));

        return allowedDays.contains(dayCode);
    }
}