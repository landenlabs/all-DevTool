package com.landenlabs.all_devtool;

/*
 * Copyright (c) 2015 - 2020 Dennis Lang (LanDen Labs) landenlabs@gmail.com
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
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.ProxyInfo;
import android.net.RouteInfo;
import android.net.TransportInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.landenlabs.all_devtool.util.LLog;
import com.landenlabs.all_devtool.util.ListInfo;
import com.landenlabs.all_devtool.util.SearchList;
import com.landenlabs.all_devtool.util.Ui;
import com.landenlabs.all_devtool.util.Utils;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static android.telephony.TelephonyManager.NETWORK_TYPE_1xRTT;
import static android.telephony.TelephonyManager.NETWORK_TYPE_CDMA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EDGE;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EHRPD;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_0;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_A;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_B;
import static android.telephony.TelephonyManager.NETWORK_TYPE_GPRS;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSDPA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSPA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSPAP;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSUPA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_IDEN;
import static android.telephony.TelephonyManager.NETWORK_TYPE_LTE;
import static android.telephony.TelephonyManager.NETWORK_TYPE_UMTS;


/**
 * Display "Network" system information.
 *
 * @author Dennis Lang
 */
@SuppressWarnings({"StatementWithEmptyBody", "Convert2Lambda"})
public class NetFragment extends DevFragment {
    // Logger - set to LLog.DBG to only log in Debug build, use LLog.On for always log.
    private final LLog m_log = LLog.DBG;

    final ArrayList<ListInfo> m_list = new ArrayList<>();
    ExpandableListView m_listView;
    TextView m_titleTime;
    ImageButton m_search;
    View m_refresh;
    String m_filter;
    SearchList m_searchList = new SearchList();

    BuildArrayAdapter m_adapter;
    SubMenu m_menu;

    public static String s_name = "Network";
    private static final int m_rowColor1 = 0;
    private static final int m_rowColor2 = 0x80d0ffe0;
    private static SimpleDateFormat m_timeFormat = new SimpleDateFormat("HH:mm:ss zz");
    private static IntentFilter INTENT_FILTER_SCAN_AVAILABLE = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
    private WifiManager wifiMgr;
    private boolean m_updateTime = true;

    // =============================================================================================
    @SuppressWarnings("Convert2Lambda")
    class NetBroadcastReceiver extends BroadcastReceiver {
        final WifiManager mWifiMgr;

        public NetBroadcastReceiver(WifiManager wifiMgr) {
            mWifiMgr = wifiMgr;
        }
        @Override
        public void onReceive(Context context, Intent intent)
        {
            m_listView.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        List<ScanResult> listWifi = mWifiMgr.getScanResults();
                        Log.d("Net", "Wifi size=" + listWifi.size());
                        if (!listWifi.isEmpty()) {
                        }
                        updateList();
                    } catch (Exception ignore) {
                    }
                }
            });
        }
    }

    // =============================================================================================
    NetBroadcastReceiver mNetBroadcastReceiver;

    // ---------------------------------------------------------------------------------------------
    String getNetworkTypeName(int type) {

        switch (type) {
            case NETWORK_TYPE_GPRS:
                return "GPRS";
            case NETWORK_TYPE_EDGE:
                return "EDGE";
            case NETWORK_TYPE_UMTS:
                return "UMTS";
            case NETWORK_TYPE_HSDPA:
                return "HSDPA";
            case NETWORK_TYPE_HSUPA:
                return "HSUPA";
            case NETWORK_TYPE_HSPA:
                return "HSPA";
            case NETWORK_TYPE_CDMA:
                return "CDMA";
            case NETWORK_TYPE_EVDO_0:
                return "CDMA - EvDo rev. 0";
            case NETWORK_TYPE_EVDO_A:
                return "CDMA - EvDo rev. A";
            case NETWORK_TYPE_EVDO_B:
                return "CDMA - EvDo rev. B";
            case NETWORK_TYPE_1xRTT:
                return "CDMA - 1xRTT";
            case NETWORK_TYPE_LTE:
                return "LTE";
            case NETWORK_TYPE_EHRPD:
                return "CDMA - eHRPD";
            case NETWORK_TYPE_IDEN:
                return "iDEN";
            case NETWORK_TYPE_HSPAP:
                return "HSPA+";
            default:
                return "UNKNOWN";
        }
    }


    public NetFragment() {
    }

    public static DevFragment create() {
        return new NetFragment();
    }


    // ============================================================================================
    // DevFragment methods

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
            @NonNull LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        View rootView = inflater.inflate(R.layout.build_tab, container, false);

        Ui.<TextView>viewById(rootView, R.id.list_title).setText(R.string.network_title);
        Ui.viewById(rootView, R.id.list_time_bar).setVisibility(View.VISIBLE);
        m_titleTime = Ui.viewById(rootView, R.id.list_time);
        m_updateTime = true;

        m_search = Ui.viewById(rootView, R.id.list_search);
        m_search.setVisibility(View.VISIBLE);
        m_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_updateTime = false;
                m_titleTime.setText("");
                m_titleTime.setHint("enter search text");
                InputMethodManager imm = getServiceSafe(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(m_titleTime, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        m_titleTime.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView edView, int actionId, KeyEvent event)
            {
                if(actionId == EditorInfo.IME_ACTION_DONE)
                {
                    InputMethodManager imm = getServiceSafe(Context.INPUT_METHOD_SERVICE);
                    // imm.showSoftInput(m_titleTime, InputMethodManager.HIDE_IMPLICIT_ONLY);
                    imm.toggleSoftInput(0, 0);

                    Toast.makeText(getContext(), "Searching...", Toast.LENGTH_SHORT).show();
                    m_filter = edView.getText().toString();
                    m_searchList.search(m_list, m_filter);
                    // updateList();
                    if (m_searchList.matchCnt == 0) {
                        Toast.makeText(getContext(), "No match", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), String.format("%d matches", m_searchList.matchCnt), Toast.LENGTH_SHORT).show();
                        // m_listView.scrollTo(m_filterGroup, 0);
                        m_listView.setSelectedChild(m_searchList.groupIdx, m_searchList.childIdx, true);
                    }

                    // m_listView.invalidate();
                    m_updateTime = true;
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
        m_adapter = new BuildArrayAdapter(getActivitySafe());
        m_listView.setAdapter(m_adapter);

        wifiMgr = getServiceSafe(Context.WIFI_SERVICE);

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
        //noinspection SwitchStatementWithTooFewBranches
        switch (id) {
            case R.id.net_clean_networks:
                clearNetworks();
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        m_menu = menu.addSubMenu("Net Options");
        inflater.inflate(R.menu.net_menu, m_menu);
        // m_menu.findItem(m_sortBy).setChecked(true);
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

    void expandFiltered() {
        if (!TextUtils.isEmpty(m_filter) && !m_filter.equals("*")) {
            for (int grpPos = 0; grpPos < m_list.size(); grpPos++) {

                ListInfo buildInfo = m_list.get(grpPos);

                String key = TextUtils.join(",", buildInfo.valueListStr().keySet().toArray());
                String val = TextUtils.join(",", buildInfo.valueListStr().values().toArray());

                String text = key + val;

                if (text.matches(m_filter) || Utils.containsIgnoreCase(text, m_filter)) {
                    m_listView.expandGroup(grpPos);
                } else {
                    m_listView.collapseGroup(grpPos);
                }
            }
        } else {
            collapseAll();
        }
    }

    @SuppressLint("MissingPermission")
    public void updateList() {
        // Time today = new Time(Time.getCurrentTimezone());
        // today.setToNow();
        // today.format(" %H:%M:%S")
        Date dt = new Date();
        if (m_updateTime) {
            m_titleTime.setText(m_timeFormat.format(dt));
        }

        boolean firstTime = m_list.isEmpty();
        m_list.clear();

        // --------------- Connection Services -------------
        Map<String, String> cellListStr = new LinkedHashMap<>();
        try {
            checkPermissions(Manifest.permission.ACCESS_COARSE_LOCATION
                    , Manifest.permission.READ_PHONE_STATE);
            TelephonyManager telephonyManager = getServiceSafe(Context.TELEPHONY_SERVICE);

            //[not allowed]  cellListStr.put("Device Id", telephonyManager.getDeviceId());
            if (Build.VERSION.SDK_INT >= 26) {
                //[not allowed]  cellListStr.put("IMEI", telephonyManager.getImei());    // api 26
                PersistableBundle carrierCfgBundle = telephonyManager.getCarrierConfig();   // api 26
                if (carrierCfgBundle != null) {
                    cellListStr.put("Carrier", carrierCfgBundle.toString());
                }
                //[not allowed] cellListStr.put("Meid", telephonyManager.getMeid());        // api 26
            }

            cellListStr.put("Call State", String.valueOf(telephonyManager.getCallState()));
            cellListStr.put("Soft Ver", telephonyManager.getDeviceSoftwareVersion());
            if (Build.VERSION.SDK_INT >= 18) {
                cellListStr.put("Grp Id Level", telephonyManager.getGroupIdLevel1());   // api 18
            }
            cellListStr.put("Line1 Num", telephonyManager.getLine1Number());

            if (Build.VERSION.SDK_INT >= 19) {
                cellListStr.put("Agent", telephonyManager.getMmsUserAgent());       // api 19
            }
            //[not allowed]  cellListStr.put("Subscriber", telephonyManager.getSubscriberId());
            cellListStr.put("VoiceMail", telephonyManager.getVoiceMailNumber());
            cellListStr.put("Data act", String.valueOf(telephonyManager.getDataActivity()));
            if (Build.VERSION.SDK_INT >= 24) {
                cellListStr.put("Net type", String.valueOf(telephonyManager.getDataNetworkType()));
            }
            //[not allowed] cellListStr.put("Subscriber", telephonyManager.getSubscriberId());
            cellListStr.put("PhoneType", String.valueOf(telephonyManager.getPhoneType()));
            if (Build.VERSION.SDK_INT >= 24) {
                cellListStr.put("Net Data Typ",
                        String.valueOf(telephonyManager.getDataNetworkType())); // api 24
            }
            if (Build.VERSION.SDK_INT >= 23) {
                cellListStr.put("Is World",
                        String.valueOf(telephonyManager.isWorldPhone()));       // api 23
            }

            // Type of the network
            int phoneTypeInt = telephonyManager.getPhoneType();
            String phoneType = "Unknown";
            phoneType = phoneTypeInt == TelephonyManager.PHONE_TYPE_GSM ? "gsm" : phoneType;
            phoneType = phoneTypeInt == TelephonyManager.PHONE_TYPE_CDMA ? "cdma" : phoneType;
            cellListStr.put("Type", phoneType);

            for (CellInfo cellinfo : telephonyManager.getAllCellInfo()) {
                // CellInfo  CellInfoGsm
                if (cellinfo instanceof  CellInfoGsm) {
                    CellInfoGsm cellitem = (CellInfoGsm)cellinfo;

                    int dbm = cellitem.getCellSignalStrength().getDbm();

                    // ASU ranges from 0 to 31 - TS 27.007 Sec 8.5
                    // asu = 0 (-113dB or less) is very weak
                    // signal, its better to show 0 bars to the user in such cases.
                    // asu = 99 is a special case, where the signal strength is unknown.
                    int asu = cellitem.getCellSignalStrength().getAsuLevel();

                    // Get signal level as an int from 0..4
                    int level = cellitem.getCellSignalStrength().getLevel();
                    
                    String msg = String.format("Db=%d, Asu=%d Level(0..4)=%d", dbm, asu, level);
                    String id = String.format("GSM LAC=%d CID=%d",
                            cellitem.getCellIdentity().getLac(),
                            cellitem.getCellIdentity().getCid());
                    cellListStr.put(id, msg);

                    // Cell Tower location
                    // https://api.mylnikov.org/geolocation/cell?v=1.1&data=open&mcc=310&mnc=260&lac=36455&cellid=10022

                } else if (cellinfo instanceof  CellInfoLte) {
                    CellInfoLte cellitem = (CellInfoLte)cellinfo;

                    int dbm = cellitem.getCellSignalStrength().getDbm();
                    int asu = cellitem.getCellSignalStrength().getAsuLevel();
                    int level = cellitem.getCellSignalStrength().getLevel();

                    String msg = String.format("Db=%d, Asu=%d Level(0..4)=%d", dbm, asu, level);
                    String id = String.format("LTE CI=%d PCI=%d",
                            cellitem.getCellIdentity().getCi(),
                            cellitem.getCellIdentity().getPci());
                    cellListStr.put(id, msg);

                } else if (cellinfo instanceof  CellInfoCdma) {
                    CellInfoCdma cellitem = (CellInfoCdma)cellinfo;

                    int dbm = cellitem.getCellSignalStrength().getDbm();
                    int asu = cellitem.getCellSignalStrength().getAsuLevel();
                    int level = cellitem.getCellSignalStrength().getLevel();

                    String msg = String.format("Db=%d, Asu=%d Level(0..4)=%d", dbm, asu, level);
                    String id = String.format("Cdma Nid=%d Sid=%d",
                            cellitem.getCellIdentity().getNetworkId(),
                            cellitem.getCellIdentity().getSystemId());
                    cellListStr.put(id, msg);
                } else if (Build.VERSION.SDK_INT >= 18 && cellinfo instanceof CellInfoWcdma) {
                    CellInfoWcdma cellitem = (CellInfoWcdma) cellinfo;

                    int dbm = cellitem.getCellSignalStrength().getDbm();
                    int asu = cellitem.getCellSignalStrength().getAsuLevel();
                    int level = cellitem.getCellSignalStrength().getLevel();

                    String msg = String.format("Db=%d, Asu=%d Level(0..4)=%d", dbm, asu, level);
                    String id = String.format("Wcdma Cid=%d Lac=%d",
                            cellitem.getCellIdentity().getCid(),
                                    cellitem.getCellIdentity().getLac());
                    cellListStr.put(id, msg);
                }
            }
        } catch (Exception ex) {
            cellListStr.put(ex.getClass().getSimpleName(), ex.getMessage());
        }
        if (!cellListStr.isEmpty()) {
            addBuild("Cell...", cellListStr);
        }

        // --------------- Connection Services -------------
        Map<String, String> netListStr = new LinkedHashMap<>();
        try {
            ConnectivityManager connMgr = getServiceSafe(Context.CONNECTIVITY_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                final Network activeNet = connMgr.getActiveNetwork();
                if (activeNet != null) {
                    ProxyInfo proxyInfo =  connMgr.getDefaultProxy();
                    if (proxyInfo != null) {
                        netListStr.put("Proxy", proxyInfo.getHost() + ":" + proxyInfo.getPort());
                    }

                    final Network[] allNet = connMgr.getAllNetworks();
                    if (allNet.length > 0) {
                        for (Network net : allNet) {
                            putIf(netListStr,  "Connected", "Yes", activeNet.equals(net));
                            final LinkProperties linkProp = connMgr.getLinkProperties(net);
                             if (linkProp != null) {
                                 String netInterfaceName = linkProp.getInterfaceName();
                                 putIf(netListStr,  "Domain", linkProp.getDomains(), !TextUtils.isEmpty(linkProp.getDomains()));
                                 addNetCapToList(netListStr, netInterfaceName, connMgr.getNetworkCapabilities(activeNet));
                                 int dnsNum=0;
                                 for (InetAddress dns : linkProp.getDnsServers()) {
                                     if (dns.getHostAddress() != null) {
                                         dnsNum++;
                                         netListStr.put(netInterfaceName + " DNS " + dnsNum, formatInet(dns));
                                     }
                                 }
                                 for (RouteInfo route : linkProp.getRoutes()) {
                                     netListStr.put(route.getInterface() + " Route ", formatInet(route.getGateway()));
                                 }
                             }
                        }
                    }
                    addBuild("Network...", netListStr);
                }
            } else {
                final NetworkInfo listInfo = connMgr.getActiveNetworkInfo();
                if (listInfo != null) {
                    putIf(netListStr, "Available", "Yes", listInfo.isAvailable());
                    putIf(netListStr, "Connected", "Yes", listInfo.isConnected());
                    putIf(netListStr, "Connecting", "Yes", !listInfo.isConnected() && listInfo.isConnectedOrConnecting());
                    putIf(netListStr, "Roaming", "Yes", listInfo.isRoaming());
                    putIf(netListStr, "Extra", listInfo.getExtraInfo(), !TextUtils.isEmpty(listInfo.getExtraInfo()));
                    putIf(netListStr, "WhyFailed", listInfo.getReason(), !TextUtils.isEmpty(listInfo.getReason()));
                    putIf(netListStr, "Metered", "Avoid heavy use", connMgr.isActiveNetworkMetered());

                    netListStr.put("NetworkType", listInfo.getTypeName());
                    if (connMgr.getAllNetworkInfo().length > 1) {
                        netListStr.put("Available Networks:", " ");
                        for (NetworkInfo netI : connMgr.getAllNetworkInfo()) {
                            if (netI.isAvailable()) {
                                String state = netI.isAvailable() ? "Yes" : "No";
                                if (netI.isConnected())
                                    state = "Connected";
                                else if (netI.isConnectedOrConnecting())
                                    state = "Connecting";

                                netListStr.put("  " + netI.getTypeName(), state);
                            }
                        }
                    }

                    if (connMgr.isActiveNetworkMetered()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            String howMetered = "";
                            switch (connMgr.getRestrictBackgroundStatus()) {
                                case ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED:
                                    // Background data usage is blocked for this app. Wherever possible,
                                    // the app should also use less data in the foreground.
                                    howMetered = "Restrict background";
                                    break;
                                case ConnectivityManager.RESTRICT_BACKGROUND_STATUS_WHITELISTED:
                                    // The app is whitelisted. Wherever possible,
                                    // the app should use less data in the foreground and background.
                                    howMetered = "Restrict bg, whitelist";
                                    break;
                                case ConnectivityManager.RESTRICT_BACKGROUND_STATUS_DISABLED:
                                    // Data Saver is disabled. Since the device is connected to a
                                    // metered network, the app should use less data wherever possible.
                                    howMetered = "Data Saver disabled";
                                    break;
                            }
                            netListStr.put("Metered", howMetered);
                        }
                    }

                    if (listInfo.isConnected()) {
                        try {
                            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                                NetworkInterface intf = en.nextElement();
                                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                                    InetAddress inetAddress = enumIpAddr.nextElement();
                                    if (!inetAddress.isLoopbackAddress()) {
                                        if (inetAddress.getHostAddress() != null) {
                                            String ipType = (inetAddress instanceof Inet4Address) ? "IPv4" : "IPv6";
                                            netListStr.put(intf.getName() + " " + ipType, inetAddress.getHostAddress());
                                        }
                                        // if (!TextUtils.isEmpty(inetAddress.getHostName()))
                                        //     listStr.put( "HostName", inetAddress.getHostName());
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            m_log.e("Network %s", ex.getMessage());
                        }
                    }

                }
            }
        } catch (Exception ex) {
            m_log.e("Network %s", ex.getMessage());
            netListStr.put(ex.getClass().getSimpleName(), ex.getMessage());
        }
        addBuild("Network...", netListStr);

        // --------------- Telephony Services -------------
        TelephonyManager telephonyManager = getServiceSafe(Context.TELEPHONY_SERVICE);
        Map<String, String> phoneListStr = new LinkedHashMap<>();
        try {
            phoneListStr.put("Version", telephonyManager.getDeviceSoftwareVersion());
            phoneListStr.put("Number", telephonyManager.getLine1Number());
            phoneListStr.put("Service", telephonyManager.getNetworkOperatorName());
            phoneListStr.put("Roaming", telephonyManager.isNetworkRoaming() ? "Yes" : "No");
            phoneListStr.put("Type", getNetworkTypeName(telephonyManager.getNetworkType()));

            if (Build.VERSION.SDK_INT >= 17) {
                if (telephonyManager.getAllCellInfo() != null) {
                    for (CellInfo cellInfo : telephonyManager.getAllCellInfo()) {
                        String cellName = cellInfo.getClass().getSimpleName();
                        int level = 0;
                        if (cellInfo instanceof CellInfoCdma) {
                            level = ((CellInfoCdma) cellInfo).getCellSignalStrength().getLevel();
                        } else if (cellInfo instanceof CellInfoGsm) {
                            level = ((CellInfoGsm) cellInfo).getCellSignalStrength().getLevel();
                        } else if (cellInfo instanceof CellInfoLte) {
                            level = ((CellInfoLte) cellInfo).getCellSignalStrength().getLevel();
                        } else if (Build.VERSION.SDK_INT >= 18 && cellInfo instanceof CellInfoWcdma) {
                            level = ((CellInfoWcdma) cellInfo).getCellSignalStrength().getLevel();
                        }
                        phoneListStr.put(cellName, "Level% " + String.valueOf(100 * level / 4));
                    }
                }
            }

            /*
            for (NeighboringCellInfo cellInfo : telephonyManager.getNeighboringCellInfo()) {
                int level = cellInfo.getRssi();
                phoneListStr.put("Cell level%", String.valueOf(100 * level / 31));
            }
            */
        } catch (Exception ex) {
            m_log.e("Cell %s", ex.getMessage());
            phoneListStr.put(ex.getClass().getSimpleName(), ex.getMessage());
        }

        if (!phoneListStr.isEmpty()) {
            addBuild("Phone...", phoneListStr);
        }

        // --------------- Bluetooth Services (API18) -------------
        if (Build.VERSION.SDK_INT >= 18) {
            Map<String, String> btListStr = new LinkedHashMap<>();
            try {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter != null) {
                    btListStr.put("Enabled", bluetoothAdapter.isEnabled() ? "yes" : "no");
                    btListStr.put("Name", bluetoothAdapter.getName());
                    btListStr.put("Address", bluetoothAdapter.getAddress());
                    btListStr.put("ScanMode", String.valueOf(bluetoothAdapter.getScanMode()));
                    btListStr.put("State", String.valueOf(bluetoothAdapter.getState()));
                    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                    // If there are paired devices
                    if (pairedDevices.size() > 0) {
                        // Loop through paired devices
                        for (BluetoothDevice device : pairedDevices) {
                            // Add the name and address to an array adapter to show in a ListView
                            btListStr.put("Paired:" + device.getName(), device.getAddress());
                        }
                    }

                    PackageManager pm = getActivity().getPackageManager();
                    boolean isBT = pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH);
                    boolean isBLE = pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
                    btListStr.put("BLE", isBLE  ? "yes" : "no");

                    // BluetoothManager btMgr = getServiceSafe(Context.BLUETOOTH_SERVICE);
                    // BluetoothGattServer mGattServer = btMgr.openGattServer(getContext(), null);

                    // BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
                    // BluetoothConfigManager manager = BluetoothConfigManager.getInstance();
                    // BluetoothProfileManager manager = BluetoothProfileManager.getInstance();



                }

            } catch (Exception ex) {
                btListStr.put(ex.getClass().getSimpleName(), ex.getMessage());
            }
            // btMgr.getAdapter().
            addBuild("Bluetooth", btListStr);
        }

        // --------------- Telephony Services -------------


        // --------------- Wifi Services -------------
        // final WifiManager wifiMgr = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr != null) {
            // boolean wifiEnabled = wifiMgr.isWifiEnabled();
            // boolean dhcpiEnabled = wifiMgr.getDhcpInfo() != null;

            if (mNetBroadcastReceiver == null) {
                mNetBroadcastReceiver = new NetBroadcastReceiver(wifiMgr);
                // getActivitySafe().unregisterReceiver(mNetBroadcastReceiver);
                getActivitySafe().registerReceiver(mNetBroadcastReceiver, INTENT_FILTER_SCAN_AVAILABLE);
            }

            if (wifiMgr.getScanResults() == null || wifiMgr.getScanResults().isEmpty() ) {
                // checkPermissions(Manifest.permission.ACCESS_COARSE_LOCATION);
                checkPermissions(Manifest.permission.ACCESS_FINE_LOCATION);
                wifiMgr.startScan();
            }

            String wifiKey = "Wifi...";
            Map<String, String> wifiListStr = new LinkedHashMap<>();
            try {
                DhcpInfo dhcpInfo = wifiMgr.getDhcpInfo();

                // java.net.InetAddress.
                wifiListStr.put("DNS1", formatIp(dhcpInfo.dns1));
                wifiListStr.put("DNS2", formatIp(dhcpInfo.dns2));
                wifiListStr.put("Default Gateway", formatIp(dhcpInfo.gateway));
                wifiListStr.put("IP Address", formatIp(dhcpInfo.ipAddress));
                wifiListStr.put("Subnet Mask", formatIp(dhcpInfo.netmask));
                wifiListStr.put("Server IP", formatIp(dhcpInfo.serverAddress));
                wifiListStr.put("Lease Time(sec)", String.valueOf(dhcpInfo.leaseDuration));

                WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
                if (wifiInfo != null) {
                    wifiListStr.put( "SSID", wifiInfo.getSSID());
                    wifiListStr.put("LinkSpeed Mbps", String.valueOf(wifiInfo.getLinkSpeed()));
                    wifiKey = String.format("Wifi %s %,dMB ", wifiInfo.getSSID(), wifiInfo.getLinkSpeed());

                    int numberOfLevels = 10;
                    int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels + 1);
                    wifiListStr.put("Signal%", String.valueOf(100 * level / numberOfLevels));
                    if (Build.VERSION.SDK_INT >= 23) {
                        wifiListStr.put("MAC", getMacAddr());
                    } else {
                        wifiListStr.put("MAC", wifiInfo.getMacAddress());
                    }
                    if (Build.VERSION.SDK_INT >= 21) {
                        wifiListStr.put("Frequency",
                                wifiInfo.getFrequency() + WifiInfo.FREQUENCY_UNITS);
                    }

                    /*
                    public boolean is24GHz() {
                    public boolean is5GHz() {
                    public long txBad;
                    public long txRetries;
                    public long txSuccess;
                    public long rxSuccess;
                    public double txBadRate;
                    public double txRetriesRate;
                    public double txSuccessRate;
                    public double rxSuccessRate;
                    */

                }

            } catch (Exception ex) {
                m_log.e("Wifi %s", ex.getMessage());
                wifiListStr.put(ex.getClass().getSimpleName(), ex.getMessage());
            }

            if (!wifiListStr.isEmpty()) {
                addBuild(wifiKey, wifiListStr);
            }

            long bootTime = System.currentTimeMillis() - SystemClock.elapsedRealtime();

            try {
                List<ScanResult> listWifi = wifiMgr.getScanResults();
                if (listWifi != null && !listWifi.isEmpty()) {
                    int idx = 0;

                    for (ScanResult scanResult : listWifi) {
                        Map<String, String> wifiScanListStr = new LinkedHashMap<>();
                        wifiScanListStr.put("SSID", scanResult.SSID);
                        if (Build.VERSION.SDK_INT >= 23) {
                            wifiScanListStr.put("  Name", scanResult.operatorFriendlyName.toString());
                            wifiScanListStr.put("  Venue", scanResult.venueName.toString());
                        }

                        //        wifiScanListStr.put("  BSSID ",scanResult.BSSID);
                        wifiScanListStr.put("  Capabilities", scanResult.capabilities);
                        //       wifiScanListStr.put("  Center Freq", String.valueOf(scanResult.centerFreq0));
                        //       wifiScanListStr.put("  Freq width", String.valueOf(scanResult.channelWidth));
                        String levelFreq = String.format("%d db, %d MHz",
                                scanResult.level, scanResult.frequency);
                        wifiScanListStr.put("  Level, Freq", levelFreq);
                        // wifiScanListStr.put("  Width", scanResult.channelWidth);
                        if (Build.VERSION.SDK_INT >= 17) {
                            Date wifiTime = new Date(scanResult.timestamp/1000 + bootTime);
                            SimpleDateFormat fmt = new SimpleDateFormat("M/d/yy h:mm a");
                            wifiScanListStr.put("  Time", fmt.format(wifiTime));
                            // wifiScanListStr.put("  Time", wifiTime.toLocaleString());
                        }

                        addBuild(String.format("WiFi%d %s %s", ++idx,
                                wifiScanListStr.get("SSID"), levelFreq), wifiScanListStr);
                    }
                }
            } catch (Exception ex) {
                m_log.e("WifiList %s", ex.getMessage());
            }

            addConfigNetworks();
        }

        if (firstTime) {
            // updateList();
            int count = m_list.size();
            for (int position = 0; position < count; position++)
                m_listView.expandGroup(position);
        }

        m_adapter.notifyDataSetChanged();
    }

    @NonNull
    private String formatInet(@Nullable InetAddress inetAddress) {
        String result = "";
        if (inetAddress != null) {
            if (inetAddress.getHostAddress() != null) {
                String ipType = (inetAddress instanceof Inet4Address) ? "IPv4 "
                        : (inetAddress instanceof Inet6Address ? "IPv6 "
                        : "");
                return ipType + inetAddress.getHostName();
            }
        }
        return result;
    }

    private void addNetCapToList(Map<String,String> netListStr, String tag, NetworkCapabilities netCap) {
        if (netCap != null) {
            if (Build.VERSION.SDK_INT >= 21) {
                netListStr.put("Down limit", String.format(Locale.US, "%,d Mbps", netCap.getLinkDownstreamBandwidthKbps()/1000));
                netListStr.put("Up limit", String.format(Locale.US, "%,d Mbps", netCap.getLinkUpstreamBandwidthKbps()/1000));
                if (Build.VERSION.SDK_INT >= 29) {
                    netListStr.put("Signal", String.valueOf(netCap.getSignalStrength()));
                    // TransportInfo transportInfo = netCap.getTransportInfo();
                }
            }
        }
    }

    @NonNull
    private String formatIp(int ipAddrss) {
        byte[] myIPAddress = BigInteger.valueOf(ipAddrss).toByteArray();
        // you must reverse the byte array before conversion. Use Apache's commons library
        invertUsingFor(myIPAddress);
        try {
            InetAddress myInetIP = InetAddress.getByAddress(myIPAddress);
            return myInetIP.getHostAddress();
        } catch (Exception ex) {
            return "<unknownIP>";
        }
    }

    void invertUsingFor(byte[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            byte temp = array[i];
            array[i] = array[array.length - 1 - i];
            array[array.length - 1 - i] = temp;
        }
    }
    
    /**
     *
     */
    void addConfigNetworks() {

        try {
            List<WifiConfiguration> listWifiCfg = wifiMgr.getConfiguredNetworks();

            for (WifiConfiguration wifiCfg : listWifiCfg) {
                Map<String, String> wifiCfgListStr = new LinkedHashMap<>();
                if (Build.VERSION.SDK_INT >= 23) {
                    wifiCfgListStr.put("Name", wifiCfg.providerFriendlyName);
                }
                wifiCfgListStr.put("SSID", wifiCfg.SSID);
                String netStatus = "";
                switch (wifiCfg.status) {
                    case WifiConfiguration.Status.CURRENT:
                        netStatus = "Connected"; break;
                    case WifiConfiguration.Status.DISABLED:
                        netStatus = "Disabled"; break;
                    case WifiConfiguration.Status.ENABLED:
                        netStatus = "Enabled"; break;
                }
                wifiCfgListStr.put(" Status", netStatus);
                wifiCfgListStr.put(" Priority", String.valueOf(wifiCfg.priority));
                if (null != wifiCfg.wepKeys) {
                     // wifiCfgListStr.put(" wepKeys", TextUtils.join(",", wifiCfg.wepKeys));
                }
                String protocols = "";
                if (wifiCfg.allowedProtocols.get(WifiConfiguration.Protocol.RSN))
                    protocols = "RSN ";
                if (wifiCfg.allowedProtocols.get(WifiConfiguration.Protocol.WPA))
                    protocols = protocols + "WPA ";
                wifiCfgListStr.put(" Protocols", protocols);

                String keyProt = "";
                if (wifiCfg.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.NONE))
                    keyProt = "none";
                if (wifiCfg.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP))
                    keyProt = "WPA+EAP ";
                if (wifiCfg.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK))
                    keyProt = "WPA+PSK ";
                wifiCfgListStr.put(" Keys", keyProt);

                if (wifiCfg.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.NONE)) {
                    // Remove network connections with no Password.
                    // wifiMgr.removeNetwork(wifiCfg.networkId);
                }

                String wifiCfgStr = wifiCfg.toString().replace("\n", " ");
                // " cuid=" + creatorUid);
                // " cname=" + creatorName);
                String creator =  wifiCfgStr.replaceAll(".* cname=([^ ]+) .*", "$1");
                wifiCfgListStr.put(" Creator", creator);

                addBuild(String.format("WiFiCfg#%s %s",
                        wifiCfg.networkId, wifiCfg.SSID), wifiCfgListStr);
            }

        } catch (Exception ex) {
            m_log.e("Wifi Cfg List %s", ex.getMessage());
        }
    }

    void addBuild(String name, Map<String, String> value) {
        if (!value.isEmpty())
            m_list.add(new ListInfo(name, value));
    }

    private void clearNetworks() {
        StringBuilder sb = new StringBuilder();
        final WifiManager wifiMgr = getServiceSafe(Context.WIFI_SERVICE);
        if (checkPermissions(Manifest.permission.CHANGE_WIFI_STATE)) { // && wifiMgr.isWifiEnabled() && wifiMgr.getDhcpInfo() != null) {

            try {
                List<WifiConfiguration> listWifiCfg = wifiMgr.getConfiguredNetworks();
                if (listWifiCfg != null) {
                    for (WifiConfiguration wifiCfg : listWifiCfg) {

                        if (wifiCfg.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.NONE)) {

                            // Remove network connections with no Password.
                            // ONLY allowed to remove networks we have added !!
                            if (wifiMgr.removeNetwork(wifiCfg.networkId)) {
                                sb.append("Removed:\n   ")
                                    .append(wifiCfg.SSID)
                                    .append("\n");
                            } else {
                                sb.append("Can't remove:\n  ")
                                    .append(wifiCfg.SSID)
                                    .append("\n");
                            }
                        }
                    }
                    wifiMgr.saveConfiguration();
                }
            } catch (Exception ex) {
                Log.e("net", ex.getLocalizedMessage());
            }
        }

        if (sb.length() != 0) {
            Toast.makeText(this.getContext(), "WiFi Networks:\n"
                    + sb.toString(), Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            // Settings.ACTION_WIFI_IP_SETTINGS
        }
    }

    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF)).append(":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ignore) {
        }
        return "02:00:00:00:00:00";
    }

    // ============================================================================================
    // DevFragment

    @Override
    public void onStop() {
        if (mNetBroadcastReceiver != null) {
            getActivitySafe().unregisterReceiver(mNetBroadcastReceiver);
            mNetBroadcastReceiver = null;
        }
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            updateList();
        }
    }

    @Override
    public void onDetach() {
        if (mNetBroadcastReceiver != null) {
            getActivitySafe().unregisterReceiver(mNetBroadcastReceiver);
            mNetBroadcastReceiver = null;
        }
        super.onDetach();
    }


    // ============================================================================================
    // Internal methods

    // Put values in List ifValue true.
    private static <M extends Map<E, E>, E> void putIf(M listObj, E v1, E v2, boolean ifValue) {
        if (ifValue) {
            listObj.put(v1, v2);
        }
    }

    // =============================================================================================

    final static int SUMMARY_LAYOUT = R.layout.build_list_row;
    final static int SUMMARY_LAYOUT_WIDE = R.layout.build_list_rows_wide;
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
        public View getChildView(
            final int groupPosition,
            final int childPosition, boolean isLastChild, View convertView,
            ViewGroup parent) {

            ListInfo listInfo = m_list.get(groupPosition);

            View expandView = convertView;

            if (childPosition < listInfo.valueListStr().keySet().size()) {
                String key = (String) listInfo.valueListStr().keySet().toArray()[childPosition];
                String val = "" + listInfo.valueListStr().get(key);

                if (key.length() + val.length() > 40) {
                    expandView = m_inflater.inflate(SUMMARY_LAYOUT_WIDE, parent, false);
                } else {
                    expandView = m_inflater.inflate(SUMMARY_LAYOUT, parent, false);
                }
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
            }  else {
                if (null == expandView) {
                    expandView = m_inflater.inflate(SUMMARY_LAYOUT, parent, false);
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
        public View getGroupView(
                int groupPosition, boolean isExpanded,
                View convertView, ViewGroup parent) {

            ListInfo listInfo = m_list.get(groupPosition);

            View summaryView = convertView;
            if (null == summaryView) {
                summaryView = m_inflater.inflate(SUMMARY_LAYOUT, parent, false);
            }

            TextView textView;
            textView = Ui.viewById(summaryView, R.id.buildField);
            textView.setText("" + groupPosition + " " + listInfo.fieldStr());
            textView.setPadding(10, 0, 0, 0);

            textView = Ui.viewById(summaryView, R.id.buildValue);
            textView.setText(listInfo.valueStr());

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