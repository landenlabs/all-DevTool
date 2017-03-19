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


import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.WindowManager;

import com.landenlabs.all_devtool.util.GoogleAnalyticsHelper;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for all DevTool fragments.
 *
 * @author Dennis Lang
 */
public abstract class DevFragment extends Fragment {

    static Map<String, WeakReference<DevFragment>> s_devFragmentCache = new HashMap<String, WeakReference<DevFragment>>();

    /**
     * @return name of fragment.
     */
    public abstract String getName();

    /**
     * @return Bitmaps of full content
     */
    public abstract List<Bitmap> getBitmaps(int maxHeight);

    /**
     * Called when fragment selected (visible)
     */
    public void onSelected() {
        GoogleAnalyticsHelper.event(this.getActivity(), "activity", "selected", getName());
        GlobalInfo.s_globalInfo.mainFragActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        GlobalInfo.s_globalInfo.mainFragActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

// ============================================================================================
    // Fragment methods

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (GlobalInfo.s_globalInfo.isLockedOrientation) {
            getActivity().setRequestedOrientation(GlobalInfo.s_globalInfo.lockedOrientation);
        }

        cacheFragment();

        // this.setRetainInstance(true);
        if (GlobalInfo.s_globalInfo.haveActionBarOverlay) {
            // ViewPager.LayoutParams viewParams = (ViewPager.LayoutParams)view.getLayoutParams();
            view.setPadding(0, GlobalInfo.s_globalInfo.actionBarHeight * 2, 0, 0);
        }
    }

    // Coming into foreground - update analytics
    @Override
    public void onResume() {
        super.onResume();
        if (this.isVisible())
            GoogleAnalyticsHelper.event(getActivity(), getClass().getName(), "onResume", "");
    }

    @Override
    public void onStop() {
        GoogleAnalyticsHelper.event(this.getActivity(), "", "stop", this.getClass().getName());
        super.onStop();
    }

    // ============================================================================================
    // DevFragment methods

    // Deprecated - nolonger used
    protected void cacheFragment() {
        // m_log.i(String.format("set %s %08x", getName(), System.identityHashCode(this)));
        if (this.getActivity() != null)
            s_devFragmentCache.put(getName(), new WeakReference<DevFragment>(this));
    }

    public static DevFragment getFragmentByName(String fragName) {
        WeakReference<DevFragment> devFragWeakRef = s_devFragmentCache.get(fragName);
        return devFragWeakRef != null ? devFragWeakRef.get() : null;
    }
}
