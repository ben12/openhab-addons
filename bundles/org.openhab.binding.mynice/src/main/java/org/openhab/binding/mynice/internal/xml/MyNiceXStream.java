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
package org.openhab.binding.mynice.internal.xml;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mynice.internal.xml.dto.Authentication;
import org.openhab.binding.mynice.internal.xml.dto.CommandType;
import org.openhab.binding.mynice.internal.xml.dto.Device;
import org.openhab.binding.mynice.internal.xml.dto.Interface;
import org.openhab.binding.mynice.internal.xml.dto.Properties;
import org.openhab.binding.mynice.internal.xml.dto.Response;
import org.openhab.binding.mynice.internal.xml.dto.UserPerm;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * The {@link MyNiceXStream} class is a utility class that wraps an XStream
 * object and provide additional functionality specific to the PlugwiseHA
 * binding. It automatically load the correct converter classes and processes
 * the XStream annotions used by the object classes.
 *
 * @author Gaël L'hopital - Initial contribution
 */

@NonNullByDefault
public class MyNiceXStream extends XStream {
    public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";

    public MyNiceXStream() {
        super(new StaxDriver());
        allowTypesByWildcard(new String[] { Response.class.getPackageName() + ".**" });
        setClassLoader(getClass().getClassLoader());
        autodetectAnnotations(true);
        ignoreUnknownElements();
        alias("Response", Response.class);
        alias("Authentication", Authentication.class);
        alias("CommandType", CommandType.class);
        alias("UserPerm", UserPerm.class);
        alias("Error", Error.class);
        alias("Interface", Interface.class);
        alias("Device", Device.class);
        alias("Properties", Properties.class);
    }

    public Response deserialize(String response) {
        return (Response) fromXML(XML_HEADER + response);
    }
}
