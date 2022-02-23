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
package org.openhab.binding.freeboxos.internal.api.rest;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.freeboxos.internal.api.ApiHandler;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.Response.ErrorCode;
import org.openhab.binding.freeboxos.internal.api.lan.LanManager;
import org.openhab.binding.freeboxos.internal.api.login.LoginManager;
import org.openhab.binding.freeboxos.internal.api.login.Session;
import org.openhab.binding.freeboxos.internal.api.login.Session.Permission;
import org.openhab.binding.freeboxos.internal.api.netshare.NetShareManager;
import org.openhab.binding.freeboxos.internal.api.wifi.WifiManager;
import org.openhab.binding.freeboxos.internal.config.FreeboxOsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeboxOsSession} is responsible for sending requests toward
 * a given url and transform the answer in appropriate dto.
 *
 * @author Gaël L'Hopital - Initial contribution
 */
@NonNullByDefault
public class FreeboxOsSession {
    private final Logger logger = LoggerFactory.getLogger(FreeboxOsSession.class);
    private final Map<Class<? extends RestManager>, RestManager> restManagers = new HashMap<>();

    private final ApiHandler apiHandler;

    private @NonNullByDefault({}) UriBuilder uriBuilder;
    private @Nullable String appToken;
    private @Nullable Session session;

    public FreeboxOsSession(ApiHandler apiHandler) {
        this.apiHandler = apiHandler;
    }

    /**
     * @param configuration
     * @return the app token used to open the session (can have changed if newly granted)
     * @throws FreeboxException
     */
    public String initialize(FreeboxOsConfiguration configuration) throws FreeboxException {
        UriBuilder uriBuilder = UriBuilder.fromPath("/").scheme(configuration.getScheme()).port(configuration.getPort())
                .host(configuration.apiDomain);
        ApiVersion version = apiHandler.executeUri(uriBuilder.clone().path("api_version").build(), HttpMethod.GET,
                ApiVersion.class, null, configuration);
        this.appToken = configuration.appToken;
        this.uriBuilder = uriBuilder.path(version.baseUrl());
        return initiateConnection();
    }

    private String initiateConnection() throws FreeboxException {
        try {
            String localToken = appToken;
            if (localToken != null) {
                session = getManager(LoginManager.class).openSession(localToken);
                getManager(NetShareManager.class);
                getManager(LanManager.class);
                getManager(WifiManager.class);
                return localToken;
            }
            throw new FreeboxException(ErrorCode.INVALID_TOKEN);
        } catch (FreeboxException e) {
            ErrorCode errorCode = e.getErrorCode();
            if (ErrorCode.INVALID_TOKEN.equals(errorCode)) {
                appToken = getManager(LoginManager.class).grant();
                return initiateConnection();
            }
            throw e;
        }
    }

    public void closeSession() {
        if (session != null) {
            try {
                getManager(LoginManager.class).closeSession();
            } catch (FreeboxException e) {
                logger.info("Error closing session : {}", e.getMessage());
            }
            session = null;
        }
        appToken = null;
        restManagers.clear();
    }

    private @Nullable String sessionToken() {
        Session localSession = session;
        return localSession != null ? localSession.getSessionToken() : null;
    }

    private <F, T extends Response<F>> @Nullable F execute(URI uri, HttpMethod method, Class<T> clazz,
            boolean retryAuth, int retryCount, @Nullable Object aPayload) throws FreeboxException {
        try {
            T response = apiHandler.executeUri(uri, method, clazz, sessionToken(), aPayload);
            if (response.getErrorCode() == ErrorCode.INTERNAL_ERROR && retryCount > 0) {
                return execute(uri, method, clazz, false, retryCount - 1, aPayload);
            } else if (retryAuth && response.getErrorCode() == ErrorCode.AUTHORIZATION_REQUIRED) {
                initiateConnection();
                return execute(uri, method, clazz, false, retryCount, aPayload);
            }
            if (!response.isSuccess()) {
                throw new FreeboxException("Api request failed : %s", response.getMsg());
            }
            return response.getResult();
        } catch (FreeboxException e) {
            if (ErrorCode.AUTHORIZATION_REQUIRED.equals(e.getErrorCode())) {
                initiateConnection();
                return execute(uri, method, clazz, false, retryCount, aPayload);
            }
            throw e;
        }
    }

    <F, T extends Response<F>> @Nullable F execute(URI uri, HttpMethod method, Class<T> clazz,
            @Nullable Object aPayload) throws FreeboxException {
        boolean retryAuth = sessionToken() != null;
        return execute(uri, method, clazz, retryAuth, 3, aPayload);
    }

    @SuppressWarnings("unchecked")
    public synchronized <T extends RestManager> T getManager(Class<T> clazz) throws FreeboxException {
        RestManager manager = restManagers.get(clazz);
        if (manager == null) {
            try {
                Constructor<T> managerConstructor = clazz.getConstructor(FreeboxOsSession.class);
                manager = managerConstructor.newInstance(this);
                restManagers.put(clazz, manager);
            } catch (SecurityException | ReflectiveOperationException e) {
                throw new FreeboxException(e, "Unable to call RestManager constructor for %s", clazz.getName());
            }
        }
        return (T) manager;
    }

    public <T extends RestManager> void addManager(Class<T> clazz, RestManager manager) {
        restManagers.put(clazz, manager);
    }

    boolean hasPermission(Permission required) {
        Session localSession = session;
        return localSession != null && localSession.hasPermission(required);
    }

    public UriBuilder getUriBuilder() {
        return uriBuilder.clone();
    }
}
