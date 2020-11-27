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
package org.openhab.binding.bluetooth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BluetoothCharacteristic} class defines the Bluetooth characteristic.
 * <p>
 * Characteristics are defined attribute types that contain a single logical value.
 * <p>
 * https://www.bluetooth.com/specifications/gatt/characteristics
 *
 * @author Chris Jackson - Initial contribution
 * @author Kai Kreuzer - Cleaned up code
 */
public class BluetoothCharacteristic {
    public static final int PROPERTY_BROADCAST = 0x01;
    public static final int PROPERTY_READ = 0x02;
    public static final int PROPERTY_WRITE_NO_RESPONSE = 0x04;
    public static final int PROPERTY_WRITE = 0x08;
    public static final int PROPERTY_NOTIFY = 0x10;
    public static final int PROPERTY_INDICATE = 0x20;
    public static final int PROPERTY_SIGNED_WRITE = 0x40;
    public static final int PROPERTY_EXTENDED_PROPS = 0x80;

    public static final int PERMISSION_READ = 0x01;
    public static final int PERMISSION_READ_ENCRYPTED = 0x02;
    public static final int PERMISSION_READ_ENCRYPTED_MITM = 0x04;
    public static final int PERMISSION_WRITE = 0x10;
    public static final int PERMISSION_WRITE_ENCRYPTED = 0x20;
    public static final int PERMISSION_WRITE_ENCRYPTED_MITM = 0x40;
    public static final int PERMISSION_WRITE_SIGNED = 0x80;
    public static final int PERMISSION_WRITE_SIGNED_MITM = 0x100;

    public static final int WRITE_TYPE_DEFAULT = 0x02;
    public static final int WRITE_TYPE_NO_RESPONSE = 0x01;
    public static final int WRITE_TYPE_SIGNED = 0x04;

    private final Logger logger = LoggerFactory.getLogger(BluetoothCharacteristic.class);

    /**
     * The {@link UUID} for this characteristic
     */
    protected UUID uuid;

    /**
     * The handle for this characteristic
     */
    protected int handle;

    /**
     * A map of {@link BluetoothDescriptor}s applicable to this characteristic
     */
    protected Map<UUID, BluetoothDescriptor> gattDescriptors = new HashMap<>();
    protected int instance;
    protected int properties;
    protected int permissions;
    protected int writeType;

    /**
     * The {@link BluetoothService} to which this characteristic belongs
     */
    protected BluetoothService service;

    /**
     * Create a new BluetoothCharacteristic.
     *
     * @param uuid the {@link UUID} of the new characteristic
     * @param handle
     */
    public BluetoothCharacteristic(UUID uuid, int handle) {
        this.uuid = uuid;
        this.handle = handle;
    }

    /**
     * Adds a descriptor to this characteristic.
     *
     * @param descriptor {@link BluetoothDescriptor} to be added to this characteristic.
     * @return true, if the descriptor was added to the characteristic
     */
    public boolean addDescriptor(BluetoothDescriptor descriptor) {
        if (gattDescriptors.get(descriptor.getUuid()) != null) {
            return false;
        }

        gattDescriptors.put(descriptor.getUuid(), descriptor);
        return true;
    }

    /**
     * Returns the {@link UUID} of this characteristic
     *
     * @return UUID of this characteristic
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Returns the instance ID for this characteristic.
     *
     * If a remote device offers multiple characteristics with the same UUID, the instance ID is used to distinguish
     * between characteristics.
     *
     * @return Instance ID of this characteristic
     */
    public int getInstanceId() {
        return instance;
    }

    /**
     * Returns the properties of this characteristic.
     *
     * The properties contain a bit mask of property flags indicating the features of this characteristic.
     *
     */
    public int getProperties() {
        return properties;
    }

    /**
     * Returns the permissions for this characteristic.
     */
    public int getPermissions() {
        return permissions;
    }

    /**
     * Gets the write type for this characteristic.
     *
     */
    public int getWriteType() {
        return writeType;
    }

    /**
     * Set the write type for this characteristic
     *
     * @param writeType
     */
    public void setWriteType(int writeType) {
        this.writeType = writeType;
    }

    /**
     * Get the service to which this characteristic belongs
     *
     * @return the {@link BluetoothService}
     */
    public BluetoothService getService() {
        return service;
    }

    /**
     * Returns the handle for this characteristic
     *
     * @return the handle for the characteristic
     */
    public int getHandle() {
        return handle;
    }

    /**
     * Get the service to which this characteristic belongs
     *
     * @return the {@link BluetoothService}
     */
    public void setService(BluetoothService service) {
        this.service = service;
    }

    /**
     * Returns a list of descriptors for this characteristic.
     *
     */
    public List<BluetoothDescriptor> getDescriptors() {
        return new ArrayList<>(gattDescriptors.values());
    }

    /**
     * Returns a descriptor with a given UUID out of the list of
     * descriptors for this characteristic.
     *
     * @return the {@link BluetoothDescriptor}
     */
    public BluetoothDescriptor getDescriptor(UUID uuid) {
        return gattDescriptors.get(uuid);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + instance;
        result = prime * result + ((service == null) ? 0 : service.hashCode());
        result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BluetoothCharacteristic other = (BluetoothCharacteristic) obj;
        if (instance != other.instance) {
            return false;
        }
        if (service == null) {
            if (other.service != null) {
                return false;
            }
        } else if (!service.equals(other.service)) {
            return false;
        }
        if (uuid == null) {
            if (other.uuid != null) {
                return false;
            }
        } else if (!uuid.equals(other.uuid)) {
            return false;
        }
        return true;
    }

    public GattCharacteristic getGattCharacteristic() {
        return GattCharacteristic.getCharacteristic(uuid);
    }

    public enum GattCharacteristic {
        // Characteristic
        ALERT_CATEGORY_ID(0x2A43),
        ALERT_CATEGORY_ID_BIT_MASK(0x2A42),
        ALERT_LEVEL(0x2A06),
        ALERT_NOTIFICATION_CONTROL_POINT(0x2A44),
        ALERT_STATUS(0x2A3F),
        APPEARANCE(0x2A01),
        BATTERY_LEVEL(0x2A19),
        BLOOD_PRESSURE_FEATURE(0x2A49),
        BLOOD_PRESSURE_MEASUREMENT(0x2A35),
        BODY_SENSOR_LOCATION(0x2A38),
        BOOT_KEYOBARD_INPUT_REPORT(0x2A22),
        BOOT_KEYOBARD_OUTPUT_REPORT(0x2A32),
        BOOT_MOUSE_INPUT_REPORT(0x2A33),
        CSC_FEATURE(0x2A5C),
        CSC_MEASUREMENT(0x2A5B),
        CURRENT_TIME(0x2A2B),
        CYCLING_POWER_CONTROL_POINT(0x2A66),
        CYCLING_POWER_FEATURE(0x2A65),
        CYCLING_POWER_MEASUREMENT(0x2A63),
        CYCLING_POWER_VECTOR(0x2A64),
        DATE_TIME(0x2A08),
        DAY_DATE_TIME(0x2A0A),
        DAY_OF_WEEK(0x2A09),
        DEVICE_NAME(0x2A00),
        DST_OFFSET(0x2A0D),
        EXACT_TIME_256(0x2A0C),
        FIRMWARE_REVISION_STRING(0x2A26),
        GLUCOSE_FEATURE(0x2A51),
        GLUCOSE_MEASUREMENT(0x2A18),
        GLUCOSE_MEASUREMENT_CONTROL(0x2A34),
        HARDWARE_REVISION_STRING(0x2A27),
        HEART_RATE_CONTROL_POINT(0x2A39),
        HEART_RATE_MEASUREMENT(0x2A37),
        HID_CONTROL_POINT(0x2A4C),
        HID_INFORMATION(0x2A4A),
        IEEE11073_20601_REGULATORY_CERTIFICATION_DATA_LIST(0x2A2A),
        INTERMEDIATE_CUFF_PRESSURE(0x2A36),
        INTERMEDIATE_TEMPERATURE(0x2A1E),
        LN_CONTROL_POINT(0x2A6B),
        LN_FEATURE(0x2A6A),
        LOCAL_TIME_INFORMATION(0x2A0F),
        LOCATION_AND_SPEED(0x2A67),
        MANUFACTURER_NAME_STRING(0x2A29),
        MEASUREMENT_INTERVAL(0x2A21),
        MODEL_NUMBER_STRING(0x2A24),
        NAVIGATION(0x2A68),
        NEW_ALERT(0x2A46),
        PERIPERAL_PREFFERED_CONNECTION_PARAMETERS(0x2A04),
        PERIPHERAL_PRIVACY_FLAG(0x2A02),
        PN_PID(0x2A50),
        POSITION_QUALITY(0x2A69),
        PROTOCOL_MODE(0x2A4E),
        RECONNECTION_ADDRESS(0x2A03),
        RECORD_ACCESS_CONTROL_POINT(0x2A52),
        REFERENCE_TIME_INFORMATION(0x2A14),
        REPORT(0x2A4D),
        REPORT_MAP(0x2A4B),
        RINGER_CONTROL_POINT(0x2A40),
        RINGER_SETTING(0x2A41),
        RSC_FEATURE(0x2A54),
        RSC_MEASUREMENT(0x2A53),
        SC_CONTROL_POINT(0x2A55),
        SCAN_INTERVAL_WINDOW(0x2A4F),
        SCAN_REFRESH(0x2A31),
        SENSOR_LOCATION(0x2A5D),
        SERIAL_NUMBER_STRING(0x2A25),
        SERVICE_CHANGED(0x2A05),
        SOFTWARE_REVISION_STRING(0x2A28),
        SUPPORTED_NEW_ALERT_CATEGORY(0x2A47),
        SUPPORTED_UNREAD_ALERT_CATEGORY(0x2A48),
        SYSTEM_ID(0x2A23),
        TEMPERATURE_MEASUREMENT(0x2A1C),
        TEMPERATURE_TYPE(0x2A1D),
        TIME_ACCURACY(0x2A12),
        TIME_SOURCE(0x2A13),
        TIME_UPDATE_CONTROL_POINT(0x2A16),
        TIME_UPDATE_STATE(0x2A17),
        TIME_WITH_DST(0x2A11),
        TIME_ZONE(0x2A0E),
        TX_POWER_LEVEL(0x2A07),
        UNREAD_ALERT_STATUS(0x2A45),
        AGGREGATE_INPUT(0x2A5A),
        ANALOG_INPUT(0x2A58),
        ANALOG_OUTPUT(0x2A59),
        DIGITAL_INPUT(0x2A56),
        DIGITAL_OUTPUT(0x2A57),
        EXACT_TIME_100(0x2A0B),
        NETWORK_AVAILABILITY(0x2A3E),
        SCIENTIFIC_TEMPERATURE_IN_CELSIUS(0x2A3C),
        SECONDARY_TIME_ZONE(0x2A10),
        STRING(0x2A3D),
        TEMPERATURE_IN_CELSIUS(0x2A1F),
        TEMPERATURE_IN_FAHRENHEIT(0x2A20),
        TIME_BROADCAST(0x2A15),
        BATTERY_LEVEL_STATE(0x2A1B),
        BATTERY_POWER_STATE(0x2A1A),
        PULSE_OXIMETRY_CONTINUOUS_MEASUREMENT(0x2A5F),
        PULSE_OXIMETRY_CONTROL_POINT(0x2A62),
        PULSE_OXIMETRY_FEATURES(0x2A61),
        PULSE_OXIMETRY_PULSATILE_EVENT(0x2A60),
        PULSE_OXIMETRY_SPOT_CHECK_MEASUREMENT(0x2A5E),
        RECORD_ACCESS_CONTROL_POINT_TESTVERSION(0x2A52),
        REMOVABLE(0x2A3A),
        SERVICE_REQUIRED(0x2A3B);

        private static Map<UUID, GattCharacteristic> uuidToServiceMapping;

        private UUID uuid;

        private GattCharacteristic(long key) {
            this.uuid = BluetoothBindingConstants.createBluetoothUUID(key);
        }

        private static void initMapping() {
            uuidToServiceMapping = new HashMap<>();
            for (GattCharacteristic s : values()) {
                uuidToServiceMapping.put(s.uuid, s);
            }
        }

        public static GattCharacteristic getCharacteristic(UUID uuid) {
            if (uuidToServiceMapping == null) {
                initMapping();
            }
            return uuidToServiceMapping.get(uuid);
        }

        /**
         * @return the key
         */
        public UUID getUUID() {
            return uuid;
        }
    }
}
