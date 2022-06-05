/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mynice.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MyNiceBindingConstants} class defines common constants, which are used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class MyNiceBindingConstants {
    private static final String BINDING_ID = "mynice";

    // List of all Channel ids
    public static final String DOOR_STATUS = "doorstatus";
    public static final String DOOR_OBSTRUCTED = "obstruct";
    public static final String DOOR_MOVING = "moving";

    // List of all Thing Type UIDs
    public static final ThingTypeUID BRIDGE_TYPE_IT4WIFI = new ThingTypeUID(BINDING_ID, "it4wifi");
    public static final ThingTypeUID THING_TYPE_SWING = new ThingTypeUID(BINDING_ID, "swing");
}
