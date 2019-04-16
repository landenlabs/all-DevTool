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

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.landenlabs.all_devtool.util.Ui;
import com.landenlabs.all_devtool.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Display "Build" system information.
 *
 * @author Dennis Lang
 */
public class BuildFragment extends DevFragment {

    final ArrayList<BuildInfo> m_list = new ArrayList<>();
    ExpandableListView m_listView;

    public static String s_name = "Build";

    public BuildFragment() {
    }

    public static DevFragment create() {
        return new BuildFragment();
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

// ============================================================================================
    // Fragment methods

    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View rootView = inflater.inflate(R.layout.build_tab, container, false);
        Ui.<TextView>viewById(rootView, R.id.list_title).setText(R.string.build_title);
        m_listView = Ui.viewById(rootView, R.id.buildListView);

        return rootView;
    }

    // Coming into forground - update list.
    @Override
    public void onResume() {
        super.onResume();
        updateList();
    }

    // ============================================================================================
    // Internal methods

    /**
     * Populate list with 'Build' parameters.
     */
    void updateList() {
        if (m_list.isEmpty()) {

            addBuild("BOARD", Build.BOARD);
            addBuild("BOOTLOADER", Build.BOOTLOADER);
            addBuild("BRAND", Build.BRAND);
            addBuild("CPU_ABI", Build.CPU_ABI);
            addBuild("CPU_ABI2", Build.CPU_ABI2);
            addBuild("OS.ARCH", System.getProperty("os.arch"));
            if (Build.VERSION.SDK_INT >= 21) {
                // addBuild("SUPPORTED_ABIS", Arrays.toString(Build.SUPPORTED_ABIS));
                if (Build.SUPPORTED_32_BIT_ABIS != null && Build.SUPPORTED_32_BIT_ABIS.length > 0) {
                    addBuild("32_BIT_ABIS", Arrays.toString(Build.SUPPORTED_32_BIT_ABIS));
                } else {
                    addBuild("64_BIT_ABIS", Arrays.toString(Build.SUPPORTED_64_BIT_ABIS));
                }
            }

            addBuild("DEVICE", Build.DEVICE);
            addBuild("DISPLAY", Build.DISPLAY);
            addBuild("FINGERPRINT", Build.FINGERPRINT);
            addBuild("HARDWARE", Build.HARDWARE);
            addBuild("HOST", Build.HOST);
            addBuild("ID", Build.ID);
            addBuild("MANUFACTURER", Build.MANUFACTURER);
            addBuild("MODEL", Build.MODEL);
            addBuild("PRODUCT", Build.PRODUCT);
            // addBuild("RADIO", Build.RADIO);
            addBuild("SERIAL", Build.SERIAL);
            addBuild("TAGS", Build.TAGS);
            addBuild("TYPE", Build.TYPE);
            addBuild("UNKNOWN", Build.UNKNOWN);
            addBuild("USER", Build.USER);
            Map<String, String> listStr = new HashMap<>();
            // listStr.put("BASE", Build.VERSION.BASE_OS);
            listStr.put("CODENAME", Build.VERSION.CODENAME);
            listStr.put("INCREMENTAL", Build.VERSION.INCREMENTAL);
            listStr.put("RELEASE", Build.VERSION.RELEASE);
            //noinspection deprecation
            listStr.put("SDK", Build.VERSION.SDK);

            addBuild("VERSION...", listStr);

            PowerManager pm = getServiceSafe(Context.POWER_SERVICE);
            if (Build.VERSION.SDK_INT >= 21)
                addBuild("Power Save Mode",
                        pm.isPowerSaveMode() ? "yes" : "no");
            if (Build.VERSION.SDK_INT >= 23)
                addBuild("Is Idel Mode", pm.isDeviceIdleMode() ? "yes" : "no");
            if (Build.VERSION.SDK_INT >= 24)
                addBuild("Has Sustained Pwr Mode",
                        pm.isSustainedPerformanceModeSupported() ? "yes" : "no");
            // activity.getWindow().setSustainedPerformanceMode(true)

        }
        final BuildArrayAdapter adapter = new BuildArrayAdapter(getActivitySafe());
        m_listView.setAdapter(adapter);

        int count = adapter.getGroupCount();
        for (int position = 0; position < count; position++)
            m_listView.expandGroup(position);

        m_listView.invalidate();
    }

    void addBuild(String name, String value) {
        if (!TextUtils.isEmpty(value))
            m_list.add(new BuildInfo(name, value.trim()));
    }

    @SuppressWarnings("SameParameterValue")
    void addBuild(String name, Map<String, String> value) {
        if (!value.isEmpty())
            m_list.add(new BuildInfo(name, value));
    }

    @SuppressWarnings("unused")
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

        @NonNull
        public String toString() {
            return (m_fieldStr != null) ? m_fieldStr : "";
        }

        public String fieldStr() {
            return m_fieldStr;
        }

        String valueStr() {
            return m_valueStr;
        }

        Map<String, String> valueListStr() {
            return m_valueList;
        }

        public int getCount() {
            return (m_valueList == null) ? 0 : m_valueList.size();
        }
    }

    final static int SUMMARY_LAYOUT = R.layout.build_list_row;

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

            View expandView; // = convertView;
            // if (null == expandView) {
                expandView = m_inflater.inflate(SUMMARY_LAYOUT, parent, false);
            // }

            String key = (String) buildInfo.valueListStr().keySet().toArray()[childPosition];
            String val = buildInfo.valueListStr().get(key);

            TextView textView = Ui.viewById(expandView, R.id.buildField);

            // textView.setSelectAllOnFocus(true);
            textView.setText(key);
            textView.setPadding(40, 0, 0, 0);

            textView = Ui.viewById(expandView, R.id.buildValue);
            textView.setText(val);

            if ((groupPosition & 1) == 1)
                expandView.setBackgroundColor(0);
            else
                expandView.setBackgroundColor(0x80d0ffe0);

            expandView.setTag(groupPosition);
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

            TextView textView = Ui.viewById(summaryView, R.id.buildField);
            textView.setText(buildInfo.fieldStr());
            textView.setPadding(10, 0, 0, 0);

            textView = Ui.viewById(summaryView, R.id.buildValue);
            textView.setText(buildInfo.valueStr());

            if ((groupPosition & 1) == 1)
                summaryView.setBackgroundColor(0);
            else
                summaryView.setBackgroundColor(0x80d0ffe0);

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
            int grpPos = (Integer)view.getTag();

            if (m_listView.isGroupExpanded(grpPos))
                m_listView.collapseGroup(grpPos);
            else
                m_listView.expandGroup(grpPos);
        }
    }
}