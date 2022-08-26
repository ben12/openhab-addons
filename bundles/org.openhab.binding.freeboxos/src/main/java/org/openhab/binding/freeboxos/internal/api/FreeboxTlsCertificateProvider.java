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
package org.openhab.binding.freeboxos.internal.api;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.DEFAULT_FREEBOX_NAME;

import java.net.URL;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.net.http.TlsCertificateProvider;
import org.osgi.service.component.annotations.Component;

/**
 * Provides a CertificateManager for the Freebox SSL certificate
 *
 * @author Gaël L'hopital - Initial Contribution
 */
@Component
@NonNullByDefault
public class FreeboxTlsCertificateProvider implements TlsCertificateProvider {

    @Override
    public String getHostName() {
        return DEFAULT_FREEBOX_NAME;
    }

    @Override
    public URL getCertificate() {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("freeboxECCRootCA.crt");
        if (resource != null) {
            return resource;
        }
        throw new IllegalStateException("Certifcate resource not found or not accessible");
    }
}