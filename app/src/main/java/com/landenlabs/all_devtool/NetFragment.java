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
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
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

import com.landenlabs.all_devtool.util.LLog;
import com.landenlabs.all_devtool.util.Ui;
import com.landenlabs.all_devtool.util.Utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
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
public class NetFragment extends DevFragment {
    // Logger - set to LLog.DBG to only log in Debug build, use LLog.On for always log.
    private final LLog m_log = LLog.DBG;

    final ArrayList<BuildInfo> m_list = new ArrayList<BuildInfo>();
    ExpandableListView m_listView;
    TextView m_titleTime;
    ImageButton m_search;
    View m_refresh;
    String m_filter;

    BuildArrayAdapter m_adapter;
    SubMenu m_menu;

    public static String s_name = "Network";
    private static final int MB = 1 << 20;
    private static final int m_rowColor1 = 0;
    private static final int m_rowColor2 = 0x80d0ffe0;
    private static SimpleDateFormat m_timeFormat = new SimpleDateFormat("HH:mm:ss zz");
    private static IntentFilter INTENT_FILTER_SCAN_AVAILABLE = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
    private WifiManager wifiMgr;

    // =============================================================================================
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
                    List<ScanResult> listWifi = mWifiMgr.getScanResults();
                    Log.d("Net", "Wifi size=" + listWifi.size());
                    if (listWifi != null &&  !listWifi.isEmpty()) {
                    }
                    updateList();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        View rootView = inflater.inflate(R.layout.build_tab, container, false);

        Ui.<TextView>viewById(rootView, R.id.list_title).setText(R.string.network_title);
        Ui.viewById(rootView, R.id.list_time_bar).setVisibility(View.VISIBLE);
        m_titleTime = Ui.viewById(rootView, R.id.list_time);

        m_search = Ui.viewById(rootView, R.id.list_search);
        m_search.setVisibility(View.VISIBLE);
        m_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_titleTime.setText("");
                m_titleTime.setHint("enter search text");
                InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
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
                    InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
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

        wifiMgr = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

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
        int pos = -1;
        int id = item.getItemId();
        switch (id) {
            case R.id.net_clean_networks:
                clean_networks();
                break;

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
        m_menu = menu.addSubMenu("Net Options");
        inflater.inflate(R.menu.net_menu, m_menu);
        // m_menu.findItem(m_sortBy).setChecked(true);
    }

    public void updateList() {
        // Time today = new Time(Time.getCurrentTimezone());
        // today.setToNow();
        // today.format(" %H:%M:%S")
        Date dt = new Date();
        m_titleTime.setText(m_timeFormat.format(dt));

        boolean firstTime = m_list.isEmpty();
        m_list.clear();



        // --------------- Connection Services -------------
        try {
            checkPermissions(Manifest.permission.ACCESS_COARSE_LOCATION
                    , Manifest.permission.READ_PHONE_STATE);

            Map<String, String> cellListStr = new LinkedHashMap<String, String>();
            TelephonyManager telephonyManager =
             (TelephonyManager)getActivity().getSystemService(Context.TELEPHONY_SERVICE);


            cellListStr.put("IMEI", telephonyManager.getImei());
            cellListStr.put("Device Id", telephonyManager.getDeviceId());
            PersistableBundle carrierCfgBundle = telephonyManager.getCarrierConfig();
            if (carrierCfgBundle != null) {
                cellListStr.put("Carrier", carrierCfgBundle.toString());
            }
            cellListStr.put("Call State", String.valueOf(telephonyManager.getCallState()));
            cellListStr.put("Soft Ver", telephonyManager.getDeviceSoftwareVersion());
            cellListStr.put("Grp Id Level", telephonyManager.getGroupIdLevel1());
            cellListStr.put("Line1 Num", telephonyManager.getLine1Number());
            cellListStr.put("Meid", telephonyManager.getMeid());
            cellListStr.put("Agent", telephonyManager.getMmsUserAgent());
            cellListStr.put("Subscriber", telephonyManager.getSubscriberId());
            cellListStr.put("VoiceMail", telephonyManager.getVoiceMailNumber());
            cellListStr.put("Data act", String.valueOf(telephonyManager.getDataActivity()));
            cellListStr.put("Net type", String.valueOf(telephonyManager.getDataNetworkType()));
            cellListStr.put("Subscriber", telephonyManager.getSubscriberId());
            cellListStr.put("PhoneType", String.valueOf(telephonyManager.getPhoneType()));
            cellListStr.put("Net Data Typ", String.valueOf(telephonyManager.getDataNetworkType()));
            cellListStr.put("Is World", String.valueOf(telephonyManager.isWorldPhone()));


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
                } else if (cellinfo instanceof  CellInfoWcdma) {
                    if (Build.VERSION.SDK_INT >= 18) {
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
            }

            addBuild("Cell...", cellListStr);
        } catch (Exception ex) {

        }

        // --------------- Connection Services -------------
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
            if (netInfo != null) {
                Map<String, String> netListStr = new LinkedHashMap<String, String>();

                putIf(netListStr, "Available", "Yes", netInfo.isAvailable());
                putIf(netListStr, "Connected", "Yes", netInfo.isConnected());
                putIf(netListStr, "Connecting", "Yes", !netInfo.isConnected() && netInfo.isConnectedOrConnecting());
                putIf(netListStr, "Roaming", "Yes", netInfo.isRoaming());
                putIf(netListStr, "Extra", netInfo.getExtraInfo(), !TextUtils.isEmpty(netInfo.getExtraInfo()));
                putIf(netListStr, "WhyFailed", netInfo.getReason(), !TextUtils.isEmpty(netInfo.getReason()));
                if (Build.VERSION.SDK_INT >= 16) {
                    putIf(netListStr, "Metered", "Avoid heavy use", connMgr.isActiveNetworkMetered());
                }

                netListStr.put("NetworkType", netInfo.getTypeName());
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

                if (netInfo.isConnected()) {
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

                addBuild("Network...", netListStr);
            }
        } catch (Exception ex) {
            m_log.e("Network %s", ex.getMessage());
        }

        // --------------- Telephony Services -------------
        TelephonyManager telephonyManager =
                (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            Map<String, String> cellListStr = new LinkedHashMap<String, String>();
            try {
                cellListStr.put("Version", telephonyManager.getDeviceSoftwareVersion());
                cellListStr.put("Number", telephonyManager.getLine1Number());
                cellListStr.put("Service", telephonyManager.getNetworkOperatorName());
                cellListStr.put("Roaming", telephonyManager.isNetworkRoaming() ? "Yes" : "No");
                cellListStr.put("Type", getNetworkTypeName(telephonyManager.getNetworkType()));

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
                            } else if (cellInfo instanceof CellInfoWcdma) {
                                if (Build.VERSION.SDK_INT >= 18) {
                                    level = ((CellInfoWcdma) cellInfo).getCellSignalStrength().getLevel();
                                }
                            }
                            cellListStr.put(cellName, "Level% " + String.valueOf(100 * level / 4));
                        }
                    }
                }

                for (NeighboringCellInfo cellInfo : telephonyManager.getNeighboringCellInfo()) {
                    int level = cellInfo.getRssi();
                    cellListStr.put("Cell level%", String.valueOf(100 * level / 31));
                }

            } catch (Exception ex) {
                m_log.e("Cell %s", ex.getMessage());
            }

            if (!cellListStr.isEmpty()) {
                addBuild("Cell...", cellListStr);
            }
        }

        // --------------- Bluetooth Services (API18) -------------
        if (Build.VERSION.SDK_INT >= 18) {
            try {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter != null) {

                    Map<String, String> btListStr = new LinkedHashMap<String, String>();

                    btListStr.put("Enabled", bluetoothAdapter.isEnabled() ? "yes" : "no");
                    btListStr.put("Name", bluetoothAdapter.getName());
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

                    BluetoothManager btMgr = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
                    if (btMgr != null) {
                        // btMgr.getAdapter().
                    }
                    addBuild("Bluetooth", btListStr);
                }

            } catch (Exception ex) {

            }
        }

        // --------------- Telephony Services -------------


        // --------------- Wifi Services -------------
        // final WifiManager wifiMgr = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr != null) {
            boolean wifiEnabled = wifiMgr.isWifiEnabled();
            boolean dhcpiEnabled = wifiMgr.getDhcpInfo() != null;

            if (mNetBroadcastReceiver == null) {
                mNetBroadcastReceiver = new NetBroadcastReceiver(wifiMgr);
                // getActivity().unregisterReceiver(mNetBroadcastReceiver);
                getActivity().registerReceiver(mNetBroadcastReceiver, INTENT_FILTER_SCAN_AVAILABLE);
            }

            if (wifiMgr.getScanResults() == null || wifiMgr.getScanResults().isEmpty() ) {
                checkPermissions(Manifest.permission.ACCESS_COARSE_LOCATION);
                checkPermissions(Manifest.permission.ACCESS_FINE_LOCATION);
                wifiMgr.startScan();
            }

            Map<String, String> wifiListStr = new LinkedHashMap<String, String>();
            try {
                DhcpInfo dhcpInfo = wifiMgr.getDhcpInfo();

                wifiListStr.put("DNS1", Formatter.formatIpAddress(dhcpInfo.dns1));
                wifiListStr.put("DNS2", Formatter.formatIpAddress(dhcpInfo.dns2));
                wifiListStr.put("Default Gateway", Formatter.formatIpAddress(dhcpInfo.gateway));
                wifiListStr.put("IP Address", Formatter.formatIpAddress(dhcpInfo.ipAddress));
                wifiListStr.put("Subnet Mask", Formatter.formatIpAddress(dhcpInfo.netmask));
                wifiListStr.put("Server IP", Formatter.formatIpAddress(dhcpInfo.serverAddress));
                wifiListStr.put("Lease Time(sec)", String.valueOf(dhcpInfo.leaseDuration));

                WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
                if (wifiInfo != null) {
                    wifiListStr.put("LinkSpeed Mbps", String.valueOf(wifiInfo.getLinkSpeed()));
                    int numberOfLevels = 10;
                    int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels + 1);
                    wifiListStr.put("Signal%", String.valueOf(100 * level / numberOfLevels));
                    if (Build.VERSION.SDK_INT >= 23) {
                        wifiListStr.put("MAC", getMacAddr());
                    } else {
                        wifiListStr.put("MAC", wifiInfo.getMacAddress());
                    }
                }

            } catch (Exception ex) {
                m_log.e("Wifi %s", ex.getMessage());
            }

            if (!wifiListStr.isEmpty()) {
                addBuild("WiFi...", wifiListStr);
            }

            try {
                List<ScanResult> listWifi = wifiMgr.getScanResults();
                if (listWifi != null && !listWifi.isEmpty()) {
                    int idx = 0;

                    for (ScanResult scanResult : listWifi) {
                        Map<String, String> wifiScanListStr = new LinkedHashMap<String, String>();
                        wifiScanListStr.put("SSID", scanResult.SSID);
                        if (Build.VERSION.SDK_INT >= 23) {
                            wifiScanListStr.put("  Name", scanResult.operatorFriendlyName.toString());
                            wifiScanListStr.put("  Venue", scanResult.venueName.toString());
                        }

                        //        wifiScanListStr.put("  BSSID ",scanResult.BSSID);
                        wifiScanListStr.put("  Capabilities", scanResult.capabilities);
                        //       wifiScanListStr.put("  Center Freq", String.valueOf(scanResult.centerFreq0));
                        //       wifiScanListStr.put("  Freq width", String.valueOf(scanResult.channelWidth));
                        wifiScanListStr.put("  Level, Freq", String.format("%d, %d", scanResult.level, scanResult.frequency));
                        if (Build.VERSION.SDK_INT >= 17) {
                            Date wifiTime = new Date(scanResult.timestamp);
                            wifiScanListStr.put("  Time", wifiTime.toLocaleString());
                        }
                        addBuild(String.format("WiFiScan #%d", ++idx), wifiScanListStr);
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


    /**
     *
     */
    void addConfigNetworks() {

        try {
            List<WifiConfiguration> listWifiCfg = wifiMgr.getConfiguredNetworks();

            for (WifiConfiguration wifiCfg : listWifiCfg) {
                Map<String, String> wifiCfgListStr = new LinkedHashMap<String, String>();
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
                    //               wifiCfgListStr.put(" wepKeys", TextUtils.join(",", wifiCfg.wepKeys));
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

                addBuild("WiFiCfg #" + wifiCfg.networkId, wifiCfgListStr);
            }

        } catch (Exception ex) {
            m_log.e("Wifi Cfg List %s", ex.getMessage());
        }
    }

    void addBuildIf(String name, String value, boolean ifValue) {
        if (ifValue)
            m_list.add(new BuildInfo(name, value));
    }

    void addBuild(String name, String value) {
        addBuildIf(name, value, !TextUtils.isEmpty(value));
    }

    void addBuild(String name, Map<String, String> value) {
        if (!value.isEmpty())
            m_list.add(new BuildInfo(name, value));
    }

    private void clean_networks() {
        StringBuilder sb = new StringBuilder();
        final WifiManager wifiMgr = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiMgr != null && checkPermissions(Manifest.permission.CHANGE_WIFI_STATE)) { // && wifiMgr.isWifiEnabled() && wifiMgr.getDhcpInfo() != null) {

            try {
                List<WifiConfiguration> listWifiCfg = wifiMgr.getConfiguredNetworks();
                if (listWifiCfg != null) {
                    for (WifiConfiguration wifiCfg : listWifiCfg) {

                        if (wifiCfg.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.NONE)) {

                            // Remove network connections with no Password.
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
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }

    // ============================================================================================
    // DevFragment

    @Override
    public void onStop() {
        if (mNetBroadcastReceiver != null) {
            getActivity().unregisterReceiver(mNetBroadcastReceiver);
            mNetBroadcastReceiver = null;
        }
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            updateList();
        }
    }

    @Override
    public void onDetach() {
        if (mNetBroadcastReceiver != null) {
            getActivity().unregisterReceiver(mNetBroadcastReceiver);
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

    class BuildInfo {
        final String m_fieldStr;
        final String m_valueStr;
        final Map<String, String> m_valueList;

        BuildInfo() {
            m_fieldStr = m_valueStr = null;
            m_valueList = null;
        }

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

    final static int EXPANDED_LAYOUT = R.layout.build_list_row;
    final static int SUMMARY_LAYOUT = R.layout.build_list_row;

    /**
     * ExpandableLis UI 'data model' class
     */
    private class BuildArrayAdapter extends BaseExpandableListAdapter {
        private final LayoutInflater m_inflater;

        public BuildArrayAdapter(Context context) {
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
            textView.setText("" + groupPosition + " " + buildInfo.fieldStr());
            textView.setPadding(10, 0, 0, 0);

            textView = Ui.viewById(summaryView, R.id.buildValue);
            textView.setText(buildInfo.valueStr());

            if ((groupPosition & 1) == 1)
                summaryView.setBackgroundColor(m_rowColor1);
            else
                summaryView.setBackgroundColor(m_rowColor2);

            return summaryView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

    }
}