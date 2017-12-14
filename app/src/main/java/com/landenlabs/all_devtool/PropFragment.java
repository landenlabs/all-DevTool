package com.landenlabs.all_devtool;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.landenlabs.all_devtool.util.Ui;
import com.landenlabs.all_devtool.util.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.landenlabs.all_devtool.util.SysUtils.runShellCmd;

/**
 * Show system property information
 * <p>
 * Created by Dennis Lang on 8/20/17.
 */

public class PropFragment extends DevFragment {

    final ArrayList<GroupInfo> m_list = new ArrayList<>();
    ExpandableListView m_listView;
    EditText m_titleTime;
    ImageButton m_search;
    View m_refresh;
    String m_filter;

    Map<String, String> m_propList;

    public static String s_name = "Prop";
    private static SimpleDateFormat m_timeFormat = new SimpleDateFormat("HH:mm:ss zz");


    public PropFragment() {
    }

    public static DevFragment create() {
        return new PropFragment();
    }

    // ============================================================================================
    // DevFragment methods

    @Override
    public String getName() {
        return s_name;
    }

    @Override
    public List<Bitmap> getBitmaps(int maxHeight) {
        return null; // Utils.getListViewAsBitmaps(m_listView, maxHeight);
    }

    @Override
    public List<String> getListAsCsv() {
        return Utils.getListViewAsCSV(m_listView);
    }

    // ============================================================================================
    // Fragment methods

    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View rootView = inflater.inflate(R.layout.prop_tab, container, false);
        Ui.<TextView>viewById(rootView, R.id.list_title).setText(R.string.prop_title);
        m_listView = Ui.viewById(rootView, R.id.propListView);

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

        if (TextUtils.isEmpty(m_filter) || m_filter.equals("*") || m_titleTime.getText().length() == 0) {
            // Time today = new Time(Time.getCurrentTimezone());
            // today.setToNow();
            // today.format(" %H:%M:%S")
            Date dt = new Date();
            m_titleTime.setText(m_timeFormat.format(dt));
            m_filter = "";
        }

        boolean firstTime = m_list.isEmpty();
        m_list.clear();

        // -----------------------------------------------------------------------------------------
        // Collect property values

        m_propList = getShellCmd(new String[]{"getprop"});
        Map<String, Map<String, String>> keyLists = new HashMap<>();
        for ( Map.Entry<String, String> item : m_propList.entrySet()) {
            String key = item.getKey().replace("[", "").replace("]", "");
            String[] split = key.split("\\.", 2);

            if (split.length == 2) {
                Map<String, String> list = keyLists.get(split[0]);
                if (list == null) {
                    list = new HashMap<>();
                }
                list.put(split[1], item.getValue());
                keyLists.put(split[0], list);
            }
        }

        for (Map.Entry<String, Map<String, String>> item : keyLists.entrySet()){
            addString(item.getKey(), item.getValue());
        }

        // -----------------------------------------------------------------------------------------
        // Collect Security values

        Map<String, String> secureList = new LinkedHashMap<>();
        Field[] fields = Settings.Secure.class.getDeclaredFields();
        for (Field f : fields) {
            if (Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())) {

                //  && isRightName(f.getName())
                String fieldType = f.getType().getName();
                if (fieldType.equals(String.class.getName())) {
                    String key = f.toString();
                    try {
                        key = f.get(null).toString();
                        String value =
                                Settings.Secure.getString(getContext().getContentResolver(), key);
                        if (!TextUtils.isEmpty(value)) {
                            secureList.put(key, value);
                        }
                    } catch (Exception ex) {

                    }
                }
            }
        }
        addString("Secure", secureList);

        // -----------------------------------------------------------------------------------------

        if (firstTime ||
                !(m_listView.getExpandableListAdapter() instanceof  BaseExpandableListAdapter)) {
            final BuildArrayAdapter adapter = new BuildArrayAdapter(this.getActivity());
            m_listView.setAdapter(adapter);

            int count = adapter.getGroupCount();
            for (int position = 0; position < count; position++)
                m_listView.expandGroup(position);
        }

        // m_listView.invalidate();
        ((BaseExpandableListAdapter) m_listView.getExpandableListAdapter())
                    .notifyDataSetChanged();
    }

    // ============================================================================================
    // Internal methods

    void addString(String name, String value) {
        if (!TextUtils.isEmpty(value))
            m_list.add(new GroupInfo(name, value.trim()));
    }

    void addString(String name, Map<String, String> value) {
        if (!value.isEmpty())
            m_list.add(new GroupInfo(name, value));
    }

    private Map<String, String> getShellCmd(String[] shellCmd) {
        Map<String, String> mapList = new LinkedHashMap<>();
        ArrayList<String> responseList = runShellCmd(shellCmd);
        for (String line : responseList) {
            String[] vals = line.split(": ");
            if (vals.length > 1) {
                mapList.put(vals[0], vals[1]);
            } else {
                mapList.put(line, "");
            }
        }
        return mapList;
    }


    // =============================================================================================


    class GroupInfo {
        final String m_fieldStr;
        final String m_valueStr;
        final Map<String, String> m_valueList;

        GroupInfo() {
            m_fieldStr = m_valueStr = null;
            m_valueList = null;
        }

        GroupInfo(String str1, String str2) {
            m_fieldStr = str1;
            m_valueStr = str2;
            m_valueList = null;
        }

        GroupInfo(String str1, Map<String, String> list2) {
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

    final static int EXPANDED_LAYOUT = R.layout.build_list_row;
    final static int SUMMARY_LAYOUT = R.layout.build_list_row;

    /**
     * ExpandableLis UI 'data model' class
     */
    private class BuildArrayAdapter extends BaseExpandableListAdapter
            implements View.OnClickListener {
        private final LayoutInflater m_inflater;

        public BuildArrayAdapter(Context context) {
            m_inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        /**
         * Generated expanded detail view object.
         */
        @Override
        public View getChildView(
                final int groupPosition, final int childPosition, boolean isLastChild,
                View convertView, ViewGroup parent) {

            GroupInfo buildInfo = m_list.get(groupPosition);

            View expandView = convertView;
            // if (null == expandView) {
            expandView = m_inflater.inflate(SUMMARY_LAYOUT, parent, false);
            // }

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
                    || Utils.containsIgnoreCase(text, m_filter))) {
                expandView.setBackgroundColor(0x80ffff00);
            } else {

                if ((groupPosition & 1) == 1)
                    expandView.setBackgroundColor(0);
                else
                    expandView.setBackgroundColor(0x80d0ffe0);
            }

            expandView.setTag(Integer.valueOf(groupPosition));
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
                int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

            GroupInfo buildInfo = m_list.get(groupPosition);

            View summaryView = convertView;
            if (null == summaryView) {
                summaryView = m_inflater.inflate(SUMMARY_LAYOUT, parent, false);
            }

            TextView textView = Ui.viewById(summaryView, R.id.buildField);
            textView.setText(buildInfo.fieldStr());
            textView.setPadding(10, 0, 0, 0);
            textView.setTypeface(Typeface.MONOSPACE);

            textView = Ui.viewById(summaryView, R.id.buildValue);
            textView.setText(buildInfo.valueStr());

            if ((groupPosition & 1) == 1)
                summaryView.setBackgroundColor(0);
            else
                summaryView.setBackgroundColor(0x80d0ffe0);

            summaryView.setTag(Integer.valueOf(groupPosition));
            summaryView.setOnClickListener(this);
            return summaryView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        @Override
        public void onClick(View view) {
            int grpPos = ((Integer) view.getTag()).intValue();

            if (m_listView.isGroupExpanded(grpPos))
                m_listView.collapseGroup(grpPos);
            else
                m_listView.expandGroup(grpPos);
        }
    }
}