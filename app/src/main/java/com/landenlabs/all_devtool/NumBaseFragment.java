package com.landenlabs.all_devtool;

/*
 * Copyright (c) 2016 Dennis Lang (LanDen Labs) landenlabs@gmail.com
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * @author Dennis Lang  (3/21/2015)
 * @see http://LanDenLabs.com/
 */


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.landenlabs.all_devtool.util.LLog;
import com.landenlabs.all_devtool.util.Ui;
import com.landenlabs.all_devtool.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Base class for Numeric asset display.
 *
 * @author Dennis Lang
 *
 */
public abstract class NumBaseFragment extends DevFragment {
    // Logger - set to LLog.DBG to only log in Debug build, use LLog.On for always log.
    private final LLog m_log = LLog.DBG;

    final ArrayList<NumInfo> m_list = new ArrayList<NumInfo>();
    ListView m_listView;
    FragmentActivity m_context;
    int m_backgroundColor = -1;
    int m_alternateColor = -1;

    // ============================================================================================
    // Abstract interfaces

    public abstract void addToList();

    // ============================================================================================
    // DevFragment overrides

    @Override
    public List<Bitmap> getBitmaps(int maxHeight) {
        Toast.makeText(getActivity(), String.format("Please wait while\n %d items extracted...",
                m_listView.getCount()), Toast.LENGTH_LONG).show();
        return Utils.getListViewAsBitmaps(m_listView, maxHeight);
    }

    // ============================================================================================
    // DevFragment(Fragment) overrides

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View rootView = inflater.inflate(R.layout.num_tab, container, false);
        m_context = this.getActivity();

        m_listView = Ui.viewById(rootView, R.id.numListView);
        m_listView.removeAllViewsInLayout();

        int[] attrs = {android.R.id.background};
        TypedArray ta = m_context.getTheme().obtainStyledAttributes(attrs);
        m_backgroundColor = ta.getColor(0, -1);
        ta.recycle();
        m_alternateColor = Utils.blend(m_backgroundColor, 0x80d0ffe0);

        m_list.clear();
        addToList();
        Collections.sort(m_list, new Comparator<NumInfo>() {
            @Override
            public int compare(NumInfo item1, NumInfo item2) {
                return item1.fieldStr().compareTo(item2.fieldStr());
            }
        });

        final NumArrayAdapter adapter = new NumArrayAdapter(
                this.getActivity(), R.layout.num_list_row, R.id.numField,
                m_list);
        m_listView.setAdapter(adapter);

        return rootView;
    }

    // ============================================================================================
    // NumBaseFragment methods

    protected void addNum(String name, int attrId) {
        try {
            addNum(name, attrId, null);
        } catch (Exception ex) {
            // Failed add asset to list.
        }
    }

    protected void addNum(String name, int attrId, String numType) {
        int[] attrs = {attrId};
        TypedArray typedArray = m_context.getTheme().obtainStyledAttributes(attrs);
        String str = "";

        if (typedArray != null) {
            TypedValue typedValue = new TypedValue();

            try {
                str = "???";
                int cnt = typedArray.getIndexCount();
                for (int idx = 0; idx != cnt; idx++) {
                    int attrIdx = typedArray.getIndex(idx);
                    str = typedArray.getString(attrIdx);

                    int refId = typedArray.getResourceId(attrIdx, -1);
                    if (refId != -1 && refId != attrId) {
                        addNum(name, refId, numType);
                    }

                    if (TextUtils.isEmpty(str)) {
                        float val = typedArray.getDimension(0, -1);
                        if (val != -1)
                            str = String.valueOf(val);
                    }
                    if (!TextUtils.isEmpty(str))
                        m_list.add(new NumInfo(name, str, numType));
                }
                if (cnt == 0) {
                    if (m_context.getTheme().resolveAttribute(attrId, typedValue, true)) {
                        str = (String) typedValue.coerceToString();
                        m_list.add(new NumInfo(name, str, numType));
                    }
                }
            } catch (Exception ex) {
                m_log.e("peekValue", ex);
            }

            typedArray.recycle();
        }
    }


    // ============================================================================================
    // Internal class(es)

    private class NumInfo {
        final String m_fieldStr;
        final String m_value;
        final String m_fieldType;

        NumInfo(String str1, String value, String fieldType) {
            m_fieldStr = str1;
            m_value = value;
            m_fieldType = fieldType;
        }

        public String toString() {
            return m_fieldStr;
        }

        public String fieldStr() {
            return m_fieldStr;
        }

        public String getValue() {
            return m_value;
        }

        public String getType() {
            return m_fieldType;
        }
    }

    private class NumArrayAdapter extends ArrayAdapter<NumInfo> {

        public NumArrayAdapter(Context context, int rowLayoutId,
                               int textViewResourceId, List<NumInfo> objects) {
            super(context, rowLayoutId, textViewResourceId, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            NumInfo numInfo = getItem(position);

            TextView textView = Ui.viewById(view, R.id.numValue);
            textView.setText(numInfo.getValue());

            textView = Ui.viewById(view, R.id.numType);
            textView.setText(numInfo.getType());


            if ((position & 1) == 1)
                view.setBackgroundColor(m_backgroundColor);
            else
                view.setBackgroundColor(m_alternateColor);

            return view;
        }
    }
}