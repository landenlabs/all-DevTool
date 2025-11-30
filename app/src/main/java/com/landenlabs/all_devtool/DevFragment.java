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

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;

import com.landenlabs.all_devtool.shortcuts.util.SendAnalytics;

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
    protected SubMenu subMenu;

    protected static final Map<String, WeakReference<DevFragment>> s_devFragmentCache = new HashMap<>();

    public abstract String getName();

    // Export methods
    public abstract List<Bitmap> getBitmaps(int maxHeight);
    public abstract  List<String> getListAsCsv();

    // Menu methods
    protected MenuProvider menuProvider = new MenuProvider() {
        @Override
        public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
            MenuCompat.setGroupDividerEnabled(menu, true);
            // menuInflater.inflate(menuRes, menu);    // R.menu.menus
            onMenuCreate(menu, menuInflater);
        }

        @Override
        public void onPrepareMenu(@NonNull Menu menu) {
            // This is called right before the menu is shown.
            // You can access and modify items from the activity's menu.
        }

        @Override
        public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
            // Return true if handled, false to allow the activity to handle it.
            return onMenuSelected(menuItem);
        }
    };
    protected void onMenuCreate(@NonNull Menu menu, @NonNull MenuInflater menuInflater) { }
    protected boolean onMenuSelected(@NonNull MenuItem menuItem) { return false; }


    // ============================================================================================
    // Fragment methods

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cacheFragment();

        requireActivity().addMenuProvider(menuProvider, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        // Apply padding to the top of your view to avoid the Status Bar
        // Apply padding to view pager bottom to avoid navigation buttons
        boolean fullScreen = "Screen".equals(getName());
        int topDecor = fullScreen ? 0 : GlobalInfo.s_insets.top;
        int botDecor = fullScreen ? 0 : GlobalInfo.s_insets.bottom;
        int tabHeightPx = 40;   // fallback guess.

        View tabBar = GlobalInfo.s_globalInfo.mainFragActivity.findViewById(R.id.tabs);
        if (tabBar != null) {
            tabBar.setY(topDecor);
            // tabBar.setPadding(tabBar.getPaddingLeft(), topDecor , tabBar.getPaddingRight(),  tabBar.getPaddingBottom());
            tabHeightPx = Math.max(tabHeightPx,  tabBar.getHeight());
            if (!fullScreen)
                topDecor += tabHeightPx;
        }

        view.setPadding(view.getPaddingLeft(), topDecor , view.getPaddingRight(),  botDecor);

    }

    // Coming into foreground - update analytics
    @Override
    public void onResume() {
        super.onResume();
        if (this.isVisible())
            SendAnalytics.event(getClass().getSimpleName(), "onResume", "");
    }

    @Override
    public void onStop() {
        SendAnalytics.event(getClass().getSimpleName(), "onStop","");
        super.onStop();
    }

    /**
     * Called when fragment selected (visible)
     */
    public void onSelected() {
        SendAnalytics.event( getClass().getSimpleName(), "selected", getName());
        // GlobalInfo.s_globalInfo.mainFragActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        // GlobalInfo.s_globalInfo.mainFragActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // ============================================================================================
    // DevFragment methods

    protected void cacheFragment() {
        // LLOG.i(String.format("set %s %08x", getName(), System.identityHashCode(this)));
        if (this.getActivity() != null)
            s_devFragmentCache.put(getName(), new WeakReference<>(this));
    }

    public static DevFragment getFragmentByName(String fragName) {
        WeakReference<DevFragment> devFragWeakRef = s_devFragmentCache.get(fragName);
        return devFragWeakRef != null ? devFragWeakRef.get() : null;
    }

    @NonNull
    public <T> T getServiceSafe(String service) {
        //noinspection unchecked
        return (T)Objects.requireNonNull(requireActivity().getSystemService(service));
    }

    @NonNull
    Window getWindow() {
        return Objects.requireNonNull(requireActivity().getWindow());
    }

    // ============================================================================================
    // Permissions
    protected static final int MY_PERMISSIONS_REQUEST = 27;
    protected boolean checkPermissions(String... needPermissions) {
        boolean okay = true;
        List<String> requestPermissions = new ArrayList<>();
        for (String needPermission : needPermissions) {
            if (requireContext()
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
