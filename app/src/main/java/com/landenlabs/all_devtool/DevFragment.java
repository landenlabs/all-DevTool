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
 * @see https://LanDenLabs.com/
 */

package com.landenlabs.all_devtool;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.landenlabs.all_devtool.shortcuts.util.GoogleAnalyticsHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Base class for all DevTool fragments.
 *
 * @author Dennis Lang
 */
public abstract class DevFragment extends Fragment {

    static final Map<String, WeakReference<DevFragment>> s_devFragmentCache = new HashMap<>();

    /**
     * @return name of fragment.
     */
    public abstract String getName();

    /**
     * @return Bitmaps of full content
     */
    public abstract List<Bitmap> getBitmaps(int maxHeight);

    public abstract  List<String> getListAsCsv();

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (GlobalInfo.s_globalInfo.isLockedOrientation) {
            getActivitySafe().setRequestedOrientation(GlobalInfo.s_globalInfo.lockedOrientation);
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
            GoogleAnalyticsHelper.event(getActivitySafe(), getClass().getName(), "onResume", "");
    }

    @Override
    public void onStop() {
        GoogleAnalyticsHelper.event(this.getActivitySafe(), "", "stop", this.getClass().getName());
        super.onStop();
    }

    // ============================================================================================
    // DevFragment methods

    // Deprecated - nolonger used
    protected void cacheFragment() {
        // m_log.i(String.format("set %s %08x", getName(), System.identityHashCode(this)));
        if (this.getActivity() != null)
            s_devFragmentCache.put(getName(), new WeakReference<>(this));
    }

    public static DevFragment getFragmentByName(String fragName) {
        WeakReference<DevFragment> devFragWeakRef = s_devFragmentCache.get(fragName);
        return devFragWeakRef != null ? devFragWeakRef.get() : null;
    }

    @NonNull
    protected Context getContextSafe() {
        return requireContext();
    }
    @NonNull
    public Activity getActivitySafe() {
        return requireActivity();
    }
    @NonNull
    public <T> T getServiceSafe(String service) {
        //noinspection unchecked
        return (T)Objects.requireNonNull(getActivitySafe().getSystemService(service));
    }

    @NonNull
    Window getWindow() {
        return Objects.requireNonNull(getActivitySafe().getWindow());
    }

    // ============================================================================================
    // Permissions
    protected static final int MY_PERMISSIONS_REQUEST = 27;
    protected boolean checkPermissions(String... needPermissions) {
        boolean okay = true;
        List<String> requestPermissions = new ArrayList<>();
        for (String needPermission : needPermissions) {
            if (getContextSafe()
                    .checkSelfPermission(needPermission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions.add(needPermission);
            }
        }
        if (! requestPermissions.isEmpty()) {
            okay = false;
            requestPermissions(requestPermissions.toArray(new String[0]), MY_PERMISSIONS_REQUEST);
        }

        return okay;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("DevFragment", " requestPermissionResult for " + requestCode);
    }
}
