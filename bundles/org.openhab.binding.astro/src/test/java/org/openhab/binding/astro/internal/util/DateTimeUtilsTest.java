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

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.astro.internal.util.DateTimeUtils.getNext;

import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.astro.internal.calc.SeasonCalc;
import org.openhab.binding.astro.internal.model.Season;

/**
 * Test class for {@link DateTimeUtils}.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class DateTimeUtilsTest {

    private static final ZoneId TIME_ZONE = ZoneId.of("Europe/Amsterdam");
    private static final ZonedDateTime JAN_20_2020 = newZdt(2020, Month.JANUARY, 20, 1, 0, TIME_ZONE);
    private static final ZonedDateTime MAY_20_2020 = newZdt(2020, Month.MAY, 20, 1, 0, TIME_ZONE);
    private static final ZonedDateTime SEPT_20_2020 = newZdt(2020, Month.SEPTEMBER, 20, 1, 0, TIME_ZONE);
    private static final ZonedDateTime DEC_10_2020 = newZdt(2020, Month.DECEMBER, 1, 1, 0, TIME_ZONE);
    private static final double AMSTERDAM_LATITUDE = 52.367607;
    private static final double SYDNEY_LATITUDE = -33.87;

    private SeasonCalc seasonCalc = new SeasonCalc();

    @Test
    public void testGetSeasonAmsterdam() {
        Season season = seasonCalc.getSeason(DEC_10_2020, AMSTERDAM_LATITUDE, true);
        assertNextSeason(season.getSpring(), JAN_20_2020, season);
        assertNextSeason(season.getSummer(), MAY_20_2020, season);
        assertNextSeason(season.getWinter(), SEPT_20_2020, season);
        assertNextSeason(season.getSpring(), DEC_10_2020, season);
    }

    @Test
    public void testGetSeasonSydney() {
        Season season = seasonCalc.getSeason(DEC_10_2020, SYDNEY_LATITUDE, true);
        assertNextSeason(season.getAutumn(), JAN_20_2020, season);
        assertNextSeason(season.getWinter(), MAY_20_2020, season);
        assertNextSeason(season.getSummer(), SEPT_20_2020, season);
        assertNextSeason(season.getAutumn(), DEC_10_2020, season);
    }

    private void assertNextSeason(ZonedDateTime expectedSeason, ZonedDateTime date, Season season) {
        assertEquals(expectedSeason, DateTimeUtils.getNext(date, season.getSpring(), season.getSummer(),
                season.getAutumn(), season.getWinter()));
    }

    @Test
    public void testSeasonCalendarRemoval() {
        ZonedDateTime theDay = ZonedDateTime.of(2021, 1, 23, 8, 0, 0, 0, TIME_ZONE);
        ZonedDateTime spring = ZonedDateTime.of(2021, 3, 20, 8, 0, 0, 0, TIME_ZONE);
        ZonedDateTime summer = ZonedDateTime.of(2021, 6, 21, 8, 0, 0, 0, TIME_ZONE);
        ZonedDateTime autumn = ZonedDateTime.of(2021, 9, 21, 8, 0, 0, 0, TIME_ZONE);
        ZonedDateTime winter = ZonedDateTime.of(2021, 12, 21, 8, 0, 0, 0, TIME_ZONE);
        ZonedDateTime next = getNext(theDay, spring, summer, autumn, winter);
        assertTrue(spring.equals(next)); // found in ordered season list
        next = getNext(theDay, autumn, winter, spring, summer);
        assertTrue(spring.equals(next)); // found in unordered season list
        next = getNext(spring, spring, summer, autumn, winter);
        assertTrue(summer.equals(next)); // after beginning of current season, it's the next one.
        next = getNext(winter, spring, summer, autumn, winter);
        assertTrue(spring.equals(next)); // we loop on first one
    }

    private static ZonedDateTime newZdt(int year, Month month, int dayOfMonth, int hourOfDay, int minute,
            ZoneId zoneId) {
        return ZonedDateTime.of(year, month.getValue(), dayOfMonth, hourOfDay, minute, 0, 0, zoneId);
    }
}
