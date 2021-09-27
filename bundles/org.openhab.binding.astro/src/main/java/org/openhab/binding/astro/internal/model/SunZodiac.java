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

import java.util.Calendar;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Extends the zodiac with a date range.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class SunZodiac {
    private final Range range;
    private final Zodiac sign;

    /**
     * Creates a Zodiac with a sign and a range.
     */
    public SunZodiac(Zodiac sign, Range range) {
        this.sign = sign;
        this.range = range;
    }

    /**
     * @return she start of the zodiac.
     */
    public Calendar getStart() {
        return range.getStart();
    }

    /**
     * @return the end of the zodiac.
     */
    public Calendar getEnd() {
        return range.getEnd();
    }

    /**
     * @return the sign of the zodiac.
     */
    public String getSign() {
        return sign.getSign();
    }

    /**
     * @return true, if the zodiac is valid on the specified calendar object.
     */
    public boolean isValid(Calendar calendar) {
        if (range.getStart() == null || range.getEnd() == null) {
            return false;
        }

        return range.getStart().getTimeInMillis() <= calendar.getTimeInMillis()
                && range.getEnd().getTimeInMillis() >= calendar.getTimeInMillis();
    }
}
