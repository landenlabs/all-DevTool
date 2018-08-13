package com.landenlabs.all_devtool.shortcuts;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;

import com.landenlabs.all_devtool.DevToolActivity;
import com.landenlabs.all_devtool.FileBrowserFragment;
import com.landenlabs.all_devtool.GlobalInfo;
import com.landenlabs.all_devtool.GpsFragment;
import com.landenlabs.all_devtool.PackageFragment;
import com.landenlabs.all_devtool.R;
import com.landenlabs.all_devtool.ScreenFragment;

/**
 * Created by Dennis Lang on 6/13/2015.
 */
public class ShortcutUtil {

    public static void updateShortcutFileBrowser(boolean makeIt) {
        updateShortcut(makeIt, FileBrowserFragment.s_name, "DS FileBrowser", R.drawable.shortcut_fb);
    }

    public static void updateShortcutGPS(boolean makeIt) {
        updateShortcut(makeIt, GpsFragment.s_name, "DS GPS", R.drawable.shortcut_gps);
    }

    public static void updateShortcutPackage(boolean makeIt) {
        updateShortcut(makeIt, PackageFragment.s_name, "DS Package", R.drawable.shortcut_pkg);
    }

    public static void updateShortcutScreen(boolean makeIt) {
        updateShortcut(makeIt, ScreenFragment.s_name, "DS Screen", R.drawable.shortcut_scn);
    }

    @SuppressWarnings("ConstantConditions")
    public static void makeShortcuts() {
        boolean makeIt = true;
        ShortcutUtil.updateShortcutFileBrowser(makeIt);
        ShortcutUtil.updateShortcutGPS(makeIt);
        ShortcutUtil.updateShortcutPackage(makeIt);
        ShortcutUtil.updateShortcutScreen(makeIt);
    }

    @SuppressWarnings("ConstantConditions")
    public static void removeShortcuts() {
        boolean makeIt = false;
        ShortcutUtil.updateShortcutFileBrowser(makeIt);
        ShortcutUtil.updateShortcutGPS(makeIt);
        ShortcutUtil.updateShortcutPackage(makeIt);
        ShortcutUtil.updateShortcutScreen(makeIt);
    }

    // http://stackoverflow.com/questions/6424246/creating-shortcuts-in-android-via-intent
    public static void updateShortcut(boolean makeIt, String fragName, String shortcutName, int iconResID) {

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
        // shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

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
            putShortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(fragActivity, iconResID));
            putShortCutIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            fragActivity.sendBroadcast(putShortCutIntent);
        }

    //    sharedPreferences.edit().putBoolean(shortcutName, true);
    //    sharedPreferences.edit().commit();
    }

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
}
