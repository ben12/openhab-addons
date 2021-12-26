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
package org.openhab.binding.netatmo.internal.handler;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.action.DeviceActions;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.ConnectionListener;
import org.openhab.binding.netatmo.internal.api.ConnectionStatus;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.data.ModuleType;
import org.openhab.binding.netatmo.internal.api.data.ModuleType.RefreshPolicy;
import org.openhab.binding.netatmo.internal.api.dto.NADevice;
import org.openhab.binding.netatmo.internal.api.dto.NAHome;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.api.dto.NAPlace;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.binding.netatmo.internal.channelhelper.AbstractChannelHelper;
import org.openhab.binding.netatmo.internal.config.NetatmoThingConfiguration;
import org.openhab.binding.netatmo.internal.providers.NetatmoDescriptionProvider;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link DeviceHandler} is the abstract class that handles
 * common behaviors of all netatmo devices
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class DeviceHandler extends BaseBridgeHandler implements ConnectionListener {

    private final Logger logger = LoggerFactory.getLogger(DeviceHandler.class);
    private final List<AbstractChannelHelper> channelHelpers;

    protected final Map<String, DeviceHandler> dataListeners = new ConcurrentHashMap<>();
    protected final NetatmoDescriptionProvider descriptionProvider;
    protected final ApiBridge apiBridge;

    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable RefreshStrategy refreshStrategy;

    protected @NonNullByDefault({}) NetatmoThingConfiguration config;

    public DeviceHandler(Bridge bridge, List<AbstractChannelHelper> channelHelpers, ApiBridge apiBridge,
            NetatmoDescriptionProvider descriptionProvider) {
        super(bridge);
        this.apiBridge = apiBridge;
        this.descriptionProvider = descriptionProvider;
        this.channelHelpers = channelHelpers;
    }

    @Override
    public void initialize() {
        logger.debug("initializing handler for thing {}", getThing().getUID());

        config = getThing().getConfiguration().as(NetatmoThingConfiguration.class);

        ModuleType supportedThingType = ModuleType.valueOf(getThing().getThingTypeUID().getId());
        refreshStrategy = supportedThingType.refreshPeriod == RefreshPolicy.AUTO ? new RefreshStrategy(-1)
                : supportedThingType.refreshPeriod == RefreshPolicy.CONFIG ? new RefreshStrategy(config.refreshInterval)
                        : null;

        getBridgeHandler().ifPresentOrElse(handler -> handler.registerDataListener(config.id, this),
                () -> apiBridge.addConnectionListener(this));
    }

    @Override
    public void apiConnectionStatusChanged(ConnectionStatus connectionStatus) {
        if (ConnectionStatus.SUCCESS.equals(connectionStatus)) {
            ThingStatus status = getThing().getStatus();
            if (status != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
                // Wait a little bit before refreshing because a dispose maybe running in parallel
                scheduler.schedule(() -> scheduleRefreshJob(), 3, TimeUnit.SECONDS);
            }
        } else {
            freeRefreshJob();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, connectionStatus.getMessage());
        }
    }

    @Override
    public void dispose() {
        logger.debug("Running dispose()");
        refreshStrategy = null;
        freeRefreshJob();
        apiBridge.removeConnectionListener(this);
        getBridgeHandler().ifPresent(handler -> handler.unregisterDataListener(this));
    }

    private void freeRefreshJob() {
        ScheduledFuture<?> job = refreshJob;
        if (job != null) {
            job.cancel(true);
        }
        refreshJob = null;
    }

    private void scheduleRefreshJob() {
        RefreshStrategy strategy = refreshStrategy;
        if (strategy != null) {
            long delay = strategy.nextRunDelay().toSeconds();
            logger.debug("Scheduling update channel thread in {} s", delay);
            updateChannels(false);
            freeRefreshJob();
            refreshJob = scheduler.schedule(() -> scheduleRefreshJob(), delay, TimeUnit.SECONDS);
        }
    }

    protected @Nullable NAThing updateReadings() throws NetatmoException {
        throw new NetatmoException("Should not be called");
    }

    private synchronized void updateChannels(boolean requireDefinedRefreshInterval) {
        RefreshStrategy strategy = refreshStrategy;
        if (strategy != null) {
            boolean dataOutdated = (requireDefinedRefreshInterval && strategy.isSearchingRefreshInterval()) ? false
                    : strategy.isDataOutdated();
            if (dataOutdated) {
                logger.debug("Trying to update channels on device {}", config.id);
                try {
                    NAThing newData = updateReadings();
                    if (newData != null) {
                        logger.debug("Successfully updated device {} readings! Now updating channels", config.id);
                        updateProperties(newData);
                        setNewData(newData);
                        newData.getLastSeen().ifPresent(ts -> strategy.setDataTimeStamp(ts));
                        updateChildModules(newData);
                    }
                } catch (NetatmoException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Unable to connect Netatmo API : " + e.getLocalizedMessage());
                }
            } else {
                logger.debug("Data still valid for device {}", config.id);
            }
        }
    }

    protected void updateChildModules(NAObject newData) {
        if (newData instanceof NADevice) {
            NADevice localNaDevice = (NADevice) newData;
            localNaDevice.getModules().entrySet().forEach(entry -> {
                DeviceHandler listener = getDataListeners().get(entry.getKey());
                if (listener != null) {
                    listener.setNewData(entry.getValue());
                }
            });
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing '{}'", channelUID);
            updateState(channelUID, getNAThingProperty(channelUID));
        }
    }

    public void setNewData(NAObject newData) {
        if (newData instanceof NAThing) {
            NAThing localNaThing = (NAThing) newData;
            if (localNaThing.isReachable()) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Device is not connected");
            }
            channelHelpers.forEach(helper -> helper.setNewData(localNaThing));
            getThing().getChannels().stream()
                    .filter(channel -> !ChannelKind.TRIGGER.equals(channel.getKind()) && isLinked(channel.getUID()))
                    .map(channel -> channel.getUID()).forEach(channelUID -> {
                        updateState(channelUID, getNAThingProperty(channelUID));
                    });
        }
    }

    private void updateProperties(NAThing naThing) {
        Map<String, String> properties = editProperties();
        int firmware = naThing.getFirmware();
        if (firmware != -1) {
            properties.put(Thing.PROPERTY_VENDOR, VENDOR);
            ModuleType modelId = naThing.getType();
            properties.put(Thing.PROPERTY_MODEL_ID, modelId.name());
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, Integer.toString(firmware));
        }
        PointType point = null;
        if (naThing instanceof NAHome) {
            point = ((NAHome) naThing).getLocation();
            NAPlace place = ((NAHome) naThing).getPlace();
            if (place != null) {
                properties.put(PROPERTY_CITY, place.getCity());
                properties.put(PROPERTY_COUNTRY, place.getCountry());
                properties.put(PROPERTY_TIMEZONE, place.getTimezone());
            }
        } else if (naThing instanceof NADevice) {
            NAPlace place = ((NADevice) naThing).getPlace();
            if (place != null) {
                point = place.getLocation();
            }
        }
        if (point != null) {
            properties.put(PROPERTY_LOCATION, point.toString());
        }
        updateProperties(properties);
    }

    private void expireData() {
        scheduler.schedule(() -> {
            RefreshStrategy strategy = refreshStrategy;
            if (strategy != null) {
                strategy.expireData();
            }
            scheduleRefreshJob();
        }, 2, TimeUnit.SECONDS);
    }

    protected void tryApiCall(Callable<Boolean> function) {
        try {
            function.call();
            expireData();
        } catch (Exception e) {
            logger.warn("Error calling api : {}", e.getMessage());
        }
    }

    private void registerDataListener(String id, DeviceHandler dataListener) {
        getDataListeners().put(id, dataListener);
        expireData();
    }

    private void unregisterDataListener(DeviceHandler dataListener) {
        getDataListeners().entrySet().removeIf(entry -> entry.getValue().equals(dataListener));
    }

    protected void updateIfLinked(String group, String channelName, State state) {
        ChannelUID channelUID = new ChannelUID(thing.getUID(), group, channelName);
        if (isLinked(channelUID)) {
            updateState(channelUID, state);
        }
    }

    private State getNAThingProperty(ChannelUID channelUID) {
        for (AbstractChannelHelper helper : channelHelpers) {
            State state = helper.getChannelState(channelUID);
            if (state != null) {
                return state;
            }
        }
        return UnDefType.UNDEF;
    }

    protected Optional<DeviceHandler> getBridgeHandler() {
        Bridge bridge = getBridge();
        return Optional.ofNullable(
                bridge != null && bridge.getHandler() instanceof DeviceHandler ? (DeviceHandler) bridge.getHandler()
                        : null);
    }

    protected Optional<HomeHandler> getHomeHandler() {
        return Optional.ofNullable(
                getBridgeHandler().filter(h -> h instanceof HomeHandler).map(HomeHandler.class::cast).orElse(null));
    }

    protected Map<String, DeviceHandler> getDataListeners() {
        return dataListeners;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singletonList(DeviceActions.class);
    }

    public void reconnectApi() {
        apiBridge.openConnection(null);
    }
}
