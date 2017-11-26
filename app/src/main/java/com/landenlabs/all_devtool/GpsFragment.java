package com.landenlabs.all_devtool;

/*
 * Copyright (c) 2016 Dennis Lang (LanDen Labs) landenlabs@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * @author Dennis Lang  (3/21/2015)
 * @see http://LanDenLabs.com/
 *
 */


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.landenlabs.all_devtool.receivers.GpsReceiver;
import com.landenlabs.all_devtool.util.LLog;
import com.landenlabs.all_devtool.util.Ui;
import com.landenlabs.all_devtool.util.Utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Display "Gps"  information.
 *
 * @author Dennis Lang
 *
 */
public class GpsFragment extends DevFragment implements
        View.OnClickListener,
        LocationListener,
        GpsStatus.Listener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // Logger - set to LLog.DBG to only log in Debug build, use LLog.On to always log.
    private final LLog m_log = LLog.DBG;

    public static String s_name = "GPS";
    SubMenu m_menu;

    static final int s_providersRow = 0;
    static final int s_lastUpdateRow = 1;
    static final int s_detailRow = 2;
    static final int s_maxRows = 3;

    private final ArrayList<GpsInfo> m_list = new ArrayList<GpsInfo>(s_maxRows);
    private final ItemList m_detailList = new ItemList();
    private ExpandableListView m_listView;
    private ImageView m_statusIcon;

    // Additional times
    private static final Locale s_locale = Locale.getDefault();
    private static final SimpleDateFormat s_hour12Format = new SimpleDateFormat("hh:mm:ss a", s_locale);
    private static final SimpleDateFormat s_hour24Format = new SimpleDateFormat("HH:mm:ss.S", s_locale);
    private static final SimpleDateFormat s_time12Format = new SimpleDateFormat("MMM-dd hh:mm a", s_locale);
    private static final SimpleDateFormat s_time24Format = new SimpleDateFormat("MMM-dd HH:mm", s_locale);

    private static SimpleDateFormat s_hourFormat = s_hour24Format;


    static final int s_colorGps = 0xff80ffff;   // must match Status toggle
    static final int s_colorMsg = s_colorGps;
    static final int s_maxToKeep = 20;
    static final long s_noTime = -1;

    // ---- GPS ----
    // https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest
    private static final String TAG = "LocationActivity";
    // 1hr = power blame once per hour
    private static final long GPS_INTERVAL = 1000 * 60 * 60;

    // Update no faster then GPS_FASTEST_INTERVAL if GPS updates for other reasons or hits
    // the GPS_INTERVAL interval
    private static final long GPS_FASTEST_INTERVAL = 1000 * 1;      // 1 sec
    private static final long GPS_SLOW_INTERVAL = 1000 * 60 * 5;    // 5 min

    LocationManager m_locMgr;
    LocationRequest m_locationRequest;
    GoogleApiClient m_googleApiClient;

    class LocInfo {
        Location m_currLocation;
        Location m_prevLocation;
    }

    static final String STATUS_CB = "Status";
    static final String FUSED_PROVIDER = "fused";
    Map<String, LocInfo> m_mapLocProviders = new HashMap<String, LocInfo>();

    TextView m_gpsTv;
    boolean m_gpsMonitor = false;

    Map<String, CheckBox> m_providersCb = new HashMap<String, CheckBox>();
    SparseArray<CheckBox> m_colorsCb = new SparseArray<CheckBox>();
    Map<String, GpsItem> m_lastUpdates = new HashMap<String, GpsItem>();

    boolean m_isGpsFixed = false;
    boolean m_isGpsEnabled = false;
    IntentFilter m_intentFilter = new IntentFilter();
    private BroadcastReceiver m_gpsReceiver = new BroadcastReceiver() {
        public void onReceive(Context paramContext, Intent paramIntent) {
            String str = paramIntent.getAction();
            Log.d("foo", "gps receiver action=" + str + paramIntent);
            // if ((str.equals("android.location.GPS_ENABLED_CHANGE")) || (str.equals("android.location.GPS_FIX_CHANGE")))
            GpsFragment.this.updateGps(paramIntent);
        }
    };

    private void updateGps(Intent paramIntent) {
        String str = paramIntent.getAction();
        boolean isEnabled = paramIntent.getBooleanExtra("enabled", false);
        if ((str.equals("android.location.GPS_FIX_CHANGE"))) {
            m_isGpsFixed = isEnabled;
            addMsgToDetailRow(s_colorGps, "Gps " + (m_isGpsFixed ? "fixed" : "unfixed"));
        } else if ((str.equals("android.location.GPS_ENABLED_CHANGE"))) {
            m_isGpsEnabled = isEnabled;
            addMsgToDetailRow(s_colorGps, "Gps " + (m_isGpsEnabled ? "enabled" : "disabled"));
        }
        showProviders();
    }

    // ============================================================================================
    // DevFragment methods
    public static DevFragment create() {
        return new GpsFragment();
    }

    @Override
    public String getName() {
        return s_name;
    }

    @Override
    public List<Bitmap> getBitmaps(int maxHeight) {
        // List<Bitmap> bitmapList = new ArrayList<Bitmap>();
        // bitmapList.add(Utils.grabScreen(this.getActivity()));
        // return bitmapList;
        return Utils.getListViewAsBitmaps(m_listView, maxHeight);
    }

    @Override
    public List<String> getListAsCsv() {
        return Utils.getListViewAsCSV(m_listView);
    }

    @Override
    public void onSelected() {
        GlobalInfo.s_globalInfo.mainFragActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        GlobalInfo.s_globalInfo.mainFragActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // ============================================================================================
    // Fragment methods
    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        View rootView = inflater.inflate(R.layout.gps_tab, container, false);
        m_statusIcon = Ui.viewById(rootView, R.id.gpsStatus);
        m_statusIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });

        Ui.viewById(rootView, R.id.gps_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearList();
            }
        });

        m_list.clear();
        // ---- Get UI components  ----
        for (int idx = 0; idx != s_maxRows; idx++)
            m_list.add(null);
        m_list.set(s_providersRow, new GpsInfo(new GpsItem("Providers")));
        m_list.set(s_lastUpdateRow, new GpsInfo(new GpsItem("Last Update")));
        m_list.set(s_detailRow, new GpsInfo(new GpsItem("Detail History")));

        m_listView = Ui.viewById(rootView, R.id.gpsListView);
        final GpsArrayAdapter adapter = new GpsArrayAdapter(this.getActivity());
        m_listView.setAdapter(adapter);

        // ---- Setup GPS ----
        m_locMgr = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        m_gpsTv = Ui.viewById(rootView, R.id.gps);
        if (isGooglePlayServicesAvailable()) {
            m_locationRequest = new LocationRequest();
            m_locationRequest.setInterval(GPS_INTERVAL);
            m_locationRequest.setFastestInterval(GPS_FASTEST_INTERVAL);
            // Priority needs to match permissions.
            //  Use LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY with
            // <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
            //  Use LocationRequest.PRIORITY_HIGH_ACCURACY with
            // <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
            m_locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

            // google_app_id
            m_googleApiClient = new GoogleApiClient.Builder(this.getActivity())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            m_gpsTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    m_gpsMonitor = !m_gpsMonitor;
                    view.setSelected(m_gpsMonitor);
                    addMsgToDetailRow(s_colorMsg, m_gpsMonitor ? "Start Monitor" : "Stop Monitor");
                    checkPermissions(Manifest.permission.ACCESS_FINE_LOCATION);
                    showProviders();
                }
            });

        } else {
            m_gpsTv.setText("Need Google Play Service");
        }

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            m_locMgr.addGpsStatusListener(this);
        }

        /*  http://stackoverflow.com/questions/11398732/how-do-i-receive-the-system-broadcast-when-gps-status-has-changed
            <uses-permission android:name="android.permission.ACCESS_COARSE_UPDATES" />
            <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
            <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
            <receiver android:name=".GpsReceiver">
            <intent-filter>
                 <action android:name="android.location.GPS_ENABLED_CHANGE" />
                 <action android:name="android.location.PROVIDERS_CHANGED" />
            </intent-filter>
            </receiver>
        */
        // GpsReceiver m_gpsReceiver = new GpsReceiver();
        m_intentFilter.addAction(GpsReceiver.GPS_ENABLED_CHANGE_ACTION);
        if (Build.VERSION.SDK_INT >= 19) {
            m_intentFilter.addAction(LocationManager.MODE_CHANGED_ACTION);
        }
        m_intentFilter.addAction(GpsReceiver.GPS_FIX_CHANGE_ACTION);
        m_intentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);

        showProviders();
        // TODO - get available providers
        getCheckBox(rootView, R.id.gpsFuseCb, FUSED_PROVIDER);
        getCheckBox(rootView, R.id.gpsGPSCb, LocationManager.GPS_PROVIDER);
        getCheckBox(rootView, R.id.gpsNetwkCb, LocationManager.NETWORK_PROVIDER);
        getCheckBox(rootView, R.id.gpsLowPwrCb, LocationManager.PASSIVE_PROVIDER);
        getCheckBox(rootView, R.id.gpsStatusCb, STATUS_CB);

        for (CheckBox cb : m_providersCb.values()) {
            cb.setOnClickListener(this);
        }
        initLastUpdate();

        // Expand All
        for (int idx = 0; idx != s_maxRows; idx++)
            m_listView.expandGroup(idx);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        startLocationUpdates();
        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(
                m_gpsReceiver, m_intentFilter);
        showProviders();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
        LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(
                m_gpsReceiver);
    }

    @Override
    public void onStart() {
        super.onStart();
        m_googleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "disconnect");
        m_googleApiClient.disconnect();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int pos = -1;
        int id = item.getItemId();
        switch (id) {
            case R.id.clock_12:
                s_hourFormat = s_hour12Format;
                addMsgToDetailRow(s_colorMsg, "12h Clock");
                break;
            case R.id.clock_24:
                s_hourFormat = s_hour24Format;
                addMsgToDetailRow(s_colorMsg, "24h Clock");
                break;

            case R.id.gps_clear_list:
                clearList();
                break;

            case R.id.gps_fast_accurate:
                if (m_locationRequest != null) {
                    m_locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    m_locationRequest.setFastestInterval(GPS_FASTEST_INTERVAL);
                    m_locationRequest.setSmallestDisplacement(0);
                    addMsgToDetailRow(s_colorMsg, "1sec 0 miles HighPwr");
                    startLocationUpdates();
                }
                break;

            case R.id.gps_fast_accurate_one_mile:
                if (m_locationRequest != null) {
                    m_locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    m_locationRequest.setFastestInterval(GPS_FASTEST_INTERVAL);
                    m_locationRequest.setSmallestDisplacement(1609.0f);  // 1 mile = 1609 km
                    addMsgToDetailRow(s_colorMsg, "1sec 1 mile HighPwr");
                    startLocationUpdates();
                }
                break;

            case R.id.gps_fast_balanced:
                if (m_locationRequest != null) {
                    m_locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    m_locationRequest.setFastestInterval(GPS_FASTEST_INTERVAL);
                    m_locationRequest.setSmallestDisplacement(0);
                    addMsgToDetailRow(s_colorMsg, "1sec 0 miles Balanced");
                    startLocationUpdates();
                }
                break;
            case R.id.gps_fast_balanced_one_mile:
                if (m_locationRequest != null) {
                    m_locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    m_locationRequest.setFastestInterval(GPS_FASTEST_INTERVAL);
                    m_locationRequest.setSmallestDisplacement(1609.0f);  // 1 mile = 1609 km
                    addMsgToDetailRow(s_colorMsg, "1sec 1 miles Balanced");
                    startLocationUpdates();
                }
                break;
            case R.id.gps_slow_balanced_one_mile:
                if (m_locationRequest != null) {
                    m_locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    m_locationRequest.setFastestInterval(GPS_SLOW_INTERVAL);
                    m_locationRequest.setSmallestDisplacement(1609.0f);  // 1 mile = 1609 km
                    addMsgToDetailRow(s_colorMsg, "5min 1 miles Balanced");
                    startLocationUpdates();
                }
                break;

            default:
                break;
        }

        item.setChecked(true);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        m_menu = menu.addSubMenu("GPS Settings");
        inflater.inflate(R.menu.gps_menu, m_menu);
        m_menu.findItem(R.id.clock_12).setChecked(true);
    }

    // ============================================================================================
    //  View.OnClickListener
    @Override
    public void onClick(View view) {
        updateDetailRows();
    }

    GpsStatus gpsStatus = null;

    // ============================================================================================
    //  GpsStatus.Listener
    @Override
    public void onGpsStatusChanged(int event) {
        if (getActivity() != null) {
            final LocationManager locMgr = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

            try {
                gpsStatus = locMgr.getGpsStatus(gpsStatus);
                String msg = "";
                switch (event) {
                    case GpsStatus.GPS_EVENT_STARTED:
                        msg = "GPS event started";
                        break;
                    case GpsStatus.GPS_EVENT_STOPPED:
                        msg = "GPS event stopped";
                        break;
                    case GpsStatus.GPS_EVENT_FIRST_FIX:
                        msg = "GPS first fix";
                        break;
                    case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                        msg = "GPS sat status";
                        break;
                }

                if (TextUtils.isEmpty(msg)) {
                    addMsgToDetailRow(s_colorGps, msg);
                    GpsItem gpsItem = m_lastUpdates.get(STATUS_CB);
                    if (gpsItem != null) {
                        gpsItem.set(System.currentTimeMillis(), msg);
                        listChanged();
                    }
                }
                showProviders();
            } catch (SecurityException ex) {
                Log.e(TAG, ex.getMessage());
            }
        }
    }

    // ============================================================================================
    // GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
        addMsgToDetailRow(s_colorMsg, "GPS Started");
    }

    @Override
    public void onConnectionSuspended(int i) {
        addMsgToDetailRow(s_colorMsg, "GPS suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Ui.ToastBig(this.getActivity(), "GPS error\n" + connectionResult.describeContents());
        addMsgToDetailRow(s_colorMsg, "GPS error");
    }

    // ============================================================================================
    // LocationListener,
    @Override
    public void onLocationChanged(Location location) {

        boolean isDupLoc = false;

        try {
            Location gpsLoc = m_locMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (gpsLoc != null) {
                showGPS(gpsLoc);
                isDupLoc = isDupLoc || (location.distanceTo(gpsLoc) == 0);
            }
        } catch (Exception ex) {
            Toast.makeText(this.getActivity(), "GPS " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }

        try {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                Location netLoc = m_locMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (netLoc != null) {
                    showGPS(netLoc);
                    isDupLoc = isDupLoc || (location.distanceTo(netLoc) == 0);
                }
            }
        } catch (Exception ex) {
            Toast.makeText(this.getActivity(), "GPS needs location permission\n" + ex.getMessage(), Toast.LENGTH_LONG).show();
        }

        try {
            Location passiveLoc = m_locMgr.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            if (null != passiveLoc) {
                showGPS(passiveLoc);
                isDupLoc = isDupLoc || (location.distanceTo(passiveLoc) == 0);
            }
        } catch (Exception ex) {
            Toast.makeText(this.getActivity(), "Passive " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }

        if (!isDupLoc)
            showGPS(location);
    }

    // ============================================================================================
    // GpsFragment
    private CheckBox getCheckBox(View rootView, int resId, String provider) {
        CheckBox cb = Ui.viewById(rootView, resId);
        m_providersCb.put(provider, cb);
        m_colorsCb.put(cb.getCurrentTextColor(), cb);
        return cb;
    }

    private int getProviderColor(String provider) {
        CheckBox cb = m_providersCb.get(provider);
        int color = (cb == null) ? s_colorMsg : cb.getCurrentTextColor();
        return color;
    }

    private void showProviders() {
        GpsInfo gpsInfo = m_list.get(s_providersRow);
        ItemList itemList = gpsInfo.getList();
        itemList.clear();

        List<String> gpsProviders = m_locMgr.getAllProviders();
        int idx = 1;
        for (String providerName : gpsProviders) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                try {
                    LocationProvider provider = m_locMgr.getProvider(providerName);
                    if (null != provider) {
                        int color = getProviderColor(providerName);
                        String msg = String.format("%-10s %3s Accuracy:%d Pwr:%d",
                                providerName,
                                (m_locMgr.isProviderEnabled(providerName) ? "On" : "Off"),
                                provider.getAccuracy(), provider.getPowerRequirement());
                        itemList.add(new GpsItem(s_noTime, msg, color));
                    }
                } catch (SecurityException ex) {
                    m_log.e(ex.getLocalizedMessage());
                    m_gpsTv.setEnabled(false);
                    addMsgToDetailRow(s_colorMsg, "GPS not available");
                    addMsgToDetailRow(s_colorMsg, ex.getLocalizedMessage());
                }
            }
        }
        listChanged();

        if (m_locMgr.isProviderEnabled(LocationManager.GPS_PROVIDER))
            m_statusIcon.setImageResource(R.drawable.gps_satellite);
        else if (m_locMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            m_statusIcon.setImageResource(R.drawable.gps_tower);
        else if (m_locMgr.isProviderEnabled(LocationManager.PASSIVE_PROVIDER))
            m_statusIcon.setImageResource(R.drawable.gps_passive);
        else
            m_statusIcon.setImageResource(R.drawable.gps_off);
    }

    private void initLastUpdate() {
        GpsInfo gpsInfo = m_list.get(s_lastUpdateRow);
        ItemList itemList = gpsInfo.getList();
        if (itemList.isEmpty()) {
            //          1234567 12345678 123456789 12345m
            //          hh:mm:ss pm 12.45678 123.56789 12345m network
            itemList.add(new GpsItem("Time Latitude Longitude Accuracy Provider"));

            for (String provider : m_providersCb.keySet()) {
                int color = getProviderColor(provider);
                GpsItem gpsItem = new GpsItem(s_noTime, provider, color);
                itemList.add(gpsItem);
                m_lastUpdates.put(provider, gpsItem);
            }
        }

        for (String provider : m_providersCb.keySet()) {
            boolean vis = m_providersCb.get(provider).isChecked();
            // m_lastUpdates.get(provider).setVisibility(vis ? View.VISIBLE : View.eE);
        }
    }

    private void startLocationUpdates() {
        if (m_googleApiClient.isConnected()) {
            stopLocationUpdates();

            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                        m_googleApiClient, m_locationRequest, this);
            }
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(m_googleApiClient, this);
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getActivity());
        if (ConnectionResult.SUCCESS == status) {
            addMsgToDetailRow(s_colorMsg, "GooglePlay Available");
            return true;
        } else {
            String errMsg = GooglePlayServicesUtil.getErrorString(status);
            Ui.ShowMessage(this.getActivity(), errMsg);
            addMsgToDetailRow(s_colorMsg, "GooglePlay Unavailable");
            return false;
        }
    }

    private void clearList() {
        GpsInfo gpsInfo = m_list.get(s_detailRow);
        gpsInfo.getList().clear();
        listChanged();
    }

    private void showGPS(Location location) {
        if (location == null)
            return;

        String provider = location.getProvider();
        LocInfo locInfo = m_mapLocProviders.get(provider);
        if (locInfo == null) {
            locInfo = new LocInfo();
            m_mapLocProviders.put(provider, locInfo);
        }

        locInfo.m_prevLocation = locInfo.m_currLocation;
        locInfo.m_currLocation = location;

        if (null != location) {
            addLocToDetailRow(locInfo);

            String msg = String.format("%8.6f,%9.6f %5.0fm %s",
                    location.getLatitude(), location.getLongitude(),
                    location.getAccuracy(), location.getProvider());
            GpsItem gpsItem = m_lastUpdates.get(provider);
            if (gpsItem != null) {
                gpsItem.set(location.getTime(), msg);
                listChanged();
            }  else
                Ui.ShowMessage(this.getActivity(), "null text for " + provider);    // DEBUG

        } else {
            m_gpsTv.setText("No location");
        }
    }

    /**
     * Get address by Geocoder using play services.
     */
    public static Address getGeocoderAddress(Context context, double latitude, double longitude) {
        Geocoder gc = new Geocoder(context, Locale.getDefault());
        int failedAttempts = 0;
        for (;;) {
            try {
                List<Address> res = gc.getFromLocation(latitude, longitude, 1);
                return ((res != null) && !res.isEmpty()) ? res.get(0) : null;
            } catch (IOException ioe) {
                if (++failedAttempts > 3) {
                    Log.e(TAG, "getGeocoderAddress failed ", ioe);
                    return null;
                }
            }
        }
    }


    private int addLocToDetailRow(LocInfo locInfo) {
       
        if (m_detailList.size() > s_maxToKeep) {
            m_detailList.remove(0);
        }

        Location prevLoc = locInfo.m_prevLocation;
        Location currLoc = locInfo.m_currLocation;
        double km = (prevLoc != null) ? Utils.kilometersBetweenLocations(prevLoc, currLoc) : 0;
        double feet = km * 3280.84;
        int color = m_providersCb.get(currLoc.getProvider()).getCurrentTextColor();

        String msg = String.format("%.7f,%.7f  %.0fm %.0ft %s",
                currLoc.getLatitude(), currLoc.getLongitude(), km * 1000, feet, currLoc.getProvider());

        Address address = getGeocoderAddress(getContext(), currLoc.getLatitude(), currLoc.getLongitude());
        if (address != null) {
            if (address.getMaxAddressLineIndex() >= 0) {
                for (int idx = 0; idx <= address.getMaxAddressLineIndex(); idx++) {
                    msg += "\n   " + address.getAddressLine(idx);
                }
            }
            msg += "\n  ";
            msg += " " + address.getLocality();
            msg += " " + address.getAdminArea();
            msg += " " + address.getCountryName();
        } else {
            msg += "\n  Geocoder not available, reboot";
        }

        m_log.i("GPS "+ msg);
        GpsItem item = new GpsItem(currLoc.getTime(), msg, color);

        return updateDetailRow(item);
    }

    private int addMsgToDetailRow(int textColor, String msg) {

        if (textColor == s_colorMsg) {
            GpsItem gpsItem = m_lastUpdates.get(STATUS_CB);
            if (gpsItem != null) {
                gpsItem.set(System.currentTimeMillis(), msg);
                listChanged();
            }
        }

        if (m_detailList.size() > s_maxToKeep) {
            m_detailList.remove(0);
        }

        Date now = new Date();
        GpsItem item = new GpsItem(now.getTime(), msg, textColor);
        return updateDetailRow(item);
    }

    /**
     * Add filtered item to detail row.
     * @param item
     * @return
     */
    private int updateDetailRow(GpsItem item) {
        boolean isDup = false;
        int detailCnt = m_detailList.size();
        if (detailCnt > 0) {
            GpsItem prevItem = m_detailList.get(detailCnt-1);
            if (prevItem.m_msg.equals(item.m_msg)
                    && (item.m_time - prevItem.m_time) < 1000) {
                isDup = true;
            }
        }

        if (!isDup) {
            m_detailList.add(item);
        }
        
        GpsInfo gpsInfo = m_list.get(s_detailRow);
        ItemList itemList = gpsInfo.getList();
        CheckBox cb = m_colorsCb.get(item.m_color);
        // if (m_gpsMonitor && (cb == null || cb.isChecked())) {
            if (!isDup) {
                itemList.add(item);
                listChanged();
            }
        // }
        
        return itemList.size();
    }

    /**
     * Refill detail row with filtered items.
     * @return
     */
    private int updateDetailRows() {
        GpsInfo gpsInfo = m_list.get(s_detailRow);
        ItemList itemList = gpsInfo.getList();
        itemList.clear();
        
        for (GpsItem item : m_detailList) {
            CheckBox cb = m_colorsCb.get(item.m_color);
            if (m_gpsMonitor && (cb == null || cb.isChecked())) {
                itemList.add(item);
            }
        }
        listChanged();
        return itemList.size();
    }

    // ============================================================================================
    // Static Helper methods

    /**
     * Format time interval
     *
     * @param elapsedMillis
     * @return
     */
    private static String formatInterval(final long elapsedMillis) {
        final long day = TimeUnit.MICROSECONDS.toHours(elapsedMillis) / 24;
        final long hr = TimeUnit.MILLISECONDS.toHours(elapsedMillis) % 24;
        final long min = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis) % 60;
        final long sec = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) % 60;
        final long ms = TimeUnit.MILLISECONDS.toMillis(elapsedMillis) % 1000;
        return String.format("%s %02d:%02d:%02d.%03d", (day == 0 ? "" : String.valueOf(day)), hr, min, sec, ms);
    }

    // ============================================================================================
    // Expanded List Adapter

    private void listChanged() {
        ((BaseExpandableListAdapter) m_listView.getExpandableListAdapter()).notifyDataSetChanged();
    }

    static class GpsItem {
        static final int s_white = 0xffffffff;
        long m_time;
        String m_msg;
        int m_color;

        public GpsItem() {
            m_time = s_noTime;
            m_msg = null;
            m_color = s_white;
        }

        public GpsItem(String msg) {
            m_time = s_noTime;
            m_msg = msg;
            m_color = s_white;
        }
        public GpsItem(String msg, int color) {
            m_time = s_noTime;
            m_msg = msg;
            m_color = color;
        }
        public GpsItem(long time, String msg, int color) {
            m_time = time;
            m_msg = msg;
            m_color = color;
        }

        public void set(long time, String msg) {
            m_time = time;
            m_msg = msg;
        }
    }

    class ItemList extends ArrayList<GpsItem> {
    }

    class GpsInfo {
        GpsItem m_item;
        ItemList m_itemList;

        GpsInfo() {
            m_item = null;
            m_itemList = null;
        }

        GpsInfo(GpsItem item) {
            m_item = item;
            m_itemList = new ItemList();
        }

        public String toString() {
            return m_item.m_msg;
        }

        public GpsItem getItem() {
            return m_item;
        }
        public ItemList getList() {
            return m_itemList;
        }
        public int getCount() {
            return (m_itemList == null) ? 0 : m_itemList.size();
        }

    }

    final static int EXPANDED_LAYOUT = R.layout.gps_list_detail_row;
    final static int SUMMARY_LAYOUT = R.layout.gps_list_group_row;

    /**
     * ExpandableLis UI 'data model' class
     */
    private class GpsArrayAdapter extends BaseExpandableListAdapter {
        private final LayoutInflater m_inflater;

        public GpsArrayAdapter(Context context) {
            m_inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        /**
         * Generated expanded detail view object.
         */
        @Override
        public View getChildView(
                final int groupPosition,
                final int childPosition,
                boolean isLastChild,
                View convertView,
                ViewGroup parent) {

            GpsInfo gpsInfoParent = m_list.get(groupPosition);
            GpsItem gpsInfoChild = gpsInfoParent.getList().get(childPosition);

            RelativeLayout expandView = (RelativeLayout)convertView;
            // if (null == expandView) {
            expandView = (RelativeLayout)m_inflater.inflate(EXPANDED_LAYOUT, parent, false);
            // }

            long time = gpsInfoChild.m_time;
            String timeStr = (time != s_noTime) ? s_hourFormat.format(time) + " " : "";
            String msg = timeStr + gpsInfoChild.m_msg;

            TextView textView = Ui.viewById(expandView, R.id.gpsColumn);
            textView.setText(msg);
            textView.setTextColor(gpsInfoChild.m_color);

            CheckBox cb = m_colorsCb.get(gpsInfoChild.m_color);
/*
            if (cb != null && !cb.isChecked()) {
                ViewGroup.LayoutParams lp = expandView.getLayoutParams();
                lp.height = 0;
                expandView.setLayoutParams(lp);
                expandView.setVisibility(View.GONE);
            }
*/
            return expandView;
        }

        @Override
        public int getGroupCount() {
            return m_list.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return m_list.get(groupPosition).getCount();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return null;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return null;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        /**
         * Generate summary (row) presentation view object.
         */
        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {

            View summaryView = convertView;
            if (null == summaryView) {
                summaryView = m_inflater.inflate(SUMMARY_LAYOUT, parent, false);
            }

            GpsInfo gpsInfo = m_list.get(groupPosition);
            if (gpsInfo != null) {
                GpsItem gpsItem = gpsInfo.getItem();

                long time = gpsItem.m_time;
                String timeStr = (time != s_noTime) ? s_hourFormat.format(time) + " " : "";
                String msg = timeStr + gpsItem.m_msg;

                TextView textView = Ui.viewById(summaryView, R.id.gpsColumn);
                textView.setText(msg);
                textView.setTextColor(gpsItem.m_color);
            }

            return summaryView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}