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

import static org.openhab.core.library.unit.MetricPrefix.KILO;
import static org.openhab.core.library.unit.SIUnits.METRE;

import java.time.ZonedDateTime;

import javax.measure.quantity.Length;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.State;

/**
 * Holds a distance informations.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Christoph Weitkamp - Introduced UoM
 */
@NonNullByDefault
public class MoonDistance {
    private ZonedDateTime date;
    private double distance;

    public MoonDistance(ZonedDateTime date, double distance) {
        this.date = date;
        this.distance = distance;
    }

    /**
     * @return the date of the calculated distance.
     */
    public ZonedDateTime getDate() {
        return date;
    }

    /**
     * @return the distance in kilometers.
     */
    public State getDistance() {
        return new QuantityType<Length>(distance, KILO(METRE));
    }
}
