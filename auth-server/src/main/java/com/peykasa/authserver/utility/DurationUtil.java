package com.peykasa.authserver.utility;

import java.util.Calendar;
import java.util.Date;

/**
 * @author kamran
 */
public class DurationUtil {
    public static Calendar addDurationToCalendar(Date startTime, long durationMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startTime.getTime() + durationMillis);
        return calendar;
    }
}
