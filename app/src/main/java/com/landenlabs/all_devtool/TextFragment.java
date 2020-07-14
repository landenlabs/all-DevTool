package com.landenlabs.all_devtool;

/*
 * Copyright (c) 2015 - 2020 Dennis Lang (LanDen Labs) landenlabs@gmail.com
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


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.provider.Settings;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.landenlabs.all_devtool.dialogs.TextInfoDialog;
import com.landenlabs.all_devtool.util.Ui;
import com.landenlabs.all_devtool.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Display Text / Font / Style examples and how much vertical space they use.
 *
 * <ul>
 * <li> click on font to see 10 line sample
 * <li> swipe on sample to advance/decrease point size
 * </ul>
 *
 * @author Dennis Lang
 *
 */
@SuppressWarnings("Convert2Lambda")
public class TextFragment extends DevFragment {

    public static final String s_name = "Text";
    private FragmentActivity m_context;
    private TableLayout m_tableLayout;
    private final ArrayList<TextInfo> m_textInfoList = new ArrayList<>();
    private View m_rootView;

    /*
    public static final int s_MSG_SHARE_PATH_KEY = 1;
    public static final String s_MSG_SHARE_PATH = "path";

    public final Handler m_handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case s_MSG_SHARE_PATH_KEY:
                    TabPagerAdapter.sharePage(msg.getData().getString(s_MSG_SHARE_PATH));
                    break;
            }
        }
    };
    */

    public TextFragment() {
    }

    public static TextFragment create() {
        return new TextFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        TextView fontScale = Ui.viewById(m_rootView, R.id.text_tab_font_scale);
        fontScale.setText(  getString( R.string.text_font_scale, getResources().getConfiguration().fontScale));
    }

    // ============================================================================================
    // DevFragment methods

    @Override
    public String getName() {
        return s_name;
    }

    @Override
    public List<Bitmap> getBitmaps(int maxHeight) {
        List<Bitmap> images = Utils.getTableLayoutAsBitmaps(m_tableLayout, 3000);
        fillLayout();   // rebuild because getTable... corrupts layout
        return images;
        // List bitmapList = new ArrayList<Bitmap>();
        // bitmapList.add(Utils.grabScreen(this.getActivity()));
        // return bitmapList;
    }

    @Override
    public List<String> getListAsCsv() {
        return null;
    }

    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_rootView = inflater.inflate(R.layout.text_tab, container, false);
        m_context = this.getActivity();

        TextView fontScale = Ui.viewById(m_rootView, R.id.text_tab_font_scale);
        fontScale.setText(  getString( R.string.text_font_scale, getResources().getConfiguration().fontScale));
        fontScale.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_DISPLAY_SETTINGS));
            }
        });
        m_tableLayout = Ui.viewById(m_rootView, R.id.text_tablelayout);

        fillLayout();

        return m_rootView;
    }

    private void fillLayout() {
        m_tableLayout.removeAllViews();
        m_textInfoList.clear();

        int minSP = 8;
        int maxSP = 20;
        int stepSP = 2;

        int[] colors = new int[]{0xffe0e0e0, 0xffffe0e0, 0xffe0ffe0, 0xffe0e0ff};

        TableLayout.LayoutParams tableLP =
                new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams rowLP =
                new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT);

        TextView textView;
        TableRow tableRow;
        int textColor = 0xff000000;

        for (int tfIdx = 0; tfIdx < TextInfo.getCount(); tfIdx++) {
            Typeface typeface = TextInfo.getTypeface(tfIdx);
            String typefaceStr = TextInfo.getTypefaceStr(tfIdx);

            textView = new TextView(m_context);
            textView.setBackgroundColor(Utils.blend(colors[tfIdx], 0x20000000));
            textView.setText(typefaceStr);
            textView.setGravity(Gravity.CENTER);
            textView.setTextColor(textColor);
            m_tableLayout.addView(textView, tableLP);

            for (int sizeSP = minSP; sizeSP <= maxSP; sizeSP += stepSP) {
                tableRow = new TableRow(m_context);
                tableRow.setBackgroundColor(colors[tfIdx]);

                tableRow.setTag(m_textInfoList.size());
                m_textInfoList.add(new TextInfo(sizeSP, tfIdx));

                tableRow.setClickable(true);
                tableRow.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        int idx = (Integer) v.getTag();
                        showTextDialog(m_textInfoList, idx);
                    }
                });

                textView = new TextView(m_context);
                textView.setText(String.valueOf(sizeSP) + "sp ");
                textView.setBackgroundColor(0x20000000);
                textView.setPadding(8, 0, 8, 0);
                textView.setGravity(Gravity.CENTER);
                textView.setTextColor(textColor);
                tableRow.addView(textView, rowLP);

                textView = new TextView(m_context);
                textView.setText("Normal");
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSP);
                textView.setTypeface(typeface, Typeface.NORMAL);
                textView.setGravity(Gravity.CENTER);
                textView.setTextColor(textColor);
                tableRow.addView(textView, rowLP);

                textView = new TextView(m_context);
                textView.setText("Bold");
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSP);
                textView.setTypeface(typeface, Typeface.BOLD);
                textView.setPadding(8, 0, 8, 0);
                textView.setGravity(Gravity.CENTER);
                textView.setTextColor(textColor);
                tableRow.addView(textView, rowLP);

                textView = new TextView(m_context);
                textView.setText("Italic");
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSP);
                textView.setTypeface(typeface, Typeface.ITALIC);
                textView.setGravity(Gravity.CENTER);
                textView.setTextColor(textColor);
                tableRow.addView(textView, rowLP);

                m_tableLayout.addView(tableRow, tableLP);
            }
        }
    }

    private void showTextDialog(final ArrayList<TextInfo> textInfoList, final int idx) {
        TextInfoDialog.showDialog(this, textInfoList, idx);
    }

}