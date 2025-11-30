/*
 * Copyright (c) 2023 Dennis Lang (LanDen Labs) landenlabs@gmail.com
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

package com.landenlabs.all_devtool.shortcuts.util;

import android.app.Activity;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Send Analytic events
 *
 * Enable / Disable firebasee console analytics -> debugView
 *    adb shell setprop debug.firebase.analytics.app com.landenlabs.all_devtool
 *    adb shell setprop debug.firebase.analytics.app .none
 *
 *
 * https://console.firebase.google.com/project/all-devtool/analytics/
 *
 * 
 * @author Dennis Lang
 */
public class SendAnalytics {
    private static FirebaseAnalytics firebaseAnalytics;


    public static void init(final Activity activity) {
        // Obtain the FirebaseAnalytics instance.
        // Optionally -  enable verbose logging
        //     adb shell setprop log.tag.FA VERBOSE
        //     adb shell setprop log.tag.FA-SVC VERBOSE
        //     adb logcat -v time -s FA FA-SVC
        firebaseAnalytics = FirebaseAnalytics.getInstance(activity);
    }
    public static void event(String category, String action, String label) {
        if (firebaseAnalytics != null) {
            Bundle params = new Bundle();
            // params.putString("category", category);
            params.putString("action", action);
            params.putString("label", label);
            firebaseAnalytics.logEvent(category, params);
        }
    }

}
