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


import android.app.Activity;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.TypedValue;
import android.widget.ShareActionProvider;

/**
 * Manage global app information in singleton class.
 *
 * @author Dennis Lang
 */
@SuppressWarnings("WeakerAccess")
public class GlobalInfo {

    public final static GlobalInfo s_globalInfo = new GlobalInfo();

    public final static String STARTUP_FRAG = "StartupFrag";

    // Main global info - set by startup activity DevToolActivity.
    public String appName = "DevTool";
    public String pkgName;
    public String version;
    public boolean isDebug = false;
    public boolean isLockedOrientation = false;
    public int lockedOrientation = 0;

    public DevToolActivity mainFragActivity;
    public TabPagerAdapter tabAdapter;
    public ShareActionProvider shareActionProvider;
    public String themeName = "Theme.Holo";  // Default theme set in our style.

    // Used to draw transparent 'screen' fragment.
    public Drawable actionBarBackground;
    public int actionBarHeight;
    public boolean haveActionBar = false;
    public boolean haveActionBarOverlay = false;

    public static void grabThemeSetings(Activity activity) {
        GlobalInfo.s_globalInfo.haveActionBar = (activity.getActionBar() != null);
        if (GlobalInfo.s_globalInfo.haveActionBar) {

            Theme actionBarTheme = activity.getActionBar().getThemedContext().getTheme();
            {
                int[] attrs = {android.R.attr.actionModeBackground};
                TypedArray typedArray = actionBarTheme.obtainStyledAttributes(attrs);
                int cnt = typedArray.getIndexCount();
                if (cnt != 0) {
                    int attrIdx = typedArray.getIndex(0);
                    // String str = typedArray.getString(attrIdx);
                    GlobalInfo.s_globalInfo.actionBarBackground = typedArray.getDrawable(attrIdx);
                }
                typedArray.recycle();
            }

            // int[] attrs = { android.R.attr.actionBarSize };

            TypedValue tv = new TypedValue();
            if (activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                GlobalInfo.s_globalInfo.actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,
                        activity.getResources().getDisplayMetrics());
            }

            // if (activity.getTheme().resolveAttribute(android.R.attr.windowActionBarOverlay, tv, true))
            {
                int[] attrs = {android.R.attr.windowActionBarOverlay};
                TypedArray typedArray = actionBarTheme.obtainStyledAttributes(attrs);
                int cnt = typedArray.getIndexCount();
                if (cnt != 0) {
                    int attrIdx = typedArray.getIndex(0);
                    GlobalInfo.s_globalInfo.haveActionBarOverlay = typedArray.getBoolean(attrIdx, false);
                }
                typedArray.recycle();
            }
        } else {
            GlobalInfo.s_globalInfo.actionBarHeight = 0;
        }

    }

    /**
     * Return theme resource for theme index.
     * @param themeIdx (0..10), if api >=21 (0..13)
     */
    public static int getThemeResId(int themeIdx) {
        // return themeIdx + R.style.Theme_00;
        switch (themeIdx) {
            case 0: return R.style.Theme_00;
            case 1: return R.style.Theme_01;
            case 2: return R.style.Theme_02;
            case 3: return R.style.Theme_03;
            case 4: return R.style.Theme_04;
            case 5: return R.style.Theme_05;
            case 6: return R.style.Theme_06;
            case 7: return R.style.Theme_07;
            case 8: return R.style.Theme_08;
            case 9: return R.style.Theme_09;
            case 10: return R.style.Theme_10;
        }

        if (Build.VERSION.SDK_INT >= 21) {
            switch (themeIdx) {
                case 11: return R.style.Theme_11;
                case 12: return R.style.Theme_12;
                case 13: return R.style.Theme_13;

                case 14: return R.style.Theme_14;
                case 15: return R.style.Theme_15;
                case 16: return R.style.Theme_16;
                case 17: return R.style.Theme_17;
            }
        }

        return R.style.Theme_06;    // Default - see style.xml
    }
}
