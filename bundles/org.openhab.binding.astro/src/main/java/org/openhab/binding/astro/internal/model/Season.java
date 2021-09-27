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

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.astro.internal.util.DateTimeUtils;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Holds the season dates of the year and the current name.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class Season {
    private final ZonedDateTime spring;
    private final ZonedDateTime summer;
    private final ZonedDateTime autumn;
    private final ZonedDateTime winter;

    private SeasonName name;

    public Season(ZonedDateTime spring, ZonedDateTime summer, ZonedDateTime autumn, ZonedDateTime winter,
            boolean useMeteorologicalSeason) {
        this.spring = useMeteorologicalSeason ? atMidnightOfFirstMonthDay(spring) : spring;
        this.summer = useMeteorologicalSeason ? atMidnightOfFirstMonthDay(summer) : summer;
        this.autumn = useMeteorologicalSeason ? atMidnightOfFirstMonthDay(autumn) : autumn;
        this.winter = useMeteorologicalSeason ? atMidnightOfFirstMonthDay(winter) : winter;
    }

    /**
     * @return the date of the beginning of spring.
     */
    public ZonedDateTime getSpring() {
        return spring;
    }

    /**
     * @return the date of the beginning of summer.
     */
    public ZonedDateTime getSummer() {
        return summer;
    }

    /**
     * @return the date of the beginning of autumn.
     */
    public ZonedDateTime getAutumn() {
        return autumn;
    }

    /**
     * @return the date of the beginning of winter.
     */
    public ZonedDateTime getWinter() {
        return winter;
    }

    /**
     * @return the current season name.
     */
    public SeasonName getName() {
        return name;
    }

    /**
     * Sets the current season name.
     */
    public void setName(SeasonName name) {
        this.name = name;
    }

    /**
     * @return the next season.
     */
    public @Nullable ZonedDateTime getNextSeason() {
        return DateTimeUtils.getNextFromToday(spring, summer, autumn, winter);
    }

    /**
     * @return the next season name.
     */
    public SeasonName getNextName() {
        int ordinal = name.ordinal() + 1;
        if (ordinal > 3) {
            ordinal = 0;
        }
        return SeasonName.values()[ordinal];
    }

    /**
     * @return the time left for current season
     */
    public State getTimeLeft() {
        ZonedDateTime next = getNextSeason();
        if (next != null) {
            ZonedDateTime now = ZonedDateTime.now().withZoneSameInstant(next.getZone());
            return new QuantityType<Time>(now.until(next, ChronoUnit.DAYS), Units.DAY);
        }
        return UnDefType.UNDEF;
    }

    private ZonedDateTime atMidnightOfFirstMonthDay(final ZonedDateTime zdt) {
        return zdt.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
        // result.set(Calendar.DAY_OF_MONTH, 1);
        // result.set(Calendar.HOUR_OF_DAY, 0);
        // result.set(Calendar.MINUTE, 0);
        // result.set(Calendar.SECOND, 0);
        // result.set(Calendar.MILLISECOND, 0);
        // return result;
    }
}
