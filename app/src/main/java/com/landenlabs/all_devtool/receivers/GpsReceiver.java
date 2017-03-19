package com.landenlabs.all_devtool.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.landenlabs.all_devtool.util.LLog;

/**
 * Created by Dennis Lang on 6/19/2015.
 */
public class GpsReceiver  extends BroadcastReceiver {
    // Logger - set to LLog.DBG to only log in Debug build, use LLog.On for always log.
    private final LLog m_log = LLog.DBG;

    /**
     * Broadcast intent action indicating that the GPS has either been
     * enabled or disabled. An intent extra provides this state as a boolean,
     * where {@code true} means enabled.
     */
    public static final String GPS_ENABLED_CHANGE_ACTION =
            "android.location.GPS_ENABLED_CHANGE";

    /**
     * Broadcast intent action indicating that the GPS has either started or
     * stopped receiving GPS fixes. An intent extra provides this state as a
     * boolean, where {@code true} means that the GPS is actively receiving fixes.
     */
    public static final String GPS_FIX_CHANGE_ACTION = "android.location.GPS_FIX_CHANGE";

    public GpsReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            Toast.makeText(context, intent.toString(), Toast.LENGTH_LONG).show();
            m_log.i("GpsReceiver " + intent.toString());
        }
    }
}
