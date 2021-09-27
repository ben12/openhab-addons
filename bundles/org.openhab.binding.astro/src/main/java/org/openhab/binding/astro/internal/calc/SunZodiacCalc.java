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
package org.openhab.binding.astro.internal.calc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.astro.internal.model.SunZodiac;
import org.openhab.binding.astro.internal.model.Zodiac;
import org.openhab.binding.astro.internal.util.DateTimeUtils;

/**
 * Calculates the sign and range of the current zodiac.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class SunZodiacCalc {
    private Map<Integer, List<SunZodiac>> zodiacsByYear = new HashMap<>();

    /**
     * @return the zodiac for the specified calendar.
     */
    public Optional<SunZodiac> getZodiac(Calendar calendar) {

        int year = calendar.get(Calendar.YEAR);
        List<SunZodiac> zodiacs;

        if (zodiacsByYear.containsKey(year)) {
            zodiacs = zodiacsByYear.get(year);
        } else {
            zodiacs = calculateZodiacs(year);
            zodiacsByYear.clear();
            zodiacsByYear.put(year, zodiacs);
        }

        return zodiacs != null ? zodiacs.stream().filter(z -> z.isValid(calendar)).findFirst() : Optional.empty();
    }

    /**
     * Calculates the zodiacs for the current year.
     */
    private List<SunZodiac> calculateZodiacs(int year) {
        List<SunZodiac> zodiacs = new ArrayList<>();

        Arrays.stream(Zodiac.values()).forEach(sign -> {
            int beginYear = year;
            int endYear = year + (sign.crossYears() ? 1 : 0);
            zodiacs.add(new SunZodiac(sign, DateTimeUtils.getRange(beginYear, sign.getBeginMonth(), sign.getBeginDay(),
                    endYear, sign.getEndMonth(), sign.getEndDay())));
            if (endYear != beginYear) {
                zodiacs.add(new SunZodiac(sign, DateTimeUtils.getRange(beginYear - 1, sign.getBeginMonth(),
                        sign.getBeginDay(), beginYear, sign.getEndMonth(), sign.getEndDay())));
            }
        });

        return zodiacs;
    }
}
