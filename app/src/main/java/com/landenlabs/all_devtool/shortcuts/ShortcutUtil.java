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

package com.landenlabs.all_devtool.shortcuts;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.landenlabs.all_devtool.DevToolActivity;
import com.landenlabs.all_devtool.FileBrowserFragment;
import com.landenlabs.all_devtool.GlobalInfo;
import com.landenlabs.all_devtool.GpsFragment;
import com.landenlabs.all_devtool.NetFragment;
import com.landenlabs.all_devtool.NetstatFragment;
import com.landenlabs.all_devtool.PackageFragment;
import com.landenlabs.all_devtool.PropFragment;
import com.landenlabs.all_devtool.R;
import com.landenlabs.all_devtool.ScreenFragment;
import com.landenlabs.all_devtool.SystemFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Dennis Lang on 6/13/2015.
 */
public class ShortcutUtil {

    private static void updateShortcutFileBrowser(boolean makeIt) {
        updateShortcut(makeIt, false, FileBrowserFragment.s_name, "FileBrowser", R.drawable.shortcut_fb);
    }

    private static void updateShortcutGPS(boolean makeIt) {
        updateShortcut(makeIt, false, GpsFragment.s_name, "GPS", R.drawable.shortcut_gps);
    }

    private static void updateShortcutPackage(boolean makeIt) {
        updateShortcut(makeIt, true, PackageFragment.s_name, "Package", R.drawable.shortcut_pkg);
    }

    private static void updateShortcutScreen(boolean makeIt) {
        updateShortcut(makeIt, false, ScreenFragment.s_name, "Screen", R.drawable.shortcut_scn);
    }


    // TODO - need unique shortcut icons !!
    private static void updateShortcutNetStat(boolean makeIt) {
        updateShortcut(makeIt, false, NetstatFragment.s_name, "NetStat", R.drawable.shortcut_scn);
    }
    private static void updateShortcutNetwork(boolean makeIt) {
        updateShortcut(makeIt, false, NetFragment.s_name, "Network", R.drawable.shortcut_scn);
    }
    private static void updateShortcutSystem(boolean makeIt) {
        updateShortcut(makeIt, false, SystemFragment.s_name, "System", R.drawable.shortcut_scn);
    }
    private static void updateShortcutProperties(boolean makeIt) {
        updateShortcut(makeIt, false, PropFragment.s_name, "Properties", R.drawable.shortcut_scn);
    }

    public static void makeShortcuts() {
        updateShortcuts(true);
    }

    public static void removeShortcuts() {
        updateShortcuts(false);
    }

    private static void updateShortcuts(boolean makeIt) {
        ShortcutUtil.updateShortcutFileBrowser(makeIt);
        ShortcutUtil.updateShortcutGPS(makeIt);
        ShortcutUtil.updateShortcutPackage(makeIt);
        ShortcutUtil.updateShortcutScreen(makeIt);
        ShortcutUtil.updateShortcutNetStat(makeIt);
        ShortcutUtil.updateShortcutNetwork(makeIt);
        ShortcutUtil.updateShortcutSystem(makeIt);
        ShortcutUtil.updateShortcutProperties(makeIt);
    }

    // http://stackoverflow.com/questions/6424246/creating-shortcuts-in-android-via-intent
    private static void updateShortcut(boolean makeIt, boolean pinShortcut, String fragName, String shortcutName, int iconResID) {

        FragmentActivity fragActivity = GlobalInfo.s_globalInfo.mainFragActivity;

        // Checking if ShortCut was already added
        SharedPreferences sharedPreferences = fragActivity.getPreferences(Activity.MODE_PRIVATE);
        boolean shortCutWasAlreadyAdded = sharedPreferences.getBoolean(shortcutName, false);
        if (shortCutWasAlreadyAdded)
            return;

        Intent shortcutIntent;
        shortcutIntent = new Intent(fragActivity, DevToolActivity.class);
        shortcutIntent.putExtra(GlobalInfo.STARTUP_FRAG, fragName);
        shortcutIntent.setAction(Intent.ACTION_MAIN);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            final Intent putShortCutIntent = new Intent();
            putShortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            putShortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutName);

            // Requires permission
            //  <uses-permission android:name= "com.android.launcher.permission.UNINSTALL_SHORTCUT"/>
            putShortCutIntent.setAction("com.android.launcher.action.UNINSTALL_SHORTCUT");
            fragActivity.sendBroadcast(putShortCutIntent);

            if (makeIt) {
                // Requires permission
                //  <uses-permission android:name="com.android.launcher.permission.INSTALL_SHORTCUT" />
                putShortCutIntent.putExtra("duplicate", false);
                putShortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                        Intent.ShortcutIconResource.fromContext(fragActivity, iconResID));
                putShortCutIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
                fragActivity.sendBroadcast(putShortCutIntent);
            }

        } else {
            ShortcutManager shortcutManager = fragActivity.getSystemService(ShortcutManager.class);
            if (makeIt) {
                // https://stackoverflow.com/questions/46796674/create-shortcut-on-home-screen-in-android-o
                ShortcutInfo.Builder mShortcutInfoBuilder = new ShortcutInfo.Builder(fragActivity, fragName);
                mShortcutInfoBuilder.setShortLabel(fragName);
                mShortcutInfoBuilder.setLongLabel(fragName);
                mShortcutInfoBuilder.setIcon(Icon.createWithResource(fragActivity, iconResID));
                mShortcutInfoBuilder.setIntent(shortcutIntent);

                ShortcutInfo mShortcutInfo = mShortcutInfoBuilder.build();
                if (pinShortcut) {
                    shortcutManager.requestPinShortcut(mShortcutInfo, null);
                } else {
                    List<ShortcutInfo> activeShortcutInfos = new ArrayList<>(1);
                    activeShortcutInfos.add(mShortcutInfo);
                    if (!shortcutManager.addDynamicShortcuts(activeShortcutInfos)) {
                        Log.w("Shortcut", "Failed to add shortcut for " + fragName);
                    }
                }
            } else {
                List<ShortcutInfo> activeShortcutInfos = shortcutManager.getDynamicShortcuts();
                List<String> removeIds = new ArrayList<>();
                for (ShortcutInfo shortcutInfo : activeShortcutInfos) {
                    if (Objects.equals(shortcutInfo.getShortLabel(), fragName)) {
                        removeIds.add(shortcutInfo.getId());
                        break;
                    }
                }
                shortcutManager.removeDynamicShortcuts(removeIds);
            }
        }

    //    sharedPreferences.edit().putBoolean(shortcutName, true);
    //    sharedPreferences.edit().commit();
    }

    /*
    // http://stackoverflow.com/questions/6424246/creating-shortcuts-in-android-via-intent
    public static void installShortcut(Class<?> cls, String shortcutName, int iconResID) {

        FragmentActivity fragActivity = GlobalInfo.s_globalInfo.mainFragActivity;

        // Checking if ShortCut was already added
        SharedPreferences sharedPreferences = fragActivity.getPreferences(Activity.MODE_PRIVATE);
        boolean shortCutWasAlreadyAdded = sharedPreferences.getBoolean(shortcutName, false);
        if (shortCutWasAlreadyAdded)
            return;
        sharedPreferences.edit().putBoolean(shortcutName, true).apply();

        Intent shortcutIntent;
        shortcutIntent = new Intent(fragActivity, cls);
        // shortcutIntent.setComponent(new ComponentName(fragActivity.getPackageName(), className));

        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        final Intent putShortCutIntent = new Intent();
        putShortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);

        // Sets the custom shortcut's title

        putShortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutName);
        putShortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(fragActivity, iconResID));

        // putShortCutIntent.putExtra("duplicate", false);
        putShortCutIntent.setAction("com.android.launcher.action.UNINSTALL_SHORTCUT");
        fragActivity.sendBroadcast(putShortCutIntent);

        putShortCutIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        fragActivity.sendBroadcast(putShortCutIntent);
    }
    */
}
