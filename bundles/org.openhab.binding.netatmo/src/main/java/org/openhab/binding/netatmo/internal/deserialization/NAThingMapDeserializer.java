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
package org.openhab.binding.netatmo.internal.deserialization;

import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.ModuleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * The {@link NAThingMapDeserializer} is a specialized deserializer aimed to transform
 * a list of `NAThings` into a map identified by the object's id.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class NAThingMapDeserializer implements JsonDeserializer<NAThingMap> {
    private static final String TYPE_KEY = "type";
    private final Logger logger = LoggerFactory.getLogger(NAThingMapDeserializer.class);

    @Override
    public @Nullable NAThingMap deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        if (json instanceof JsonArray) {
            NAThingMap result = new NAThingMap();
            ((JsonArray) json).forEach(item -> {
                String itemType = item.getAsJsonObject().get(TYPE_KEY).getAsString();
                if (ModuleType.isModuleTypeImplemented(itemType)) {
                    ModuleType module = ModuleType.valueOf(itemType);
                    Class<?> dto = module.getDto();
                    if (dto != null) {
                        result.put(context.deserialize(item, dto));
                    } else {
                        logger.warn("No dto declared for thing of type : {}", itemType);
                    }
                } else {
                    logger.warn("Unsupported moduletype {} found during discovery", itemType);
                }
            });
            return result;
        }
        throw new JsonParseException(String.format("An array was expected but received : %s", json));
    }
}