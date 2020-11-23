/*
 * Copyright (c) 2020 Dennis Lang (LanDen Labs) landenlabs@gmail.com
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
 * @author Dennis Lang
 * @see http://LanDenLabs.com/
 */

package com.landenlabs.all_devtool.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnGenericMotionListener;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.landenlabs.all_devtool.R;
import com.landenlabs.all_devtool.TextFragment;
import com.landenlabs.all_devtool.TextInfo;
import com.landenlabs.all_devtool.util.GoogleAnalyticsHelper;
import com.landenlabs.all_devtool.util.Ui;
import com.landenlabs.all_devtool.util.Utils;

import java.util.ArrayList;

/**
 * Show Text font / style example, 10 rows with veritcal pixel height.
 * Swipe to increase/decrease point size.
 *
 * @author Dennis Lang
 *
 */
@SuppressWarnings({"Convert2Lambda", "Anonymous2MethodRef"})
public class TextInfoDialog extends DialogFragment {

    static final int MSG_GET_UI_SIZE = 1;
    static final String STATE_LIST = "my_list";
    static final String STATE_IDX = "my_idx";
    ArrayList<TextInfo> m_textInfoList;
    int m_idx;
    TextView m_textHeight;
    LinearLayout m_textGroup;
    View m_dialogLayout;
    TextView m_charMaxText;
    TextView m_charMaxWidth;
    LinearLayout m_charGroup;
    private final Handler m_handler = new Handler() {

        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MSG_GET_UI_SIZE:
                    setTextSize();
                    break;
            }
        }
    };
    char m_charMaxRange;
    float m_lastTouchX, m_lastTouchY;
    // boolean m_setDownStart = true;

    public TextInfoDialog() {
        m_textInfoList = null;
        m_idx = -1;
    }

    public static TextInfoDialog create(TextFragment textFragment, final ArrayList<TextInfo> textInfoList, final int idx) {
        TextInfoDialog textInfoDialog = new TextInfoDialog();
        textInfoDialog.m_textInfoList = textInfoList;
        textInfoDialog.m_idx = idx;

        GoogleAnalyticsHelper.event(textInfoDialog.getActivity(), "", "dialog", textInfoDialog.getClass().getName());
        return textInfoDialog;
    }

    public static void showDialog(TextFragment textFragment, final ArrayList<TextInfo> textInfoList, final int idx) {
        DialogFragment newFragment = TextInfoDialog.create(textFragment, textInfoList, idx);
        newFragment.show(textFragment.getActivitySafe().getFragmentManager(), "dialog");
    }

    void setTouch(View view) {
        view.setOnGenericMotionListener(new OnGenericMotionListener() {

            @Override
            public boolean onGenericMotion(View v, MotionEvent event) {
                return handleEvent(v, event);
            }
        });
        view.setOnTouchListener(new OnTouchListener() {

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return handleEvent(v, event);
            }
        });
    }

    boolean handleEvent(View view, MotionEvent event) {
        final int action = event.getAction(); // MotionEventCompat.getActionMasked(event);
        // final int pointerIndex = MotionEventCompat.getActionIndex(event);
        final float x = event.getX(); // MotionEventCompat.getX(event, pointerIndex);
        final float y = event.getY(); // MotionEventCompat.getY(event, pointerIndex);
        float dx, dy;

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                m_lastTouchX = x;
                m_lastTouchY = y;
                return true;
            case MotionEvent.ACTION_MOVE:
                // Calculate the distance moved
                dx = x - m_lastTouchX;
                dy = y - m_lastTouchY;
                if (Math.abs(dx) > 40 && Math.abs(dx) > Math.abs(dy)) {
                    m_lastTouchX = x;
                    m_lastTouchY = y;
                    if (dx > 0 && m_idx + 1 < m_textInfoList.size()) {
                        m_idx++;
                        updateDialog();
                    } else if (dx < 0 && m_idx > 0) {
                        m_idx--;
                        updateDialog();
                    }

                }
                if (Math.abs(dy) > 40 && Math.abs(dy) > Math.abs(dx)) {
                    m_lastTouchX = x;
                    m_lastTouchY = y;

                    switch (m_charMaxRange) {
                        case '~':
                            computeMaximumTextWidth('0', '9');
                            break;
                        case '9':
                            computeMaximumTextWidth('A', 'Z');
                            break;
                        case 'Z':
                            computeMaximumTextWidth('a', 'z');
                            break;
                        case 'z':
                            computeMaximumTextWidth(' ', '~');
                            break;
                    }
                    updateDialog();
                }
        }
        return true;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            m_textInfoList = savedInstanceState.getParcelableArrayList(STATE_LIST);
            m_idx = savedInstanceState.getInt(STATE_IDX, m_idx);
        }

        LayoutInflater inflater = getActivity().getLayoutInflater();
        m_dialogLayout = inflater.inflate(R.layout.text_dlg, null);
        m_textHeight = Ui.viewById(m_dialogLayout, R.id.text_height);
        m_textGroup = m_dialogLayout.findViewById(R.id.text_group);
        m_charMaxText = Ui.viewById(m_dialogLayout, R.id.char_max_text);
        m_charMaxWidth = Ui.viewById(m_dialogLayout, R.id.char_max_width);
        m_charGroup =  m_dialogLayout.findViewById(R.id.char_group);

        View shareBtn = Ui.viewById(m_dialogLayout, R.id.text_dlg_share);
        shareBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.shareScreen(m_dialogLayout, "TextDetails", null);
            }
        });

        computeMaximumTextWidth(' ', '~');

        updateDialog();
        setTouch(m_dialogLayout);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(m_dialogLayout);

        builder.setCancelable(false)
                .setPositiveButton("Close",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, close
                                dialog.cancel();
                            }
                        });

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putParcelableArrayList(STATE_LIST, m_textInfoList);
        savedInstanceState.putInt(STATE_IDX, m_idx);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    void setTextSize() {
        int heightPx = m_textGroup.getMeasuredHeight();
        float heightDp = Utils.pxToDp(heightPx);
        m_textHeight.setText(heightPx + " px\n" + String.format("%.1f dp", heightDp));

        int widthPx = m_charGroup.getMeasuredWidth();
        float widthDp = Utils.pxToDp(widthPx);
        m_charMaxWidth.setText("10 characters\n " + widthPx + " px " + String.format("%.1f dp", widthDp));
    }

    void computeMaximumTextWidth(char minChar, char maxChar) {
        // Compute maximum text width
        m_charMaxRange = maxChar;
        Paint paint = new Paint();

        TextInfo textInfo = m_textInfoList.get(m_idx);
        paint.setTypeface(textInfo.getTypeface());// your preference here
        paint.setTextSize(textInfo.m_sizeSP);// have this the same as your text size

        String char10 = "aaaaaaaaaa";
        int maxWidth = 0;
        for (char cValue = minChar; cValue <= maxChar; cValue++) {
            Rect bounds = new Rect();

            String testStr = char10.replace('a', cValue);
            paint.getTextBounds(testStr, 0, testStr.length(), bounds);
            if (bounds.width() > maxWidth) {
                maxWidth = bounds.width();
                m_charMaxText.setText(testStr);
            }
        }
    }

    void updateDialog() {
        if (m_textInfoList != null && m_idx >= 0 && m_idx < m_textInfoList.size()) {
            TextInfo textInfo = m_textInfoList.get(m_idx);
            final TextView textTitle = m_dialogLayout.findViewById(R.id.text_dlg_title);
            String infoStr = "Font typeface:" + textInfo.getTypefaceStr()
                    + "\nFont size:"
                    + textInfo.m_sizeSP
                    + " sp";
            textTitle.setText(infoStr);

            m_textGroup.removeAllViews();

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.gravity=Gravity.RIGHT;

            TextView textView;
            for (int idx = 0; idx < 10; idx++) {
                textView = new TextView(m_textGroup.getContext());
                textView.setText("Normal" + String.format("%02d",  idx+1));
                textView.setTextColor(Color.WHITE);
                textView.setPadding(20,  0,  20,  0);	// set padding left and right.
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textInfo.m_sizeSP);
                textView.setTypeface(textInfo.getTypeface(), Typeface.NORMAL);
                textView.setLayoutParams(params);

                m_textGroup.addView(textView);
            }

            m_charMaxText.setTextSize(TypedValue.COMPLEX_UNIT_SP, textInfo.m_sizeSP);
            m_charMaxText.setTypeface(textInfo.getTypeface(), Typeface.NORMAL);

            Message msgObj = m_handler.obtainMessage(MSG_GET_UI_SIZE);
            m_handler.sendMessageDelayed(msgObj, 500);
        }
    }

}
