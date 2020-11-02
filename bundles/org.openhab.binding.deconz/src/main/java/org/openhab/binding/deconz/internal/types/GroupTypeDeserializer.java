/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.deconz.internal.types;

import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * Custom deserializer for {@link GroupType}
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class GroupTypeDeserializer implements JsonDeserializer<GroupType> {
    @Override
    public GroupType deserialize(@Nullable JsonElement json, @Nullable Type typeOfT,
            @Nullable JsonDeserializationContext context) throws JsonParseException {
        String s = json != null ? json.getAsString() : null;
        return s == null ? GroupType.UNKNOWN : GroupType.fromString(s);
    }
}