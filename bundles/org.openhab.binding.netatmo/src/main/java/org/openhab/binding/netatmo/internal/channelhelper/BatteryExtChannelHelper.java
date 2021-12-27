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
package org.openhab.binding.netatmo.internal.channelhelper;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.toStringType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.NAModule;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.core.types.State;

/**
 * The {@link BatteryExtChannelHelper} handles specific channels of modules using batteries
 * having battery status information available on top of standard information
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class BatteryExtChannelHelper extends BatteryChannelHelper {

    public BatteryExtChannelHelper() {
        super(GROUP_BATTERY_EXTENDED);
    }

    @Override
    protected @Nullable State internalGetProperty(String channelId, NAThing naThing) {
        if (naThing instanceof NAModule) {
            NAModule module = (NAModule) naThing;
            if (CHANNEL_BATTERY_STATUS.equals(channelId)) {
                return toStringType(module.getBatteryState());
            }
        }
        return super.internalGetProperty(channelId, naThing);
    }
}
