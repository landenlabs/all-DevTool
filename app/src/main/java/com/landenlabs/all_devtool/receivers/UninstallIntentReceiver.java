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

package com.landenlabs.all_devtool.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.landenlabs.all_devtool.shortcuts.util.LLog;

/**
 * Created by Dennis Lang on 2/21/2015.
 */

public class UninstallIntentReceiver extends BroadcastReceiver {

    private final LLog mLog = LLog.DBG;

    // Intent.ACTION_UNINSTALL_PACKAGE;  // "android.intent.action.UNINSTALL_PACKAGE"
    // Intent.ACTION_PACKAGE_FULLY_REMOVED;    // "android.intent.action.PACKAGE_FULLY_REMOVED"
    public static final String MSG_PACKAGE_UNINSTALLED = "package-uninstalled";
    public static final String SHARED_PACKAGE_UNINSTALLER = "package-uninstaller";
    public static final String SHARED_PKG_NAME = "Pkg";

    /*
        <action android:name="android.intent.action.PACKAGE_FULLY_REMOVED"  />
        <action android:name="android.intent.action.DELETE" />
        <action android:name="android.intent.action.UNINSTALL_PACKAGE" />
    */
    @Override
    public void onReceive(Context context, Intent intent) {
        mLog.i("onReceive " + intent);
        if (intent.getExtras() != null) {
            mLog.i("onReceive keys=" + TextUtils.join(", ", intent.getExtras().keySet()));
        }
        // String packageName = intent.getData().getSchemeSpecificPart();
        String packageName = intent.getDataString().replace("package:", "");

        // Keys in PACKAGE_REMOVED action:
        // android.intent.extra.REMOVED_FOR_ALL_USERS, android.intent.extra.DATA_REMOVED, android.intent.extra.UID, android.intent.extra.user_handle
        // fetching package names from extras
        // String[] packageNames = intent.getStringArrayExtra("android.intent.extra.PACKAGES");
        
        if (!TextUtils.isEmpty(packageName)) {
            mLog.i("onReceive pkgs=" + packageName);
            SharedPreferences prefs = context.getSharedPreferences(
                    SHARED_PACKAGE_UNINSTALLER,
                    Context.MODE_PRIVATE);
            prefs.edit().putString(SHARED_PKG_NAME, packageName).apply();

        //    Intent newIntent = new Intent(Intent.ACTION_VIEW);
        //    newIntent.setClass(context, UninstallDialog.class);
        //    context.startActivity(newIntent);


            // Toast.makeText(context, "Package uninstalled " + packageNames[0], Toast.LENGTH_LONG).show();
            sendMessage(context, packageName);
        }
    }

    // Send an Intent with an action named "my-event".
    private void sendMessage(Context context, String packageName) {
        Intent intent = new Intent(MSG_PACKAGE_UNINSTALLED);
        intent.putExtra("package", packageName);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}