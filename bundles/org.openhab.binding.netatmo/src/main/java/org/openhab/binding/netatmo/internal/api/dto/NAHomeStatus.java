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
package org.openhab.binding.netatmo.internal.api.dto;

import org.openhab.binding.netatmo.internal.api.ApiResponse;

/**
 * The {@link NAHomeStatus} holds data for a given home.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

public class NAHomeStatus {
    public class NAHomeStatusResponse extends ApiResponse<NAHomeStatus> {
    }

    private NAHome home;

    public NAHome getHome() {
        return home;
    }
}
