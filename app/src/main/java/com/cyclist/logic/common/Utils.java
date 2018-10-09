package com.cyclist.logic.common;

import java.util.Locale;

public class Utils {
    public static String getTimeString(double duration, String text) {
        double totalMinutes = duration / 60;
        Double minutes = totalMinutes % 60;
        Double hours = totalMinutes / 60;
        if (hours > 1) {
            return String.format(Locale.getDefault(), " %1d:%2d", hours.intValue(), minutes.intValue());
        } else {
            if (text != null) {
                return String.format(Locale.getDefault(), " %1d %s", minutes.intValue(), text);
            } else{
                return String.format(Locale.getDefault(), " %1d", minutes.intValue());
            }
        }
    }
}
