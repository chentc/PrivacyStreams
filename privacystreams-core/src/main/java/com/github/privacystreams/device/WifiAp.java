package com.github.privacystreams.device;


import android.net.wifi.ScanResult;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.github.privacystreams.core.Item;
import com.github.privacystreams.core.providers.MultiItemStreamProvider;
import com.github.privacystreams.utils.annotations.PSItem;
import com.github.privacystreams.utils.annotations.PSItemField;

/**
 * A WifiAp item represents the information of a WIFI AP.
 */
@PSItem
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
public class WifiAp extends Item {

    /**
     * The timestamp of when the WIFI AP information is found.
     */
    @PSItemField(type = Long.class)
    private static final String TIMESTAMP = "timestamp";

    /**
     * The BSSID.
     */
    @PSItemField(type = String.class)
    private static final String BSSID = "bssid";

    /**
     * The SSID.
     */
    @PSItemField(type = String.class)
    private static final String SSID = "ssid";

    /**
     * The frequency.
     */
    @PSItemField(type = String.class)
    private static final String FREQUENCY = "frequency";

    /**
     * The RSSI.
     */
    @PSItemField(type = String.class)
    private static final String RSSI = "rssi";

    /**
     * Whether this AP is connected.
     */
    @PSItemField(type = Boolean.class)
    private static final String CONNECTED = "connected";

    WifiAp(ScanResult scanResult, boolean connected) {
        this.setFieldValue(TIMESTAMP, scanResult.timestamp);
        this.setFieldValue(BSSID, scanResult.BSSID);
        this.setFieldValue(SSID, scanResult.SSID);
        this.setFieldValue(FREQUENCY, scanResult.frequency);
        this.setFieldValue(RSSI, scanResult.level);
        this.setFieldValue(CONNECTED, connected);
    }

    public static MultiItemStreamProvider asScanList() {
        return new WifiApListProvider();
    }

}