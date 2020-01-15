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


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.landenlabs.all_devtool.util.GoogleAnalyticsHelper;
import com.landenlabs.all_devtool.util.Ui;
import com.landenlabs.all_devtool.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * User selectable theme - demonstrates 'look' of several major UI components.
 *
 * @author Dennis Lang
 *
 */

@SuppressWarnings({"Convert2Lambda", "FieldCanBeLocal"})
public class ThemeFragment extends DevFragment implements OnItemSelectedListener,
        OnClickListener {

    public static final String s_name = "Theme";

    private View m_rootView;
    @SuppressWarnings("unused")
    private TextView m_title;
    private Spinner m_theme_spinner;
    private Spinner m_dialog_style_spinner;
    private Button m_dialogBtn;

    public ThemeFragment() {
    }

    public static ThemeFragment create() {
        return new ThemeFragment();
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
        Bitmap bitmap = Utils.grabScreen(getActivitySafe());
        if (null != bitmap && !bitmap.isRecycled())
            bitmapList.add(bitmap);
        else
            Toast.makeText(getActivity(), "Failed to grab screen\nExit App & retry", Toast.LENGTH_LONG).show();
        return bitmapList;
    }

    @Override
    public List<String> getListAsCsv() {
        return null;
    }

    // private GoogleAnalyticsHelper mAnalyticsHelper;

    /** Called when the activity is first created. */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_rootView = inflater.inflate(R.layout.theme_tab, container, false);

        m_title = Ui.viewById(m_rootView, R.id.theme_title);
        // mTitle.setText("Build:" + Build.VERSION.SDK);

        int startThemeIdx = Utils.getThemeIdx();

        m_theme_spinner = Ui.viewById(m_rootView, R.id.theme_spinner);
        m_theme_spinner.setSelection(startThemeIdx);
        m_theme_spinner.setOnItemSelectedListener(this);

        m_dialog_style_spinner = Ui.viewById(m_rootView, R.id.dialog_style_spinner);
        m_dialog_style_spinner.setSelection(startThemeIdx);
        m_dialog_style_spinner.setOnItemSelectedListener(this);

        m_dialogBtn = Ui.viewById(m_rootView, R.id.dialogBtn);
        m_dialogBtn.setOnClickListener(this);

        return m_rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        // mAnalyticsHelper.start(this);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        String themeName = parent.getItemAtPosition(pos).toString();

        // This method is used for both spinner/pull-down menus
        //  1 - Main app theme selector
        //  2 - Dialog theme selector
        if (parent == m_theme_spinner) {
            // m_title.setText(selectionStr);
            Utils.changeToTheme(this.getActivity(), pos, themeName.substring(2));
            GoogleAnalyticsHelper.event(this.getActivity(), "", "ThemeApp", themeName);
        } else if (id == R.id.dialogBtn) {
            openDialog(pos, themeName);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

    @Override
    public void onClick(View v) {
        openDialog(Utils.sNoThemeIdx, GlobalInfo.s_globalInfo.themeName);
    }

    private void openDialog(int style, String title) {
        GoogleAnalyticsHelper.event(this.getActivity(), "", "ThemeDialog", title);

        AlertDialog.Builder alertDialogBuilder;

        if (style != Utils.sNoThemeIdx) {
            // Create dialog with user specified theme.
            ContextThemeWrapper themedContext = new ContextThemeWrapper(
                    this.getActivity(), R.style.AlertDialogStyle_0 + style);
            alertDialogBuilder = new AlertDialog.Builder(themedContext);
        } else {
            // Force specific theme.
            //   ContextThemeWrapper themedContext = new ContextThemeWrapper(this,
            //   android.R.style.Theme_Dialog);

            // Create dialog using app's theme context.
            alertDialogBuilder = new AlertDialog.Builder(this.getActivity());
        }

        // set title
        alertDialogBuilder.setTitle(title);

        // set dialog message
        alertDialogBuilder
                .setMessage("Click yes or no")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

}