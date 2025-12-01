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

import static com.landenlabs.all_devtool.shortcuts.util.LLog.LLOG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.landenlabs.all_devtool.shortcuts.ShortcutUtil;
import com.landenlabs.all_devtool.shortcuts.util.ALogNotification;
import com.landenlabs.all_devtool.shortcuts.util.SendAnalytics;
import com.landenlabs.all_devtool.shortcuts.util.Ui;
import com.landenlabs.all_devtool.shortcuts.util.UncaughtExceptionHandler;
import com.landenlabs.all_devtool.shortcuts.util.Utils;

/**
 * Main activity for Dev Tool
 * <p/>
 * Dev Tool - Display useful developer information such as:
 * <ul>
 * <li> Build system information
 * <li> Text / Font examples with vertical space usage.
 * <li> Theme changer with UI samples.
 * <li> Screen space (pixels and DP's)
 * <li> System attributes and icons
 * </ul>
 * Reference links:
 * <ul>
 * <li> <a href="http://joerg-richter.fuyosoft.com/?p=181"> Alert dialog theme </a>
 * <li> <a href="http://stackoverflow.com/questions/2422562/how-to-change-theme-for-alertdialog"> Alert dialog theme  </a>
 * <li> <a href="https://sites.google.com/site/androidhowto/how-to-1/customize-alertdialog-theme"> Alert dialog theme </a>
 * <li> <a href="http://joshclemm.com/blog/?p=136"> Tab  </a>
 * <li> <a href="http://www.androidhive.info/2011/08/android-tab-layout-tutorial"> Tab </a>
 * <li> <A href="http://developer.android.com/training/implementing-navigation/lateral.html"> Swipe  </a>
 * <li> <a href="http://stackoverflow.com/questions/8191529/get-theme-attributes-programmatically"> Get attributes </a>
 * </ul>
 *
 * @author Dennis Lang
 * @version v1.1  Nov-2014 Released
 * @see <a href="https://LanDenLabs.com/android"> Author site </a>
 */
public class DevToolActivity extends AppCompatActivity {

    protected String startFrag;

    // Prepare  permission request launcher
    private final ActivityResultLauncher<String> requestPerm =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            LLOG.d("Permission granted");
                        } else {
                            LLOG.w( "Permission denied.");
                        }
                    });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This tells the app to draw behind the system bars (draw edge-to-edge)
        // See DevFragment - where padding is restored.
        // See setOnApplyWindowInsetsListener
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        boolean DEBUG = (getApplicationInfo().flags & 2) != 0;

        GlobalInfo.s_globalInfo.requestPerm = requestPerm;
        GlobalInfo.s_globalInfo.mainFragActivity = this;
        try {
            GlobalInfo.s_globalInfo.isDebug =  (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
            GlobalInfo.s_globalInfo.pkgName = getPackageName();
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            GlobalInfo.s_globalInfo.version = pInfo.versionName;
            // GlobalInfo.s_globalInfo.appName = pInfo.applicationInfo.name;
        } catch (Exception ex) {
            GlobalInfo.s_globalInfo.version = "7.11.29";
        }

        LLOG.d("startup");  // call after global info setup completed.

        /*
        // See build.gradle to add
        // debugCompile "com.squareup.leakcanary:leakcanary-android:${leakCanaryVersion}"
        if (GlobalInfo.s_globalInfo.isDebug) {
            LeakCanary.install(this.getApplication());
        }
        */

        Utils.onActivityCreateSetTheme(this);

        setContentView(R.layout.main);

        // Apply a listener to the root view of the fragment's layout
        View root = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            // Get the insets for the system bars (Status Bar + Navigation Bar)
            GlobalInfo.s_insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            return windowInsets;
        });

        setTitle(String.format("%s v%s API=%d", GlobalInfo.s_globalInfo.appName, GlobalInfo.s_globalInfo.version,  Build.VERSION.SDK_INT));
        // setTitle(GlobalInfo.s_globalInfo.appName + " v" + BuildConfig.VERSION_NAME + " API" + Build.VERSION.SDK_INT +  (BuildConfig.DEBUG ? " Debug" : ""));

        // Initialize tab pager
        ViewPager2 viewPager = Ui.viewById(this, R.id.pager);
        // viewPager.setUserInputEnabled(false);       // Disable swipe

        TabLayout tabLayout = Ui.viewById(this, R.id.tabs);
        GlobalInfo.s_globalInfo.tabAdapter = new TabPagerAdapter(this, viewPager, tabLayout);

        GlobalInfo.grabThemeSettings(this);


        SendAnalytics.init(this);
        SendAnalytics.event(  this.getLocalClassName(), "create", "");

        Intent intent = this.getIntent();
        if (intent != null) {
            String startupFrag = intent.getStringExtra(GlobalInfo.STARTUP_FRAG);
            if (!TextUtils.isEmpty(startupFrag)) {
                startFrag = startupFrag;
            }
        }

        if (!TextUtils.isEmpty(startFrag)) {
            viewPager.setCurrentItem(GlobalInfo.s_globalInfo.tabAdapter.findFragPos(startFrag, 0));
        }

        // In debug build - enable full StrictMode
        if (false && DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    // .detectDiskReads()
                    // .detectDiskWrites()
                    // .detectNetwork()
                    .detectAll()
                    .penaltyLog()
                    // .penaltyDeath()
                    .build());
        }
    }

    /**
     * Create option menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menus, menu);
        menu.findItem( R.id.menu_lock_orientation).setChecked(GlobalInfo.s_globalInfo.isLockedOrientation);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_about) {
            showAbout();
            return true;
        } else if (itemId == R.id.menu_web) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://LanDenLabs.com"));
            startActivity(intent);
            return true;
        } else if (itemId == R.id.menu_share) {
            GlobalInfo.s_globalInfo.tabAdapter.sharePage();
            //  invalidateOptionsMenu();
            return true;
        } else if (itemId == R.id.menu_shortcuts_on) {
            ShortcutUtil.makeShortcuts();
            return true;
        } else if (itemId == R.id.menu_shortcuts_off) {
            ShortcutUtil.removeShortcuts();
            return true;
        } else if (itemId == R.id.menu_lock_orientation) {
            item.setChecked(!item.isChecked());
            GlobalInfo.s_globalInfo.isLockedOrientation = item.isChecked();
            if (GlobalInfo.s_globalInfo.isLockedOrientation) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
        // return false;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Because configChanges includes "orientation", this method is called *after* an orientation
        // change has already occurred. Trying to set the orientation here is too late and
        // can cause flickering or be ignored. The lock must be established beforehand in onOptionsItemSelected.
    }

    @Override
    public void onStart() {
        super.onStart();
        GlobalInfo.s_globalInfo.mainFragActivity = this;
        new UncaughtExceptionHandler(this);
        ALogNotification.init(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ALogNotification.updateNotification(this, "AllDevTool");
    }

    @Override
    public void onStop() {
        super.onStop();
        GlobalInfo.s_globalInfo.mainFragActivity = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GlobalInfo.s_globalInfo.mainFragActivity = null;
    }

    // ============================================================================================
    // Local methods

    /**
     * Show about information in dialog box.
     * Use html web viewer in AlertDialog.
     */
    private void showAbout() {
        @SuppressLint("DiscouragedApi")
        int resId = getResources().getIdentifier("compileSdkVersion", "string", getPackageName());
        String compileSdk = (resId > 0) ? getResources().getString(resId) : "";
        resId = getResources().getIdentifier("buildToolsVersion", "string", getPackageName());
        String buildToolsVersion = (resId > 0) ? getResources().getString(resId) : "";
        String htmlStr = String.format(Utils.LoadData(this, "about.html"),
                getPackageInfo().versionName, "", compileSdk, buildToolsVersion);
        Ui.showWebMessage(this, Ui.HTML_CENTER_BOX, htmlStr);
        SendAnalytics.event(getLocalClassName(),  "dialog", "about");
    }

    /**
     * @return PackageInfo
     */
    @NonNull
    private PackageInfo getPackageInfo() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (NameNotFoundException e) {
            return new PackageInfo();
        }
    }
}
