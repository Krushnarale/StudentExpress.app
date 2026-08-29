package com.core2web.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class TrackingUtil {

    private static final Random RANDOM = new Random();

    public static String generateTrackingId() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randNum = 1000 + RANDOM.nextInt(9000);
        return "SE-" + dateStr + "-" + randNum;
    }
}
