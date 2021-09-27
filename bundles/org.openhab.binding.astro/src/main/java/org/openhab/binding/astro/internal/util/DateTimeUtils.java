/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.astro.internal.util;

import java.time.Instant;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.regex.Pattern;

import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.astro.internal.config.AstroChannelConfig;
import org.openhab.binding.astro.internal.model.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common used DateTime functions.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class DateTimeUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(DateTimeUtils.class);
    private static final Pattern HHMM_PATTERN = Pattern.compile("^([0-1][0-9]|2[0-3])(:[0-5][0-9])$");

    private static final double J1970 = 2440588.0;
    private static final double MILLISECONDS_PER_DAY = 1000 * 60 * 60 * 24;

    /** Constructor */
    private DateTimeUtils() {
        throw new IllegalAccessError("Non-instantiable");
    }

    /**
     * Truncates the time from the calendar object.
     */
    public static Calendar truncateToMidnight(Calendar calendar) {
        return DateUtils.truncate(calendar, Calendar.DAY_OF_MONTH);
    }

    /**
     * Creates a Range object within the specified months and days. The start
     * time is midnight, the end time is end of the day.
     */
    private static Range getRange(int startYear, int startMonth, int startDay, int endYear, int endMonth, int endDay) {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.YEAR, startYear);
        start.set(Calendar.MONTH, startMonth);
        start.set(Calendar.DAY_OF_MONTH, startDay);
        start = truncateToMidnight(start);

        Calendar end = Calendar.getInstance();
        end.set(Calendar.YEAR, endYear);
        end.set(Calendar.MONTH, endMonth);
        end.set(Calendar.DAY_OF_MONTH, endDay);
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 999);

        return new Range(start, end);
    }

    /**
     * Creates a Range object within the specified months and days. The start
     * time is midnight, the end time is end of the day.
     */
    public static Range getRange(int startYear, Month startMonth, int startDay, int endYear, Month endMonth,
            int endDay) {
        return getRange(startYear, startMonth.getValue() - 1, startDay, endYear, endMonth.getValue() - 1, endDay);
    }

    /**
     * Returns a calendar object from a julian date.
     */
    public static Calendar toCalendar(double julianDate) {
        if (Double.compare(julianDate, Double.NaN) == 0 || julianDate == 0) {
            throw new IllegalArgumentException();
        }
        long millis = (long) ((julianDate + 0.5 - J1970) * MILLISECONDS_PER_DAY);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        return DateUtils.round(cal, Calendar.MINUTE);
    }

    /**
     * Returns a ZonedDateTime object from a julian date.
     */
    public static ZonedDateTime toZonedDateTime(double julianDate) {
        if (Double.compare(julianDate, Double.NaN) == 0 || julianDate == 0) {
            throw new IllegalArgumentException();
        }
        long millis = (long) ((julianDate + 0.5 - J1970) * MILLISECONDS_PER_DAY);
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneOffset.UTC).truncatedTo(ChronoUnit.MINUTES);
    }

    /**
     * Returns the julian date from the calendar object.
     */
    public static double dateToJulianDate(Calendar calendar) {
        return calendar.getTimeInMillis() / MILLISECONDS_PER_DAY - 0.5 + J1970;
    }

    /**
     * Returns the midnight julian date from the calendar object.
     */
    public static double midnightDateToJulianDate(Calendar calendar) {
        return dateToJulianDate(truncateToMidnight(calendar));
    }

    /**
     * Returns the end of day from the calendar object.
     */
    public static Calendar endOfDayDate(Calendar calendar) {
        Calendar cal = (Calendar) calendar.clone();
        cal = DateUtils.ceiling(cal, Calendar.DATE);
        cal.add(Calendar.MILLISECOND, -1);
        return cal;
    }

    /**
     * Returns the end of day julian date from the calendar object.
     */
    public static double endOfDayDateToJulianDate(Calendar calendar) {
        return dateToJulianDate(endOfDayDate(calendar));
    }

    /**
     * Returns the year of the calendar object as a decimal value.
     */
    public static double getDecimalYear(Calendar calendar) {
        return calendar.get(Calendar.YEAR)
                + (double) calendar.get(Calendar.DAY_OF_YEAR) / calendar.getActualMaximum(Calendar.DAY_OF_YEAR);
    }

    /**
     * Converts the time (hour.minute) to a calendar object.
     */
    public static @Nullable Calendar timeToCalendar(Calendar calendar, double time) {
        if (time < 0.0) {
            return null;
        }
        Calendar cal = (Calendar) calendar.clone();
        int hour = 0;
        int minute = 0;
        if (time == 24.0) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
        } else {
            hour = (int) time;
            minute = (int) ((time * 100) - (hour * 100));
        }
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        return DateUtils.truncate(cal, Calendar.MINUTE);
    }

    /**
     * Returns true, if two calendar objects are on the same day ignoring time.
     */
    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        return DateUtils.isSameDay(cal1, cal2);
    }

    public static boolean isSameDay(ZonedDateTime date1, ZonedDateTime date2) {
        return date1.truncatedTo(ChronoUnit.DAYS).equals(date2.truncatedTo(ChronoUnit.DAYS));
    }

    /**
     * @return the next ZonedDateTime from today.
     */
    public static @Nullable ZonedDateTime getNextFromToday(ZonedDateTime... seasons) {
        return getNext(ZonedDateTime.now().withZoneSameLocal(seasons[0].getZone()), seasons);
    }

    public static @Nullable ZonedDateTime getNext(ZonedDateTime now, ZonedDateTime... seasons) {
        ZonedDateTime next = null;
        ZonedDateTime firstSeasonOfYear = null;
        for (ZonedDateTime season : seasons) {
            if (firstSeasonOfYear == null || season.isBefore(firstSeasonOfYear)) {
                firstSeasonOfYear = season;
            }
            if (season.isAfter(now) && (next == null || season.isBefore(next))) {
                next = season;
            }
        }
        return next == null ? firstSeasonOfYear : next;
    }

    /**
     * Returns true, if cal1 is greater or equal than cal2, ignoring seconds.
     */
    public static boolean isTimeGreaterEquals(Calendar cal1, Calendar cal2) {
        Calendar truncCal1 = DateUtils.truncate(cal1, Calendar.MINUTE);
        Calendar truncCal2 = DateUtils.truncate(cal2, Calendar.MINUTE);
        return truncCal1.getTimeInMillis() >= truncCal2.getTimeInMillis();
    }

    public static boolean isTimeGreaterEquals(ZonedDateTime zdt1, ZonedDateTime zdt2) {
        ZonedDateTime truncCal1 = zdt1.truncatedTo(ChronoUnit.MINUTES);
        ZonedDateTime truncCal2 = zdt2.truncatedTo(ChronoUnit.MINUTES);
        return truncCal1.isAfter(truncCal2) || truncCal1.isEqual(truncCal2);
    }

    /**
     * Applies the config to the given calendar.
     */
    public static Calendar applyConfig(Calendar cal, AstroChannelConfig config) {
        Calendar cCal = cal;
        if (config.offset != 0) {
            Calendar cOffset = Calendar.getInstance();
            cOffset.setTime(cCal.getTime());
            cOffset.add(Calendar.MINUTE, config.offset);
            cCal = cOffset;
        }

        Calendar cEarliest = adjustTime(cCal, getMinutesFromTime(config.earliest));
        if (cCal.before(cEarliest)) {
            return cEarliest;
        }
        Calendar cLatest = adjustTime(cCal, getMinutesFromTime(config.latest));
        if (cCal.after(cLatest)) {
            return cLatest;
        }

        return cCal;
    }

    public static ZonedDateTime applyConfig(ZonedDateTime zdt, AstroChannelConfig config) {
        ZonedDateTime result = zdt;
        if (config.offset != 0) {
            result = result.plusMinutes(config.offset);
        }

        ZonedDateTime cEarliest = adjustTime(result, getMinutesFromTime(config.earliest));
        if (result.isBefore(cEarliest)) {
            return cEarliest;
        }
        ZonedDateTime cLatest = adjustTime(result, getMinutesFromTime(config.latest));
        if (result.isAfter(cLatest)) {
            return cLatest;
        }

        return result;
    }

    private static Calendar adjustTime(Calendar cal, int minutes) {
        if (minutes > 0) {
            Calendar cTime = Calendar.getInstance();
            cTime = DateUtils.truncate(cal, Calendar.DAY_OF_MONTH);
            cTime.add(Calendar.MINUTE, minutes);
            return cTime;
        }
        return cal;
    }

    private static ZonedDateTime adjustTime(ZonedDateTime zdt, int minutes) {
        if (minutes > 0) {
            return zdt.truncatedTo(ChronoUnit.DAYS).plusMinutes(minutes);
        }
        return zdt;
    }

    /**
     * Parses a HH:MM string and returns the minutes.
     */
    private static int getMinutesFromTime(@Nullable String configTime) {
        if (configTime != null) {
            String time = configTime.trim();
            if (!time.isEmpty()) {
                try {
                    if (!HHMM_PATTERN.matcher(time).matches()) {
                        throw new NumberFormatException();
                    } else {
                        String[] elements = time.split(":");
                        int hour = Integer.parseInt(elements[0]);
                        int minutes = Integer.parseInt(elements[1]);
                        return (hour * 60) + minutes;
                    }
                } catch (NumberFormatException ex) {
                    LOGGER.warn(
                            "Can not parse astro channel configuration '{}' to hour and minutes, use pattern hh:mm, ignoring!",
                            time);
                }

            }
        }
        return 0;
    }
}
