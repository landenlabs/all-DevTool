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


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
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
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;

import com.landenlabs.all_devtool.util.ArrayListPair;
import com.landenlabs.all_devtool.util.LLog;
import com.landenlabs.all_devtool.util.NetUtils;
import com.landenlabs.all_devtool.util.Ui;
import com.landenlabs.all_devtool.util.Utils;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


/**
 * Display "Network" system information.
 *
 * @author Dennis Lang
 */
@SuppressWarnings({"Convert2Lambda", "FieldCanBeLocal", "SynchronizeOnNonFinalField"})
public class NetstatFragment extends DevFragment {
    // Logger - set to LLog.DBG to only log in Debug build, use LLog.On for always log.
    private final LLog m_log = LLog.DBG;

    private ArrayList<NetInfo> m_list = new ArrayList<>();
    private ExpandableListView m_listView;
    private TextView m_titleTime;
    private ImageButton m_search;
    private View m_refresh;
    private String m_filter;

    private NetArrayAdapter m_adapter;
    private SubMenu m_menu;

    public static String s_name = "Netstat";
    private static final int m_rowColor1 = 0;
    private static final int m_rowColor2 = 0x80d0ffe0;
    private static SimpleDateFormat m_timeFormat = new SimpleDateFormat("HH:mm:ss zz");
    private boolean m_updateTime = true;



    public NetstatFragment() {
    }

    public static DevFragment create() {
        return new NetstatFragment();
    }


    // ============================================================================================
    // DevFragment methods

    @Override
    public String getName() {
        return s_name;
    }

    @Override
    public List<Bitmap> getBitmaps(int maxHeight) {
        synchronized (m_listView) {
            return Utils.getListViewAsBitmaps(m_listView, maxHeight);
        }
    }

    @Override
    public List<String> getListAsCsv() {
        synchronized (m_listView) {
            return Utils.getListViewAsCSV(m_listView);
        }
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

                    m_filter = edView.getText().toString();
                    Toast.makeText(getContext(), "Searching for " + m_filter , Toast.LENGTH_SHORT).show();
                    // updateList();
                    expandFiltered();
                    m_listView.invalidate();
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
        m_adapter = new NetArrayAdapter(getActivitySafe());
        m_listView.setAdapter(m_adapter);

        m_adapter.setOnItemLongClickListener1(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View view, int pos, long id) {
                Toast.makeText(getActivity(), String.format("Long Press on %d id:%d ", pos, id), Toast.LENGTH_LONG).show();
                // int grpPos = (Integer) view.getTag();
                if (pos >= 0 && pos < m_list.size()) {
                    NetInfo netInfo = m_list.get(pos);
                    String packName = netInfo.valueListStr().get(0).first.trim();
                    if (!TextUtils.isEmpty(packName) && packName.contains(".")) {
                        openPackageInfo(packName);
                        return true;
                    }
                }
                return false;
            }
        });

        m_listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View view, int groupPosition,
                    int childPosition, long id) {

                if (groupPosition >= 0 && groupPosition < m_list.size()) {
                    NetInfo netInfo = m_list.get(groupPosition);

                    final String LATITUDE = "latitude:";
                    final String LONGITUDE = "longitude:";

                    String netLines = netInfo.valueListStr().get(childPosition).second;
                    if (netLines != null && netLines.contains(LATITUDE)) {
                        String[] lines = netLines.split("\n");
                        String lat="", lng="";
                        for (String line : lines) {
                            if (line.contains(LATITUDE))
                                lat = line.split(":")[1].trim();
                            else if (line.contains(LONGITUDE)) {
                                lng = line.split(":")[1].trim();
                            }
                        }

                        if (!TextUtils.isEmpty(lat) && !TextUtils.isEmpty(lng)) {
                            // Uri gmmIntentUri = Uri.parse("geo:" + lat + "," + lng);
                            Uri gmmIntentUri = Uri.parse("geo:0,0?q="+lat+","+lng+"(Remote)");
                            // Create a Uri from an intent string. Use the result to create an Intent.
                            // Uri gmmIntentUri = Uri.parse("google.streetview:cbll="+ lat + "," + lng);
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");

                            // https://tools.keycdn.com/geo?host=<ip>
                            // http://api.ipstack.com/<ip>?access_key=<apiKey>
                            
                            if (mapIntent.resolveActivity(
                                            getActivitySafe().getPackageManager()) != null) {
                                startActivity(mapIntent);
                            }
                        }
                    }
                }
                return true;
            }
        });

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
    public void onPause() {
        super.onPause();
        if (getGeoLocation != null) {
            ipAddressQueue.clear();
            getGeoLocation.interrupt();
            getGeoLocation = null;
        }
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
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
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        m_menu = menu.addSubMenu("Netstat Options");
    }

    private void collapseAll() {
        updateListIf();
        synchronized (m_listView) {
            int count = m_listView.getAdapter().getCount(); // m_list.size();
            for (int position = 0; position < count; position++) {
                // m_log.d("Collapse " + position);
                m_listView.collapseGroup(position);
            }
        }
    }

    private void expandAll() {
        updateListIf();
        synchronized (m_listView) {
            // Keeps crashing in this code, not sure why listview and list would be different.
            int count = Math.min(m_listView.getAdapter().getCount(), m_list.size());
            for (int position = 0; position < count; position++) {
                m_listView.expandGroup(position);
            }
        }
    }

    private void updateListIf() {
        if (m_listView.getCount() != m_list.size()) {
            updateList();
        }
    }

    private void expandFiltered() {
        if (!TextUtils.isEmpty(m_filter) && !m_filter.equals("*")) {
            updateListIf();
            synchronized (m_listView) {
                for (int grpPos = 0; grpPos < m_list.size(); grpPos++) {

                    NetInfo buildInfo = m_list.get(grpPos);

                    boolean isMatch = false;
                    for (Pair<String,String> strPair : buildInfo.valueListStr()) {
                        isMatch = strPair.first.matches(m_filter)
                                || strPair.second.matches(m_filter)
                                || Utils.containsIgnoreCase(strPair.first, m_filter)
                                || Utils.containsIgnoreCase(strPair.second, m_filter);
                    }

                    if (isMatch) {
                        m_listView.expandGroup(grpPos);
                    } else {
                        m_listView.collapseGroup(grpPos);
                    }
                }
            }
        } else {
            collapseAll();
        }
    }

    private void updateList() {
        Date dt = new Date();
        if (m_updateTime) {
            m_titleTime.setText(m_timeFormat.format(dt));
        }

        synchronized (m_listView) {
            boolean firstTime = m_list.isEmpty();
            ArrayList<NetInfo> newList = new ArrayList<>();

            // --------------- Network connections ------------

            NetUtils.NetConnections netConnections =
                    NetUtils.getConnetions(getContextSafe());
            if (netConnections.size() > 0) {
                Map<String, ArrayListPairString> pkgConnections = new TreeMap<>();

                for (NetUtils.NetConnection netConnection : netConnections) {
                    String appName = netConnection.appName == null ? "unknown" : netConnection.appName;
                    // if (!TextUtils.isEmpty(netConnection.appName)) {
                    ArrayListPairString netConnectionListStr =
                                pkgConnections.get(appName);
                        if (netConnectionListStr == null) {
                            netConnectionListStr = new ArrayListPairString();
                            pkgConnections.put(appName, netConnectionListStr);
                            netConnectionListStr.add(" " + netConnection.packageName,
                                    "v" + netConnection.appVersion);
                        }

                        String netVal;
                        if ( netConnection.netStatus == NetUtils.NetStatus.LISTEN) {
                            netVal = "Local: " + netConnection.localAddr
                                    .getHostAddress() + " : " + netConnection.localPort
                                    + getIpGeoLocation(netConnection.remoteAddr);
                        } else {
                            netVal =  "Remote: " + netConnection.remoteAddr
                                    .getHostAddress() + " : " + netConnection.remotePort
                                    + getIpGeoLocation(netConnection.remoteAddr);
                        }
                        netConnectionListStr.add(String
                                        .format("#%d %s %s", netConnectionListStr.size(),
                                                netConnection.type, netConnection.netStatus.name())
                                , netVal);
                    // }
                }
                for (String appName : pkgConnections.keySet()) {
                    ArrayListPairString netConnList = pkgConnections.get(appName);
                    if (netConnList != null) {
                        newList.add(new NetInfo(appName, netConnList));
                    }
                }
            }

            m_listView.post(new Runnable() {
                @Override
                public void run() {
                    synchronized (m_listView) {
                        m_list = newList;
                        m_adapter.notifyDataSetChanged();   // Does nothing ?
                        m_listView.invalidate();            // Force UI to rebuild.

                        if (firstTime) {
                            int count = m_list.size();
                            for (int position = 0; position < count; position++) {
                                m_listView.expandGroup(position);
                            }
                        }
                    }
                }
            });
        }
    }



    /**
     * Open Application Detail Info dialog for package.
     */
    private void openPackageInfo(String packageName) {
        //redirect user to app Settings
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse("package:" + packageName));
        startActivity(intent);
    }


    // ============================================================================================
    // Ip geolookup

    private static final Map<InetAddress, Map<String, String>> ipGeoLocations = new HashMap<>();
    private static final BlockingQueue<InetAddress> ipAddressQueue = new ArrayBlockingQueue<>(100);

    static class GetGeoLocation extends Thread {
        @Override
        public void run() {
            super.run();
            InetAddress addr;
            try {
                while ((addr = ipAddressQueue.take()) != null) {
                    ipGeoLocations.put(addr, NetUtils.getIpLocation(addr));
                    Log.d("netstat", "geoLoc size=" + ipGeoLocations.size());
                }
            } catch (InterruptedException ex) {
                Log.d("netstat", "geoLoc err=" + ex.getMessage());
            }
        }
    }

    private GetGeoLocation getGeoLocation = null;

    private void appendIf(StringBuilder sb, Map<String, String> mapStr, String key) {
        if (mapStr != null && mapStr.containsKey(key) && !TextUtils.isEmpty(mapStr.get(key)) )
            sb.append("\n").append(key).append(":").append(mapStr.get(key));
    }
    private String getIpGeoLocation(InetAddress addr) {
        if (ipGeoLocations.containsKey(addr)) {
            Map<String, String> ipInfo = ipGeoLocations.get(addr);
            StringBuilder sb = new StringBuilder();
            appendIf(sb, ipInfo, "city");
            appendIf(sb, ipInfo,"region");
            appendIf(sb, ipInfo,"country_name");
            appendIf(sb, ipInfo,"latitude");
            appendIf(sb, ipInfo,"longitude");
            return sb.toString();
        } else {
            if (!ipAddressQueue.contains(addr)) {
                ipAddressQueue.add(addr);
                if (getGeoLocation == null) {
                    getGeoLocation = new GetGeoLocation();
                    getGeoLocation.start();
                }
            }
        }

        return "";
    }

    // ============================================================================================
    // DevFragment

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            updateList();
        }
    }


    // ============================================================================================
    private static class ArrayListPairString extends ArrayListPair<String, String> {
    }

    // =============================================================================================
    static
    class NetInfo {
        final String m_fieldStr;
        final String m_valueStr;
        final ArrayListPairString  m_valueList;    // key order added maintained.
        final String m_appName;

        /*
        NetInfo(String str1, String str2) {
            m_fieldStr = str1;
            m_valueStr = str2;
            m_valueList = null;
        }
        */

        NetInfo(String appName, ArrayListPairString connections) {
            m_appName = appName;
            m_fieldStr = appName + " #" + (connections.size()-1);
            m_valueStr = null;
            m_valueList = connections;
        }

        @NonNull
        public String toString() {
            return m_fieldStr;
        }

        public String fieldStr() {
            return m_fieldStr;
        }

        String valueStr() {
            return m_valueStr;
        }

        public  ArrayListPairString valueListStr() {
            return m_valueList;
        }

        public int getCount() {
            return (m_valueList == null) ? 0 : m_valueList.size();
        }
    }

    // =============================================================================================

    private final static int SUMMARY_LAYOUT = R.layout.build_list_row;
    private final static int SUMMARY_LAYOUT_WIDE = R.layout.build_list_rows_wide;
    /**
     * ExpandableLis UI 'data model' class
     */
    private class NetArrayAdapter extends BaseExpandableListAdapter
            implements View.OnClickListener,  View.OnLongClickListener {
        private final LayoutInflater m_inflater;

        NetArrayAdapter(Context context) {
            m_inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        AdapterView.OnItemLongClickListener m_onItemLongClickListener;
        void setOnItemLongClickListener1( AdapterView.OnItemLongClickListener longClickList) {
            m_onItemLongClickListener = longClickList;
        }

        /**
         * Generated expanded detail view object.
         */
        @Override
        public View getChildView(
                final int groupPosition,
                final int childPosition, boolean isLastChild, View convertView,
                ViewGroup parent) {

            NetInfo netInfo = m_list.get(groupPosition);

            View expandView = convertView;

            if (childPosition < netInfo.valueListStr().size()) {
                String key = netInfo.valueListStr().get(childPosition).first;
                String val = netInfo.valueListStr().get(childPosition).second;

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

            // expandView.setTag(groupPosition);
            // expandView.setOnLongClickListener(this);
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

            NetInfo netInfo = m_list.get(groupPosition);

            View summaryView = convertView;
            if (null == summaryView) {
                summaryView = m_inflater.inflate(SUMMARY_LAYOUT, parent, false);
            }

            TextView textView;
            textView = Ui.viewById(summaryView, R.id.buildField);
            textView.setText("" + groupPosition + " " + netInfo.fieldStr());
            textView.setPadding(10, 0, 0, 0);

            textView = Ui.viewById(summaryView, R.id.buildValue);
            textView.setText(netInfo.valueStr());

            if ((groupPosition & 1) == 1)
                summaryView.setBackgroundColor(m_rowColor1);
            else
                summaryView.setBackgroundColor(m_rowColor2);

            summaryView.setTag(groupPosition);
            summaryView.setOnClickListener(this);
            summaryView.setOnLongClickListener(this);
            return summaryView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        // ============================================================================================

        @Override
        public void onClick(View view) {
            int grpPos = (Integer) view.getTag();

            if (m_listView.isGroupExpanded(grpPos))
                m_listView.collapseGroup(grpPos);
            else
                m_listView.expandGroup(grpPos);
        }

        // ============================================================================================
        // View.OLongClickListener

        @Override
        public boolean onLongClick(View view) {
            int grpPos = (Integer) view.getTag();
            // PackageFragment.this.m_list.get(grpPos).m_checked = ((CheckBox)v).isChecked();
            return m_onItemLongClickListener == null || m_onItemLongClickListener
                    .onItemLongClick(null, view, grpPos, -1);

        }
    }
}