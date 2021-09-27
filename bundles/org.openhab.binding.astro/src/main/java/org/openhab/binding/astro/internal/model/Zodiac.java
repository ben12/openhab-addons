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
package org.openhab.binding.astro.internal.model;

import java.time.Month;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * All zodiac signs.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public enum Zodiac {
    ARIES(Month.MARCH, 21, Month.APRIL, 19),
    TAURUS(Month.APRIL, 20, Month.MAY, 20),
    GEMINI(Month.MAY, 21, Month.JUNE, 20),
    CANCER(Month.JUNE, 21, Month.JULY, 22),
    LEO(Month.JULY, 23, Month.AUGUST, 22),
    VIRGO(Month.AUGUST, 23, Month.SEPTEMBER, 22),
    LIBRA(Month.SEPTEMBER, 23, Month.OCTOBER, 22),
    SCORPIO(Month.OCTOBER, 23, Month.NOVEMBER, 21),
    SAGITTARIUS(Month.NOVEMBER, 22, Month.DECEMBER, 21),
    CAPRICORN(Month.DECEMBER, 22, Month.JANUARY, 19),
    AQUARIUS(Month.JANUARY, 20, Month.FEBRUARY, 18),
    PISCES(Month.FEBRUARY, 19, Month.MARCH, 20);

    private final Month beginMonth;
    private final int beginDay;
    private final Month endMonth;
    private final int endDay;

    Zodiac(Month beginMonth, int beginDay, Month endMonth, int endDay) {
        this.beginMonth = beginMonth;
        this.beginDay = beginDay;
        this.endMonth = endMonth;
        this.endDay = endDay;
    }

    public Month getBeginMonth() {
        return beginMonth;
    }

    public int getBeginDay() {
        return beginDay;
    }

    public Month getEndMonth() {
        return endMonth;
    }

    public int getEndDay() {
        return endDay;
    }

    /**
     * @return the sign of the zodiac.
     */
    public String getSign() {
        return name();
    }

    public boolean crossYears() {
        return endMonth.getValue() < beginMonth.getValue();
    }
}
