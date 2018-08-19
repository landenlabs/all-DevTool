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


import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.DhcpInfo;
import android.net.TrafficStats;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.landenlabs.all_devtool.util.LLog;
import com.landenlabs.all_devtool.util.Ui;
import com.landenlabs.all_devtool.util.Utils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Display "Build" system information.
 *
 * @author Dennis Lang
 */
@SuppressWarnings("Convert2Lambda")
public class ConsoleFragment extends DevFragment implements View.OnClickListener {
    private final LLog m_log = LLog.DBG;

    CheckBox m_refreshCb;

    public static String s_name = "Console";

    private static final String DASH = "Dash";

    private static final String MEMORY_TIME = "Time:";
    private static final String MEMORY_USING = "Using:";
    private static final String MEMORY_FREE = "Free:";
    private static final String MEMORY_TOTAL = "Total:";
    private static final String[] MEMORY_NAMES = { MEMORY_TIME, MEMORY_USING, MEMORY_FREE, MEMORY_TOTAL };

    private static final String SYSTEM_PACKAGE = "Package:";
    private static final String SYSTEM_MODEL = "Model:";
    private static final String SYSTEM_ANDROID = "Android:";
    private static final String SYSTEM_PERM = "Perm:";
    private static final String[] SYSTEM_STATIC_NAMES = { SYSTEM_PACKAGE, SYSTEM_MODEL, SYSTEM_ANDROID, SYSTEM_PERM };
    private static final String SYSTEM_PROCESSES = "#Process:";
    private static final String SYSTEM_BATTERY = "Battery:";
    // private static final String SYSTEM_CPU = "Cpu:";
    private static final String[] SYSTEM_DYNAMIC_NAMES = { SYSTEM_PROCESSES, SYSTEM_BATTERY/* , SYSTEM_CPU */ };

    private static final String NETWORK_WIFI_IP = "IP";
    private static final String NETWORK_WIFI_SPEED = "Speed:";
    private static final String NETWORK_WIFI_SIGNAL = "Signal:";
    private static final String[] NETWORK_WIFI_NAMES = { NETWORK_WIFI_IP, NETWORK_WIFI_SPEED, NETWORK_WIFI_SIGNAL };
    private static final String NETWORK_RCV_BYTES = "RcvBytes:";
    private static final String NETWORK_RCV_PACK = "RcvPack:";
    private static final String NETWORK_SND_BYTES = "SndBytes:";
    private static final String NETWORK_SND_PACK = "SndPack:";
    private static final String[] NETWORK_TRAFFIC_NAMES = {NETWORK_RCV_BYTES, NETWORK_RCV_PACK, NETWORK_SND_BYTES, NETWORK_SND_PACK };

    private static final int[] WIDTHS_2 = { 70, 210 };
    private static final int[] WIDTHS_3 = { 70, 100, 100, 80 };
    private static SimpleDateFormat TIMEFORMAT = new SimpleDateFormat("HH:mm:ss.SS");
    private static int sNextId = 100;

    // WSIAppConsoleSettings mConsoleSettings;
    Map<String, List<TextView>> mMemoryViews = new HashMap<>();
    Map<String, List<TextView>> mSystemViews = new HashMap<>();
    Map<String, List<TextView>> mNetworkViews = new HashMap<>();

    ConsoleState consoleState = ConsoleState.s_consoleState;

    // ---- Timer
    private static final int REFRESH_MSEC = 1000;
    private Handler m_handler = new Handler();
    private Runnable m_updateElapsedTimeTask = new Runnable() {
        public void run() {
            updateConsole();
            if (m_refreshCb != null) {
                m_handler.postDelayed(this, REFRESH_MSEC);   // Re-execute after xxx ms.
            }
        }
    };

    public ConsoleFragment() {
    }

    public static DevFragment create() {
        return new ConsoleFragment();
    }

    // ============================================================================================
    // DevFragment methods

    @Override
    public String getName() {
        return s_name;
    }

    @Override
    public List<Bitmap> getBitmaps(int maxHeight) {
        List<Bitmap> bitmapList = new ArrayList<>();
        bitmapList.add(Utils.grabScreen(getActivitySafe()));
        return bitmapList;
    }

    @Override
    public List<String> getListAsCsv() {
        List<String> csvList = new ArrayList<>();
        Utils.getTextCsv(getView(), "", csvList);
        return csvList;
    }

    @Override
    public void onSelected() {
        super.onSelected();
        updateConsole();
    }

    // ============================================================================================
    // Fragment methods

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View rootView = inflater.inflate(R.layout.console_tab, container, false);

        rootView.findViewById(R.id.consoleSnap).setOnClickListener(this);
        rootView.findViewById(R.id.consoleRefresh).setOnClickListener(this);

        m_refreshCb = Ui.viewById(rootView, R.id.consoleRefresh);

        // mConsoleSettings = mWsiApp.getSettingsManager().getSettings(WSIAppConsoleSettings.class);
        final int belowIdTop = 0;

        RelativeLayout systemLayout = Ui.viewById(rootView, R.id.consoleSystemLayout);
        buildGrid(systemLayout, mSystemViews, SYSTEM_STATIC_NAMES, WIDTHS_2, belowIdTop);
        buildGrid(systemLayout, mSystemViews, SYSTEM_DYNAMIC_NAMES, WIDTHS_3,
                mSystemViews.get(SYSTEM_STATIC_NAMES[SYSTEM_STATIC_NAMES.length-1]).get(0).getId());

        RelativeLayout networkWifiLayout = Ui.viewById(rootView, R.id.consoleNetworkWifiLayout);
        buildGrid(networkWifiLayout, mNetworkViews, NETWORK_WIFI_NAMES, WIDTHS_2, belowIdTop);
        RelativeLayout networkTrafficLayout = Ui.viewById(rootView, R.id.consoleNetworkTrafficLayout);
        buildGrid(networkTrafficLayout, mNetworkViews, NETWORK_TRAFFIC_NAMES, WIDTHS_3, belowIdTop);

        RelativeLayout memoryLayout = Ui.viewById(rootView, R.id.consoleMemoryLayout);
        buildGrid(memoryLayout, mMemoryViews, MEMORY_NAMES, WIDTHS_3, belowIdTop);

        if (consoleState.usingMemory == 0) {
            snapConsole();
        }

        // updateConsole();

        return rootView;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        consoleState.restoreInstanceState(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        consoleState.saveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        m_handler.removeCallbacks(m_updateElapsedTimeTask);
        m_handler.postDelayed(m_updateElapsedTimeTask, 0);
    }

    @Override
    public void onPause() {
        m_handler.removeCallbacks(m_updateElapsedTimeTask);
        super.onPause();
    }

    // ============================================================================================
    // OnClickListener

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.consoleSnap:
                snapConsole();
                updateConsole();
                break;
            case R.id.consoleRefresh:
                if (m_refreshCb.isChecked()) {
                    m_handler.removeCallbacks(m_updateElapsedTimeTask);
                    m_handler.postDelayed(m_updateElapsedTimeTask, 0);
                } else {
                    m_handler.removeCallbacks(m_updateElapsedTimeTask);
                }
                // updateConsole();
                break;
        }
    }

    // ============================================================================================
    // Internal methods

    private TextView addTextView(RelativeLayout relLayout,
                 int belowId, int rightId, int widthDp, int heightDp, int padLeft) {

        int widthParam = (widthDp != 0) ? dpToPx(widthDp): RelativeLayout.LayoutParams.WRAP_CONTENT;
        int heightParam = (heightDp != 0) ? dpToPx(heightDp) : RelativeLayout.LayoutParams.WRAP_CONTENT;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(widthParam, heightParam);

        // params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        if (belowId != 0)
            params.addRule(RelativeLayout.BELOW, belowId);
        if (rightId != 0) {
            if (rightId > 0)
                params.addRule(RelativeLayout.ALIGN_RIGHT, rightId);
            else
                params.addRule(RelativeLayout.RIGHT_OF, -rightId);
        }

        // relLayout.setPadding(padLeft,  0,  0,  0);
        params.setMargins(padLeft, 0, 0, 0);

        TextView textView = new TextView(relLayout.getContext());
        textView.setLines(1);
        if (widthDp > 0)
            textView.setMaxWidth(dpToPx(widthDp));


        if (Build.VERSION.SDK_INT >= 17) {
            textView.setId(View.generateViewId());
        } else {
            textView.setId(sNextId++);
        }

        relLayout.addView(textView, params);
        return textView;
    }

    private List<TextView> addViews(RelativeLayout relLayout, Map<String, List<TextView>> listView,
                String name, int numCol, List<TextView> colViews) {
        List<TextView> rowView = new ArrayList<>();
        for (int col = 0; col != numCol; col++) {
            rowView.add(addTextView(relLayout, colViews.get(col).getId(), colViews.get(col).getId(), 0, 0, 0));
        }
        rowView.get(0).setText(name);
        listView.put(name, rowView);
        return rowView;
    }

    private void buildGrid(RelativeLayout systemLayout, Map<String, List<TextView>> listView,
               String[] names, int[] widthsDp, int belowId) {
        // listView.clear();
        int numCol = widthsDp.length;
        int dashHeight = 2;
        int widthDp = pxToDp(Resources.getSystem().getDisplayMetrics().widthPixels);
        int[] actualWidthsDp = new int[widthsDp.length];
        int padLeftDp = 10;

        List<TextView> colViews = new ArrayList<>();
        colViews.add(addTextView(systemLayout, belowId, 0, widthsDp[0], dashHeight, padLeftDp));
        actualWidthsDp[0] = widthsDp[0];
        widthDp -=  widthsDp[0] + padLeftDp;
        for (int col=1; col != numCol; col++) {
            int colWidthDp = (widthsDp[col] < widthDp) ? widthsDp[col] : widthDp;
            colViews.add(addTextView(systemLayout, belowId, -colViews.get(col-1).getId(), colWidthDp, dashHeight, padLeftDp));
            actualWidthsDp[col] = colWidthDp;
            widthDp -=  colWidthDp + padLeftDp;
        }
        listView.put(DASH,  colViews);

        for (int col = 0; col != numCol; col++) {
            colViews.get(col).setBackgroundColor(-1);
        }

        for (int row = 0; row != names.length; row++) {
            String name = names[row];
            colViews = addViews(systemLayout, listView, name, numCol, colViews);
            for (int col=0; col != numCol; col++) {
                colViews.get(col).setMaxWidth(dpToPx(actualWidthsDp[col]) );
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private void updateConsole() {

        if (mSystemViews.isEmpty())
            return;

        try {
            // ----- System -----
            ApplicationInfo appInfo = getActivitySafe().getApplicationInfo();

            mSystemViews.get(SYSTEM_PACKAGE).get(1).setText(appInfo.packageName);
            mSystemViews.get(SYSTEM_MODEL).get(1).setText(Build.MODEL);
            mSystemViews.get(SYSTEM_ANDROID).get(1).setText(Build.VERSION.RELEASE);

            int lines = 0;
            final StringBuilder permSb = new StringBuilder();
            try {
                PackageInfo pi = getContextSafe().getPackageManager().getPackageInfo(
                        getContextSafe().getPackageName(), PackageManager.GET_PERMISSIONS);
                for (int i = 0; i < pi.requestedPermissions.length; i++) {
                    if ((pi.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0) {
                        permSb.append(pi.requestedPermissions[i]).append("\n");
                        lines++;
                    }
                }
            } catch (Exception ex) {
                m_log.e(ex.getMessage());
            }
            final int lineCnt = lines;
            mSystemViews.get(SYSTEM_PERM).get(1).setText(String.format("%d perms [press]", lines));
            mSystemViews.get(SYSTEM_PERM).get(1).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (v.getTag() == null) {
                            String permStr = permSb.toString()
                                    .replaceAll("android.permission.", "")
                                    .replaceAll("\n[^\n]*permission", "");
                            mSystemViews.get(SYSTEM_PERM).get(1).setText(permStr);
                            mSystemViews.get(SYSTEM_PERM).get(0).setLines(lineCnt);
                            mSystemViews.get(SYSTEM_PERM).get(1).setLines(lineCnt);
                            mSystemViews.get(SYSTEM_PERM).get(1).setTag(lineCnt);
                        } else {
                            mSystemViews.get(SYSTEM_PERM).get(1).setText(String.format("%d perms", lineCnt));
                            mSystemViews.get(SYSTEM_PERM).get(0).setLines(1);
                            mSystemViews.get(SYSTEM_PERM).get(1).setLines(1);
                            mSystemViews.get(SYSTEM_PERM).get(1).setTag(null);
                        }
                    }
                }
            );


            ActivityManager actMgr = getServiceSafe(Context.ACTIVITY_SERVICE);
            int processCnt = actMgr.getRunningAppProcesses().size();
            mSystemViews.get(SYSTEM_PROCESSES).get(1).setText(String.format("%d", consoleState.processCnt));
            mSystemViews.get(SYSTEM_PROCESSES).get(2).setText(String.format("%d", processCnt));
            // mSystemViews.get(SYSTEM_BATTERY).get(1).setText(String.format("%d%%", consoleState.batteryLevel));
            mSystemViews.get(SYSTEM_BATTERY).get(2).setText(String.format("%%%d", calculateBatteryLevel(getActivitySafe())));
            // long cpuNano = Debug.threadCpuTimeNanos();
            // mSystemViews.get(SYSTEM_CPU).get(2).setText(String.format("%d%%", cpuNano));

        } catch (Exception ex) {
            m_log.e(ex.getMessage());
        }

        try {
            // ----- Network WiFi-----

            WifiManager wifiMgr = getServiceSafe(Context.WIFI_SERVICE);
            if (wifiMgr.isWifiEnabled() && wifiMgr.getDhcpInfo() != null) {
                DhcpInfo dhcpInfo = wifiMgr.getDhcpInfo();


                byte[] myIPAddress = BigInteger.valueOf(dhcpInfo.ipAddress).toByteArray();
                InetAddress myInetIP = InetAddress.getByAddress(myIPAddress);

                mNetworkViews.get(NETWORK_WIFI_IP).get(1).setText(myInetIP.getHostAddress());
                WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
                mNetworkViews.get(NETWORK_WIFI_SPEED).get(1).setText(String.valueOf(wifiInfo.getLinkSpeed()));
                int numberOfLevels = 10;
                int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels + 1);
                mNetworkViews.get(NETWORK_WIFI_SIGNAL).get(1).setText(String.format("%%%d", 100 * level / numberOfLevels));
            }
        } catch (Exception ex) {
            m_log.e(ex.getMessage());
        }
        try {
            // ----- Network Traffic-----
            // int uid = android.os.Process.myUid();
            mNetworkViews.get(NETWORK_RCV_BYTES).get(1).setText(String.format("%d", consoleState.netRxBytes));
            mNetworkViews.get(NETWORK_RCV_PACK).get(1).setText(String.format("%d", consoleState.netRxPacks));
            mNetworkViews.get(NETWORK_SND_BYTES).get(1).setText(String.format("%d", consoleState.netTxBytes));
            mNetworkViews.get(NETWORK_SND_PACK).get(1).setText(String.format("%d", consoleState.netTxPacks));

            mNetworkViews.get(NETWORK_RCV_BYTES).get(2).setText(String.format("%d", TrafficStats.getTotalRxBytes()));
            mNetworkViews.get(NETWORK_RCV_PACK).get(2).setText(String.format("%d", TrafficStats.getTotalRxPackets()));
            mNetworkViews.get(NETWORK_SND_BYTES).get(2).setText(String.format("%d", TrafficStats.getTotalTxBytes()));
            mNetworkViews.get(NETWORK_SND_PACK).get(2).setText(String.format("%d", TrafficStats.getTotalRxPackets()));

            mNetworkViews.get(NETWORK_RCV_BYTES).get(3).setText(String.format("%d", TrafficStats.getTotalRxBytes() - consoleState.netRxBytes));
            mNetworkViews.get(NETWORK_RCV_PACK).get(3).setText(String.format("%d", TrafficStats.getTotalRxPackets() - consoleState.netRxPacks));
            mNetworkViews.get(NETWORK_SND_BYTES).get(3).setText(String.format("%d", TrafficStats.getTotalTxBytes() - consoleState.netTxBytes));
            mNetworkViews.get(NETWORK_SND_PACK).get(3).setText(String.format("%d", TrafficStats.getTotalRxPackets()- consoleState.netTxPacks));

        } catch (Exception ex) {
            m_log.e(ex.getMessage());
        }

        // ----- Memory -----
        try {
            MemoryInfo mi = new  MemoryInfo();
            ActivityManager activityManager = getServiceSafe(Context.ACTIVITY_SERVICE);
            activityManager.getMemoryInfo(mi);

            long heapUsing = Debug.getNativeHeapSize();

            Date now = new Date();

            List<TextView> timeViews = mMemoryViews.get(MEMORY_TIME);
            timeViews.get(1).setText(TIMEFORMAT.format(consoleState.lastFreeze));
            timeViews.get(2).setText(TIMEFORMAT.format(now));
            timeViews.get(3).setText( DateUtils.getRelativeTimeSpanString (consoleState.lastFreeze.getTime(), now.getTime(), 0));
            // timeViews.get(3).setText( String.valueOf(deltaMsec));

            List<TextView> usingViews = mMemoryViews.get(MEMORY_USING);
            usingViews.get(1).setText(String.format("%d", consoleState.usingMemory));
            usingViews.get(2).setText(String.format("%d", heapUsing));
            usingViews.get(3).setText(String.format("%d", heapUsing - consoleState.usingMemory));


            List<TextView> freeViews = mMemoryViews.get(MEMORY_FREE);
            freeViews.get(1).setText(String.format("%d", consoleState.freeMemory));
            freeViews.get(2).setText(String.format("%d", mi.availMem));
            freeViews.get(3).setText(String.format("%d", mi.availMem - consoleState.freeMemory));

            List<TextView> totalViews = mMemoryViews.get(MEMORY_TOTAL);
            totalViews.get(1).setText(String.format("%d", consoleState.totalMemory));
            totalViews.get(2).setText(String.format("%d", mi.totalMem));
            totalViews.get(3).setText(String.format("%d", mi.totalMem - consoleState.totalMemory));

        } catch (Exception ex) {
            m_log.e(ex.getMessage());
        }
    }

    private void snapConsole() {
        try {
            MemoryInfo mi = new MemoryInfo();
            ActivityManager activityManager = getServiceSafe(Context.ACTIVITY_SERVICE);
            activityManager.getMemoryInfo(mi);

            consoleState.lastFreeze = new Date();
            consoleState.usingMemory = Debug.getNativeHeapSize();
            consoleState.freeMemory = mi.availMem;
            consoleState.totalMemory = mi.totalMem;

            consoleState.netRxBytes = TrafficStats.getTotalRxBytes();
            consoleState.netRxPacks = TrafficStats.getTotalRxPackets();
            consoleState.netTxBytes = TrafficStats.getTotalTxBytes();
            consoleState.netTxPacks = TrafficStats.getTotalRxPackets();

            ActivityManager actMgr = getServiceSafe(Context.ACTIVITY_SERVICE);
            consoleState.processCnt = actMgr.getRunningAppProcesses().size();
            consoleState.batteryLevel = calculateBatteryLevel(getActivitySafe());
        } catch (Exception ex) {
            m_log.e(ex.getMessage());
        }
    }

    private int calculateBatteryLevel(Context context) {
        Intent batteryIntent = context.getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        if (batteryIntent != null) {
            int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
            return level * 100 / scale;
        }
        return 0;
    }

    /**
     * Gets total system cpu usage (not just this app)
     */
    private float getCpuUsage() {
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();

            String[] toks = load.split(" ");

            long idle1 = Long.parseLong(toks[5]);
            long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            try {
                Thread.sleep(360);
            } catch (Exception e) {
                m_log.e(e.getMessage());
            }

            reader.seek(0);
            load = reader.readLine();
            reader.close();

            toks = load.split(" ");

            long idle2 = Long.parseLong(toks[5]);
            long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            return (float)(cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    /**
     * @return Convert dp to px, return px
     */
    private static int dpToPx(int dp) {
        return  (int)(dp * Resources.getSystem().getDisplayMetrics().density);
    }

    private static int pxToDp(int px) {
        return  (int)(px / Resources.getSystem().getDisplayMetrics().density);
    }
}