/*
 * Copyright (c) 2026 Dennis Lang (LanDen Labs)
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
 * @see https://LanDenLabs.com/
 */

package com.landenlabs.all_devtool;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentActivity;

import com.landenlabs.all_devtool.dialogs.DrawView;
import com.landenlabs.all_devtool.shortcuts.util.Ui;
import com.landenlabs.all_devtool.shortcuts.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Display screen pixel and DP dimensions.
 *
 * @author Dennis Lang
 *
 */
@SuppressWarnings("Convert2Lambda")
public class ScreenFragment extends DevFragment {

    public static final String s_name = "Screen";

    private FragmentActivity m_context;
    private View m_rootView;

    private LinearLayout m_layout;
    private TextView m_screenDevice;
    private ImageView m_horzWindowArrow;
    private ImageView m_vertPanelArrow;
    private TextView m_horzWindowText;
    private TextView m_vertPanelText;
    private DrawView m_drawPoints;
    private Drawable m_actionBarBackground;

    private DisplayMetrics m_displayMetrics;

    private static final int MSG_GET_UI_SIZE = 1;
    private final Handler m_handler = new Handler(Looper.getMainLooper()) {

        @SuppressWarnings("SwitchStatementWithTooFewBranches")
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MSG_GET_UI_SIZE:
                    setPanelSize();
                    break;
            }
        }
    };

    public ScreenFragment() {
    }

    public static ScreenFragment create() {
        return new ScreenFragment();
    }

    // ============================================================================================
    // Override DevFragment

    @Override
    public String getName() {
        return s_name;
    }

    @Override
    public List<Bitmap> getBitmaps(int maxHeight) {
        List<Bitmap> bitmapList = new ArrayList<>();
        bitmapList.add(Utils.grabScreen(requireActivity()));
        return bitmapList;
    }

    @Override
    public List<String> getListAsCsv() {
        return null;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Ui.viewById(m_layout, R.id.touch_pos).setVisibility(View.INVISIBLE);
            Ui.viewById(m_layout, R.id.grid_size).setVisibility(View.INVISIBLE);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Ui.viewById(m_layout, R.id.touch_pos).setVisibility(View.VISIBLE);
            Ui.viewById(m_layout, R.id.grid_size).setVisibility(View.VISIBLE);
        }
        updateView();
    }

    // ============================================================================================
    // Override DevFragment(Fragment)


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // This tells the app to draw behind the system bars (draw edge-to-edge)
        // WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        m_context = this.getActivity();
        //	m_context.setTheme(R.style.Theme_TranslucentActionBar_ActionBar_Overlay);
        m_rootView = inflater.inflate(R.layout.screen_tab, container, false);


        // Apply a listener to the root view of the fragment's layout
        ViewCompat.setOnApplyWindowInsetsListener(m_rootView, (v, windowInsets) -> {
            // Get the insets for the system bars (Status Bar + Navigation Bar)
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            int topPad = ScreenFragment.s_name.equals(getName()) ? 0 : insets.top;

            // Apply padding to the top of your view to avoid the Status Bar
            v.setPadding(v.getPaddingLeft(), topPad, v.getPaddingRight(), v.getPaddingBottom());

            // You might need to apply padding/margin to the bottom to avoid the Navigation Bar
            // v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), insets.bottom);

            // Return the insets so they can be consumed by the system
            return windowInsets;
        });

        updateView();

        return m_rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (requireActivity() instanceof AppCompatActivity appCompatActivity) {
            if (appCompatActivity.getSupportActionBar() != null) {
                // appCompatActivity.getSupportActionBar().hide();
                // m_actionBarBackground = appCompatActivity.getSupportActionBar().getCustomView().getBackground();
                appCompatActivity.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        }
        // WindowCompat.setDecorFitsSystemWindows(requireActivity().getWindow(), false);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (requireActivity() instanceof AppCompatActivity appCompatActivity) {
            if (appCompatActivity.getSupportActionBar() != null) {
                // appCompatActivity.getSupportActionBar().show();
                // appCompatActivity.getSupportActionBar().setBackgroundDrawable(m_actionBarBackground);
            }
        }
        // WindowCompat.setDecorFitsSystemWindows(requireActivity().getWindow(), true);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // super.onViewCreated(view, savedInstanceState);
        cacheFragment();
        if (GlobalInfo.s_globalInfo.haveActionBarOverlay) {
            RelativeLayout.LayoutParams relParams = (RelativeLayout.LayoutParams) m_screenDevice.getLayoutParams();
            int padTop = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ?
                    GlobalInfo.s_globalInfo.actionBarHeight * 2 : GlobalInfo.s_globalInfo.actionBarHeight;
            relParams.setMargins(0, padTop, 0, 0);
        }
    }

    @Override
    public boolean onMenuSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.screen_clear_menu) {
            m_drawPoints.clear();
            m_drawPoints.invalidate();
        } else if (id == R.id.screen_freeze_menu) {
            m_drawPoints.setAutoPrune(false);
        } else if (id == R.id.screen_prune_menu) {
            m_drawPoints.setAutoPrune(true);
        }

        item.setChecked(true);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMenuCreate(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.screen_menu, menu.addSubMenu("Screen Options"));

        menu.findItem(R.id.screen_prune_menu).setChecked(m_drawPoints.getAutoPrune());
    }

    // ============================================================================================
    // Local methods

    void updateView() {
        m_displayMetrics = Utils.getDisplayMetrics(m_context);

        m_layout = Ui.viewById(m_rootView, R.id.screen_layout);
        m_screenDevice = Ui.viewById(m_rootView, R.id.screen_device);

        TextView screenSizeText = Ui.viewById(m_rootView, R.id.screen_size);
        TextView screenDensityText = Ui.viewById(m_rootView, R.id.screen_density);

        m_screenDevice.setText(Build.MODEL);
        int widthPx = m_displayMetrics.widthPixels;
        int heightPx = m_displayMetrics.heightPixels;
        String sizeStr = String.format(Locale.getDefault(),
                "%.0f dp x %.0f dp\n%d px x %d px\n%.1f in x %.1f in",
                Utils.pxToDp(widthPx), Utils.pxToDp(heightPx),
                widthPx, heightPx,
                (float) widthPx / m_displayMetrics.densityDpi,
                (float) heightPx / m_displayMetrics.densityDpi);
        screenSizeText.setText(sizeStr);

        String densityStr;
        if (m_displayMetrics.densityDpi <= DisplayMetrics.DENSITY_MEDIUM)
            densityStr = "Medium";
        else if (m_displayMetrics.densityDpi <= DisplayMetrics.DENSITY_HIGH)
            densityStr = "High";
        else if (m_displayMetrics.densityDpi <= DisplayMetrics.DENSITY_XHIGH)
            densityStr = "x-High";
        else if (m_displayMetrics.densityDpi <= DisplayMetrics.DENSITY_XXHIGH)
            densityStr = "xx-High";
        else if (m_displayMetrics.densityDpi <= DisplayMetrics.DENSITY_XXXHIGH)
            densityStr = "xxx-High";
        else
            densityStr = "xxxx-High";

        screenDensityText.setText(String.format("Density %s(%d) px/dp=%.2f",
                densityStr, m_displayMetrics.densityDpi, m_displayMetrics.density));

        m_horzWindowArrow = Ui.viewById(m_rootView, R.id.horz_arrow);
        m_vertPanelArrow = Ui.viewById(m_rootView, R.id.vert_panel_arrow);

        m_horzWindowText = Ui.viewById(m_rootView, R.id.horz_arrow_text);
        m_vertPanelText = Ui.viewById(m_rootView, R.id.vert_panel_text);

        TextView themeTv = Ui.viewById(m_rootView, R.id.theme);
        // themeTv.setRotation(-90);
        String themeName = GlobalInfo.s_globalInfo.themeName;
        if (!themeName.equals("Theme.Holo"))
            themeName = themeName + "\nBest with Theme.Halo";
        themeTv.setText(themeName);

        /*
         * Dynamically get theme - always returns 'AppTheme' which is set in style
            try {
                PackageInfo packageInfo = getPackageMgr().getPackageInfo(GlobalInfo.s_globalInfo.pkgName, 0);
                 int themeResId = packageInfo.applicationInfo.theme;
                 String themeName = getResources().getResourceEntryName(themeResId);
                 themeTv.setText(themeName);
            } catch (Exception ex) {
                themeTv.setVisibility(View.GONE);
            }
        */

        Message msgObj = m_handler.obtainMessage(MSG_GET_UI_SIZE);
        m_handler.sendMessageDelayed(msgObj, 1000);

        final TextView touchPos = Ui.viewById(m_rootView, R.id.touch_pos);
        m_drawPoints = Ui.viewById(m_rootView, R.id.drawPoints);
        m_drawPoints.setOnTouchInfo(new DrawView.TouchInfo() {
            public void onTouchInfo(MotionEvent event) {
                touchPos.setText(String.format("%.0f,%.0f", event.getX(), event.getY()));
            }
        });
    }
    /**
     *
     * @return Display Metrics.
     */
    DisplayMetrics getDisplayMetrics() {
        if (m_displayMetrics == null)
            m_displayMetrics = Utils.getDisplayMetrics(GlobalInfo.s_globalInfo.mainFragActivity);
        return m_displayMetrics;
    }

    /**
     * Layout has completed and now we can display the correct screen dimensions.
     */
    void setPanelSize() {
        int heightPx = m_vertPanelArrow.getHeight();
        float heightDp = Utils.pxToDp(heightPx);

        m_vertPanelText.setText(String.format("%d px\n%.0f dp\n%.1f in",
                heightPx, heightDp, (float) heightPx / getDisplayMetrics().densityDpi));

        int widthPx = m_horzWindowArrow.getWidth();
        float widthDp = Utils.pxToDp(widthPx);
        m_horzWindowText.setText(String.format("%d px|%.0f dp|%.1f in",
                widthPx, widthDp, (float) widthPx / getDisplayMetrics().densityDpi));
    }
}
