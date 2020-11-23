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

package com.landenlabs.all_devtool;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import android.widget.ShareActionProvider;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.os.ConfigurationCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.landenlabs.all_devtool.shortcuts.ShortcutUtil;
import com.landenlabs.all_devtool.util.ALogNotification;
import com.landenlabs.all_devtool.util.AppCrash;
import com.landenlabs.all_devtool.util.GoogleAnalyticsHelper;
import com.landenlabs.all_devtool.util.Ui;
import com.landenlabs.all_devtool.util.UncaughtExceptionHandler;
import com.landenlabs.all_devtool.util.Utils;

import net.danlew.android.joda.JodaTimeAndroid;

import java.util.Locale;


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
 * @see <a href="http://LanDenLabs.com/android"> Author site </a>
 */
public class DevToolActivity extends FragmentActivity {

    protected String m_startFrag;


    @SuppressWarnings({"FieldCanBeLocal"})
    private UncaughtExceptionHandler m_uncaughtExceptionHandler;
    @SuppressWarnings({"FieldCanBeLocal"})
    private FirebaseAnalytics mFirebaseAnalytics;

    @SuppressLint("DefaultLocale")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // m_uncaughtExceptionHandler = new UncaughtExceptionHandler(getApplicationContext());
        boolean DEBUG = (getApplicationInfo().flags & 2) != 0;
        AppCrash.initalize(getApplication(), DEBUG);

        GlobalInfo.s_globalInfo.mainFragActivity = this;
        try {
            GlobalInfo.s_globalInfo.isDebug =  (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
            GlobalInfo.s_globalInfo.pkgName = getPackageName();
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            GlobalInfo.s_globalInfo.version = pInfo.versionName;
            // GlobalInfo.s_globalInfo.appName = pInfo.applicationInfo.name;
        } catch (Exception ex) {
            GlobalInfo.s_globalInfo.version = "1.3";
        }

        /*
        // See build.gradle to add
        // debugCompile "com.squareup.leakcanary:leakcanary-android:${leakCanaryVersion}"
        if (GlobalInfo.s_globalInfo.isDebug) {
            LeakCanary.install(this.getApplication());
        }
        */

        // See ClockFragment
        JodaTimeAndroid.init(this); // Load TimeZone database.

        GoogleAnalyticsHelper.init(this);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Utils.onActivityCreateSetTheme(this);

        setContentView(R.layout.main);
        setTitle(String.format("%s v%s API=%d", GlobalInfo.s_globalInfo.appName, GlobalInfo.s_globalInfo.version,  Build.VERSION.SDK_INT));
        // setTitle(GlobalInfo.s_globalInfo.appName + " v" + BuildConfig.VERSION_NAME + " API" + Build.VERSION.SDK_INT +  (BuildConfig.DEBUG ? " Debug" : ""));

        // Initialization
        ViewPager viewPager = Ui.viewById(this, R.id.pager);
        GlobalInfo.s_globalInfo.tabAdapter =
                new TabPagerAdapter(getSupportFragmentManager(), viewPager, getActionBar());

        GlobalInfo.grabThemeSetings(this);

        GoogleAnalyticsHelper.event(this, this.getLocalClassName(), "create", "");

        Intent intent = this.getIntent();
        if (intent != null) {
            String startupFrag = intent.getStringExtra(GlobalInfo.STARTUP_FRAG);
            if (!TextUtils.isEmpty(startupFrag)) {
                m_startFrag = startupFrag;
            }
        }

        if (!TextUtils.isEmpty(m_startFrag)) {
            viewPager.setCurrentItem(GlobalInfo.s_globalInfo.tabAdapter.findFragPos(m_startFrag, 0));
        }

        // In debug build - enable full StrictMode
        if (DEBUG) {
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

        MenuItem m_shareMenuItem = menu.findItem(R.id.menu_share);
        GlobalInfo.s_globalInfo.shareActionProvider = (ShareActionProvider) m_shareMenuItem.getActionProvider();

        menu.findItem( R.id.menu_lock_orientation).setChecked(GlobalInfo.s_globalInfo.isLockedOrientation);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_about:
                showAbout();
                return true;
            case R.id.menu_web:
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://LanDenLabs.com"));
                startActivity(intent);
                return true;
            case R.id.menu_share:
                GlobalInfo.s_globalInfo.tabAdapter.sharePage();
                //  invalidateOptionsMenu();
                return true;
            case R.id.menu_shortcuts_on:
                ShortcutUtil.makeShortcuts();
                return true;
            case R.id.menu_shortcuts_off:
                ShortcutUtil.removeShortcuts();
                return true;

            case R.id.menu_lock_orientation:
                item.setChecked(!item.isChecked());
                GlobalInfo.s_globalInfo.isLockedOrientation = item.isChecked();
                GlobalInfo.s_globalInfo.lockedOrientation = getResources().getConfiguration().orientation;
                return true;
        }

        return super.onOptionsItemSelected(item);
        // return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (GlobalInfo.s_globalInfo.isLockedOrientation) {
            setRequestedOrientation(GlobalInfo.s_globalInfo.lockedOrientation);
        }

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "Landscape", Toast.LENGTH_SHORT).show();
            // GlobalInfo.s_globalInfo.tabAdapter.m_actionBar.hide();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(this, "Portrait", Toast.LENGTH_SHORT).show();
            // GlobalInfo.s_globalInfo.tabAdapter.m_actionBar.show();
        }

        Locale.setDefault(ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0));
    }

    @Override
    public void onStart() {
        super.onStart();
        GlobalInfo.s_globalInfo.mainFragActivity = this;
        m_uncaughtExceptionHandler = new UncaughtExceptionHandler(this);
        Locale.setDefault(ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0));
        ALogNotification.init(this);
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
        // wv.loadUrl("file:///android_asset/about.html");
        int resId = getResources().getIdentifier("compileSdkVersion", "string", getPackageName());
        String compileSdk = (resId > 0) ? getResources().getString(resId) : "";
        resId = getResources().getIdentifier("buildToolsVersion", "string", getPackageName());
        String buildToolsVersion = (resId > 0) ? getResources().getString(resId) : "";

        String htmlStr = String.format(Utils.LoadData(this, "about.html"),
                getPackageInfo().versionName, "", compileSdk, buildToolsVersion);
        Ui.showWebMessage(this, Ui.HTML_CENTER_BOX, htmlStr);
        GoogleAnalyticsHelper.event(this, "", "dialog", "about");
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
