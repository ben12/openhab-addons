/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.glh.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link CardBookConfiguration} is the class used to match the
 * thing configuration.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class CardBookConfiguration {
    public String rootDirectory = "";
    public Integer refresh = 1;
    public String matchCategory = "";

    public String domain = "";
    public String username = "";
    public String password = "";
    public Boolean useHTTPS = true;
}
