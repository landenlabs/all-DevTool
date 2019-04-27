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
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.ProcessErrorStateInfo;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.ConfigurationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.UserManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.os.ConfigurationCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.CaptioningManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.landenlabs.all_devtool.util.LLog;
import com.landenlabs.all_devtool.util.Ui;
import com.landenlabs.all_devtool.util.Utils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;


/**
 * Display system information.
 *
 * @author Dennis Lang
 */
@SuppressWarnings({"ConstantConditions", "ConstantIfStatement", "Convert2Lambda"})
public class SystemFragment extends DevFragment {
    final static int SUMMARY_LAYOUT = R.layout.build_list_row;
    private static final int MB = 1 << 20;
    public static String s_name = "System";
    private static int m_rowColor1 = 0;
    private static int m_rowColor2 = 0x80d0ffe0;
    private static SimpleDateFormat m_timeFormat = new SimpleDateFormat("HH:mm:ss zz");
    final ArrayList<BuildInfo> m_list = new ArrayList<>();

    // Logger - set to LLog.DBG to only log in Debug build, use LLog.On for always log.
    private final LLog m_log = LLog.DBG;
    ExpandableListView m_listView;
    TextView m_titleTime;
    ImageButton m_search;
    View m_refresh;
    String m_filter;

    BuildArrayAdapter m_adapter;
    SubMenu m_menu;

    // ============================================================================================
    // DevFragment methods

    public SystemFragment() {
    }

    public static DevFragment create() {
        return new SystemFragment();
    }

    // Put values in List ifValue true.
    private static <M extends Map<E, E>, E> void putIf(M listObj, E v1, E v2, boolean ifValue) {
        if (ifValue) {
            listObj.put(v1, v2);
        }
    }

    @Override
    public String getName() {
        return s_name;
    }

    @Override
    public List<Bitmap> getBitmaps(int maxHeight) {
        return Utils.getListViewAsBitmaps(m_listView, maxHeight);
    }

    @Override
    public List<String> getListAsCsv() {
        return Utils.getListViewAsCSV(m_listView);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        View rootView = inflater.inflate(R.layout.build_tab, container, false);

        Ui.<TextView>viewById(rootView, R.id.list_title).setText(R.string.system_title);
        Ui.viewById(rootView, R.id.list_time_bar).setVisibility(View.VISIBLE);
        m_titleTime = Ui.viewById(rootView, R.id.list_time);

        m_search = Ui.viewById(rootView, R.id.list_search);
        m_search.setVisibility(View.VISIBLE);
        m_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_titleTime.setText("");
                m_titleTime.setHint("enter search text");
                InputMethodManager imm = (InputMethodManager) getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(m_titleTime, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        m_titleTime.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView edView, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = (InputMethodManager) getContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    // imm.showSoftInput(m_titleTime, InputMethodManager.HIDE_IMPLICIT_ONLY);
                    imm.toggleSoftInput(0, 0);

                    Toast.makeText(getContext(), "Searching...", Toast.LENGTH_SHORT).show();
                    m_filter = edView.getText().toString();
                    updateList();
                    return true; // consume.
                }
                return false; // pass on to other listeners.
            }
        });


        final ToggleButton expandTb = Ui.viewById(rootView, R.id.list_collapse_tb);
        expandTb.setVisibility(View.VISIBLE);
        expandTb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (expandTb.isChecked())
                    expandAll();
                else
                    collapseAll();
            }
        });


        m_refresh = Ui.viewById(rootView, R.id.list_refresh);
        m_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateList();
                m_listView.invalidateViews();
            }
        });


        m_listView = Ui.viewById(rootView, R.id.buildListView);
        m_adapter = new BuildArrayAdapter(this.getActivity());
        m_listView.setAdapter(m_adapter);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != m_listView) {
            updateList();
            m_listView.invalidateViews();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        m_menu = menu.addSubMenu("Sys Options");
        inflater.inflate(R.menu.sys_menu, m_menu);
    }

    public void updateList() {
        // Time today = new Time(Time.getCurrentTimezone());
        // today.setToNow();
        // today.format(" %H:%M:%S")
        Date dt = new Date();
        m_titleTime.setText(m_timeFormat.format(dt));

        boolean expandAll = m_list.isEmpty();
        m_list.clear();

        // Swap colors
        int color = m_rowColor1;
        m_rowColor1 = m_rowColor2;
        m_rowColor2 = color;

        ActivityManager actMgr =
                (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        String androidIDStr = Settings.Secure
                .getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        addBuild("Android ID", androidIDStr);
        ConfigurationInfo info = actMgr.getDeviceConfigurationInfo();
        addBuild("OpenGL", info.getGlEsVersion());

        try {
            try {
                AdvertisingIdClient.Info adInfo =
                        AdvertisingIdClient.getAdvertisingIdInfo(getContext());
                final String adIdStr = adInfo.getId();
                // final boolean isLAT = adInfo.isLimitAdTrackingEnabled();
                addBuild("Ad ID", adIdStr);
            } catch (IOException e) {
                // Unrecoverable error connecting to Google Play services (e.g.,
                // the old version of the service doesn't support getting AdvertisingId).
            } catch (GooglePlayServicesNotAvailableException e) {
                // Google Play services is not available entirely.
            }

            /*
            try {
                InstanceID instanceID = InstanceID.getInstance(getContext());
                if (instanceID != null) {
                    // Requires a Google Developer project ID.
                    String authorizedEntity = "<need to make this on google developer site>";
                    instanceID.getToken(authorizedEntity, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                    addBuild("Instance ID", instanceID.getId());
                }
            } catch (Exception ex) {
            }
            */
        } catch (Exception ex) {
            m_log.e(ex.getMessage());
        }

        try {
            long heapSize = Debug.getNativeHeapSize();
            // long maxHeap = Runtime.getRuntime().maxMemory();

            // ConfigurationInfo cfgInfo = actMgr.getDeviceConfigurationInfo();
            int largHeapMb = actMgr.getLargeMemoryClass();
            int heapMb = actMgr.getMemoryClass();

            MemoryInfo memInfo = new MemoryInfo();
            actMgr.getMemoryInfo(memInfo);

            final String sFmtMB = "%.2f MB";
            Map<String, String> listStr = new TreeMap<>();
            listStr.put("Mem Available (now)",
                    String.format(sFmtMB, (double) memInfo.availMem / MB));
            listStr.put("Mem LowWhenOnlyAvail",
                    String.format(sFmtMB, (double) memInfo.threshold / MB));
            listStr.put("Mem Installed", String.format(sFmtMB, (double) memInfo.totalMem / MB));
            listStr.put("Heap (this app)", String.format(sFmtMB, (double) heapSize / MB));
            listStr.put("HeapMax (default)", String.format(sFmtMB, (double) heapMb));
            listStr.put("HeapMax (large)", String.format(sFmtMB, (double) largHeapMb));
            addBuild("Memory...", listStr);
        } catch (Exception ex) {
            m_log.e(ex.getMessage());
        }

        try {
            List<ProcessErrorStateInfo> procErrList = actMgr.getProcessesInErrorState();
            int errCnt = (procErrList == null ? 0 : procErrList.size());

            // List<RunningAppProcessInfo> procList = actMgr.getRunningAppProcesses();
            int procCnt = actMgr.getRunningAppProcesses().size();
            int srvCnt = actMgr.getRunningServices(100).size();

            Map<String, String> listStr = new TreeMap<>();
            listStr.put("#Processes", String.valueOf(procCnt));
            listStr.put("#Proc With Err", String.valueOf(errCnt));
            listStr.put("#Services", String.valueOf(srvCnt));
            // Requires special permission
            //	int taskCnt = actMgr.getRunningTasks(100).size();
            //	listStr.put("#Tasks",  String.valueOf(taskCnt));
            addBuild("Processes...", listStr);
        } catch (Exception ex) {
            m_log.e("System-Processes %s", ex.getMessage());
        }

        try {
            Map<String, String> listStr = new LinkedHashMap<>();
            listStr.put("LargeIconDensity", String.valueOf(actMgr.getLauncherLargeIconDensity()));
            listStr.put("LargeIconSize", String.valueOf(actMgr.getLauncherLargeIconSize()));
            putIf(listStr, "isRunningInTestHarness", "Yes",
                    ActivityManager.isRunningInTestHarness());
            putIf(listStr, "isUserAMonkey", "Yes", ActivityManager.isUserAMonkey());
            addBuild("Misc...", listStr);
        } catch (Exception ex) {
            m_log.e("System-Misc %s", ex.getMessage());
        }

        // --------------- Locale / Timezone -------------
        try {
            Locale ourLocale = Locale.getDefault();
            Date m_date = new Date();
            TimeZone tz = TimeZone.getDefault();

            Map<String, String> localeListStr = new LinkedHashMap<>();

            localeListStr.put("Locale Name", ourLocale.getDisplayName());
            localeListStr.put(" Variant", ourLocale.getVariant());
            localeListStr.put(" Country", ourLocale.getCountry());
            localeListStr.put(" Country ISO", ourLocale.getISO3Country());
            localeListStr.put(" Language", ourLocale.getLanguage());
            localeListStr.put(" Language ISO", ourLocale.getISO3Language());
            localeListStr.put(" Language Dsp", ourLocale.getDisplayLanguage());

            localeListStr.put("TimeZoneID", tz.getID());
            localeListStr.put(" DayLightSavings", tz.useDaylightTime() ? "Yes" : "No");
            localeListStr.put(" In DLS", tz.inDaylightTime(m_date) ? "Yes" : "No");
            localeListStr.put(" Short Name", tz.getDisplayName(false, TimeZone.SHORT, ourLocale));
            localeListStr.put(" Long Name", tz.getDisplayName(false, TimeZone.LONG, ourLocale));

            Locale locale0 = ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0);
            if (locale0 != null
                && (!locale0.getLanguage().equals(ourLocale.getLanguage())
                || !locale0.getCountry().equals(ourLocale.getCountry()))) {
                localeListStr.put("Locale[0] Name", locale0.getDisplayName());
                localeListStr.put(" Country", locale0.getCountry());
                localeListStr.put(" Language", locale0.getLanguage());
            }

            addBuild("Locale TZ...", localeListStr);
        } catch (Exception ex) {
            m_log.e("Locale/TZ %s", ex.getMessage());
        }

        // --------------- Caption Manager ---------
        try {

            if (Build.VERSION.SDK_INT >= 19) {
                Map<String, String> listStr = new LinkedHashMap<>();
                final CaptioningManager capMgr = getServiceSafe(Context.CAPTIONING_SERVICE);
                listStr.put("Enabled", capMgr.isEnabled() ? "yes" : "no");
                listStr.put("Font Size", String.valueOf(capMgr.getFontScale()));
                listStr.put("Language", capMgr.getLocale().getLanguage());
                addBuild("Caption...", listStr);
            }
        } catch (Exception ignore) {

        }
        // --------------- Location Services -------------
        try {
            Map<String, String> listStr = new LinkedHashMap<>();

            final LocationManager locMgr = getServiceSafe(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                GpsStatus gpsStatus = locMgr.getGpsStatus(null);

                if (gpsStatus != null) {
                    listStr.put("Sec ToGetGPS", String.valueOf(gpsStatus.getTimeToFirstFix()));

                    Iterable<GpsSatellite> satellites = gpsStatus.getSatellites();
                    for (GpsSatellite satellite : satellites) {
                        putIf(listStr,
                                String.format("Azm:%.0f, Elev:%.0f", satellite.getAzimuth(),
                                        satellite.getElevation()),
                                String.format("%.2f Snr", satellite.getSnr()),
                                satellite.usedInFix());
                    }
                }

                Location location = locMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (null == location)
                    location = locMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (null == location)
                    location = locMgr.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

                if (null != location) {
                    listStr.put(location.getProvider() + " lat,lng",
                            String.format("%.3f, %.3f", location.getLatitude(),
                                    location.getLongitude()));
                }
            }

            if (listStr.size() != 0) {
                List<String> gpsProviders = locMgr.getAllProviders();
                for (String providerName : gpsProviders) {
                    LocationProvider provider = locMgr.getProvider(providerName);
                    if (null != provider) {
                        listStr.put(providerName, (locMgr.isProviderEnabled(providerName) ? "On " : "Off ") +
                                String.format("Accuracy:%d Pwr:%d", provider.getAccuracy(), provider.getPowerRequirement()));
                    }
                }
                addBuild("GPS...", listStr);
            } else
                addBuild("GPS", "Off");
        } catch (Exception ex) {
            m_log.e(ex.getMessage());
            addBuild("GPS", ex.getLocalizedMessage());
        }

        // --------------- Battery -------------
        if (Build.VERSION.SDK_INT >= 21) {
            BatteryManager mBatteryManager = getServiceSafe(Context.BATTERY_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                Integer avgCurrent = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE);
                Integer currentNow = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
            //    Integer capPer = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

                // Battery remaining energy in nanowatt-hours, as a long integer.
                // Long nanowattHours = mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER);

                Map<String, String> listStr = new LinkedHashMap<>();
                if (avgCurrent != 0) {
                    listStr.put("Current (avg)", String.format("%.3f mA", avgCurrent / 1e3));
                }
                listStr.put("Current (now)", String.format("%.3f mA", currentNow/1e3));
            //    listStr.put("Percent", String.format("%d%%", capPer.intValue()));
            //    listStr.put("Remain", String.format("%.4f Hours", nanowattHours/1e9));

                IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                Intent batteryStatus = getContext().registerReceiver(null, ifilter);

                // Are we charging / charged?
                int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL;

                int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
                boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
                String charging = isCharging ? "Yes" : "No";
                charging = (isCharging && usbCharge) ? "USB" : charging;
                charging = (isCharging && acCharge) ? "AC" : charging;
                listStr.put("Charging", charging);

                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                float batteryPct = level / (float)scale;
                listStr.put("Percent", String.format("%.1f%%", batteryPct*100));

                int voltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
                listStr.put("Voltage", String.format("%d mV", voltage));

                addBuild("Battery...", listStr);
            }
        }

        // --------------- Application Info -------------
        ApplicationInfo appInfo = getActivity().getApplicationInfo();
        if (null != appInfo) {
            Map<String, String> appList = new LinkedHashMap<>();
            try {
                appList.put("ProcName", appInfo.processName);
                appList.put("PkgName", appInfo.packageName);
                appList.put("DataDir", appInfo.dataDir);
                appList.put("SrcDir", appInfo.sourceDir);
                //    appList.put("PkgResDir", getActivity().getPackageResourcePath());
                //     appList.put("PkgCodeDir", getActivity().getPackageCodePath());
                String[] dbList = getActivity().databaseList();
                if (dbList != null && dbList.length != 0)
                    appList.put("DataBase", dbList[0]);
                // getActivity().getComponentName().

            } catch (Exception ignore) {
            }
            addBuild("AppInfo...", appList);
        }

        // --------------- Account Services -------------
        final AccountManager accMgr = (AccountManager) getActivity().getSystemService(Context.ACCOUNT_SERVICE);
        if (null != accMgr) {
            Map<String, String> strList = new LinkedHashMap<>();
            try {
                for (Account account : accMgr.getAccounts()) {
                    strList.put(account.name, account.type);
                }
            } catch (Exception ex) {
                m_log.e(ex.getMessage());
            }
            addBuild("Accounts...", strList);
        }

        // --------------- Package Features -------------
        PackageManager pm = getActivity().getPackageManager();
        FeatureInfo[] features = pm.getSystemAvailableFeatures();
        if (features != null) {
            Map<String, String> strList = new LinkedHashMap<>();
            for (FeatureInfo featureInfo : features) {
                strList.put(featureInfo.name, "");
            }
            addBuild("Features...", strList);
        }

        // --------------- Sensor Services -------------
        final SensorManager senMgr = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        if (null != senMgr) {
            Map<String, String> strList = new LinkedHashMap<>();
            // Sensor accelerometer = senMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            // senMgr.registerListener(foo, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            List<Sensor> listSensor = senMgr.getSensorList(Sensor.TYPE_ALL);
            try {
                for (Sensor sensor : listSensor) {
                    strList.put(sensor.getName(), sensor.getVendor());
                }
            } catch (Exception ex) {
                m_log.e(ex.getMessage());
            }
            addBuild("Sensors...", strList);
        }

        // --------------- TEST - available services -------
        if (true) {
            Map<String, String> serviceList = new LinkedHashMap<>();
            Field[] fields = Context.class.getDeclaredFields();
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {

                    //  && isRightName(f.getName())
                    String fieldType = field.getType().getName();
                    if (fieldType.equals(String.class.getName()) && field.getName()
                            .endsWith("_SERVICE")) {
                        @SuppressWarnings("UnusedAssignment")
                        String key = field.toString();
                        try {
                            key = field.get(null).toString();
                            if (!"sensorhub".equals(key)) { // Galaxy crashes on sensorhub
                                Object value = getActivity().getSystemService(key);
                                if (value != null) {
                                    serviceList.put(key, value.getClass().getSimpleName());
                                }
                            }
                        } catch (Exception ignore) {
                        }
                    }
                }
            }
            addBuild("Services", serviceList);
        }

        // ----- User info -----
        try {
            if (Build.VERSION.SDK_INT >= 17) {
                final UserManager userMgr = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
                if (null != userMgr) {
                    try {
                        addBuild("UserName", userMgr.getUserName());
                    } catch (Exception ex) {
                        m_log.e(ex.getMessage());
                    }
                }
            }
        } catch (Exception ignore) {
        }

        try {
            Map<String, String> strList = new LinkedHashMap<>();
            int screenTimeout = Settings.System.getInt(getActivity().getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
            strList.put("ScreenTimeOut", String.valueOf(screenTimeout / 1000));
            int rotate = Settings.System.getInt(getActivity().getContentResolver(), Settings.System.ACCELEROMETER_ROTATION);
            strList.put("RotateEnabled", String.valueOf(rotate));
            if (Build.VERSION.SDK_INT >= 17) {
                // Global added in API 17
                int adb = Settings.Global.getInt(getActivity().getContentResolver(), Settings.Global.ADB_ENABLED);
                strList.put("AdbEnabled", String.valueOf(adb));
            }
            strList.put("ByteOrder", ByteOrder.nativeOrder().toString());
            addBuild("Settings...", strList);
        } catch (Exception ignore) {
        }

        if (Build.VERSION.SDK_INT >= 23) {
            try {
                Map<String, String> strList = new LinkedHashMap<>();
                NotificationManager notificationManager =  getServiceSafe(Context.NOTIFICATION_SERVICE);
                int filter = notificationManager.getCurrentInterruptionFilter();
                String distStr = "none";
                switch (filter) {
                    case NotificationManager.INTERRUPTION_FILTER_ALL:
                        distStr = "all";
                        break;
                    case NotificationManager.INTERRUPTION_FILTER_ALARMS:
                        distStr = "alarms";
                        break;
                    case NotificationManager.INTERRUPTION_FILTER_PRIORITY:
                        distStr = "priority";
                        break;
                    case NotificationManager.INTERRUPTION_FILTER_UNKNOWN:
                        distStr = "unknown";
                        break;
                    case NotificationManager.INTERRUPTION_FILTER_NONE:
                        distStr = "none";
                        break;
                }
                strList.put("Interrupts allowed", distStr);
                addBuild("Notification...", strList);
            } catch (Exception ignore) {
            }
        }
        if (expandAll) {
            // updateList();
            int count = m_list.size();
            for (int position = 0; position < count; position++)
                m_listView.expandGroup(position);
        }

        m_adapter.notifyDataSetChanged();
    }

    void addBuildIf(String name, String value, boolean ifValue) {
        if (ifValue)
            m_list.add(new BuildInfo(name, value));
    }

    void addBuild(String name, String value) {
        addBuildIf(name, value, !TextUtils.isEmpty(value));
    }

    private void collapseAll() {
        int count = m_listView.getAdapter().getCount(); // m_list.size();
        for (int position = 0; position < count; position++) {
            // m_log.d("Collapse " + position);
            m_listView.collapseGroup(position);
        }
    }

    private void expandAll() {
        int count = m_listView.getAdapter().getCount(); // m_list.size();
        for (int position = 0; position < count; position++)
            m_listView.expandGroup(position);
    }

    // ============================================================================================
    // DevFragment

    void addBuild(String name, Map<String, String> value) {
        if (!value.isEmpty())
            m_list.add(new BuildInfo(name, value));
    }

    // ============================================================================================
    // Internal methods

    @SuppressWarnings("unused")
    private void clean_networks() {
        StringBuilder sb = new StringBuilder();
        final WifiManager wifiMgr = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiMgr != null && wifiMgr.isWifiEnabled() && wifiMgr.getDhcpInfo() != null) {
            try {
                List<WifiConfiguration> listWifiCfg = wifiMgr.getConfiguredNetworks();
                for (WifiConfiguration wifiCfg : listWifiCfg) {

                    if (wifiCfg.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.NONE)) {

                        // Remove network connections with no Password.
                        if (wifiMgr.removeNetwork(wifiCfg.networkId)) {
                            sb.append(wifiCfg.SSID);
                            sb.append("\n");
                        }
                    }
                }
            } catch (Exception ignore) {
            }
        }

        if (sb.length() != 0) {
            Toast.makeText(this.getContext(), "Removed Networks: " + sb.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    class BuildInfo {
        final String m_fieldStr;
        final String m_valueStr;
        final Map<String, String> m_valueList;


        BuildInfo(String str1, String str2) {
            m_fieldStr = str1;
            m_valueStr = str2;
            m_valueList = null;
        }

        BuildInfo(String str1, Map<String, String> list2) {
            m_fieldStr = str1;
            m_valueStr = null;
            m_valueList = list2;
        }

        public String toString() {
            return m_fieldStr;
        }

        public String fieldStr() {
            return m_fieldStr;
        }

        public String valueStr() {
            return m_valueStr;
        }

        public Map<String, String> valueListStr() {
            return m_valueList;
        }

        public int getCount() {
            return (m_valueList == null) ? 0 : m_valueList.size();
        }
    }

    // =============================================================================================
    /**
     * ExpandableLis UI 'data model' class
     */
    private class BuildArrayAdapter extends BaseExpandableListAdapter
            implements View.OnClickListener {
        private final LayoutInflater m_inflater;

        BuildArrayAdapter(Context context) {
            m_inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        /**
         * Generated expanded detail view object.
         */
        @Override
        public View getChildView(final int groupPosition,
                final int childPosition, boolean isLastChild, View convertView,
                ViewGroup parent) {

            BuildInfo buildInfo = m_list.get(groupPosition);

            View expandView = convertView;
            if (null == expandView) {
                expandView = m_inflater.inflate(SUMMARY_LAYOUT, parent, false);
            }

            if (childPosition < buildInfo.valueListStr().keySet().size()) {
                String key = (String) buildInfo.valueListStr().keySet().toArray()[childPosition];
                String val = buildInfo.valueListStr().get(key);

                TextView textView = Ui.viewById(expandView, R.id.buildField);
                textView.setText(key);
                textView.setPadding(40, 0, 0, 0);

                textView = Ui.viewById(expandView, R.id.buildValue);
                textView.setText(val);

                String text = key + val;

                if (!TextUtils.isEmpty(m_filter) && (m_filter.equals("*")
                        || text.matches(m_filter)
                        || Utils.containsIgnoreCase(text, m_filter))  ) {
                    expandView.setBackgroundColor(0x80ffff00);
                } else {
                    if ((groupPosition & 1) == 1)
                        expandView.setBackgroundColor(m_rowColor1);
                    else
                        expandView.setBackgroundColor(m_rowColor2);
                }
            }

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
            return false;
        }

        /**
         * Generate summary (row) presentation view object.
         */
        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                View convertView, ViewGroup parent) {

            BuildInfo buildInfo = m_list.get(groupPosition);

            View summaryView = convertView;
            if (null == summaryView) {
                summaryView = m_inflater.inflate(SUMMARY_LAYOUT, parent, false);
            }

            TextView textView;
            textView = Ui.viewById(summaryView, R.id.buildField);
            textView.setText(buildInfo.fieldStr());
            textView.setPadding(10, 0, 0, 0);

            textView = Ui.viewById(summaryView, R.id.buildValue);
            textView.setText(buildInfo.valueStr());

            if ((groupPosition & 1) == 1)
                summaryView.setBackgroundColor(m_rowColor1);
            else
                summaryView.setBackgroundColor(m_rowColor2);

            summaryView.setTag(groupPosition);
            summaryView.setOnClickListener(this);
            return summaryView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }


        @Override
        public void onClick(View view) {
            int grpPos = (Integer) view.getTag();

            if (m_listView.isGroupExpanded(grpPos))
                m_listView.collapseGroup(grpPos);
            else
                m_listView.expandGroup(grpPos);
        }
    }

}