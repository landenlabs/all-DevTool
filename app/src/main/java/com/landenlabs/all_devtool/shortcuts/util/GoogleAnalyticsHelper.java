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
 * @see http://LanDenLabs.com/
 */

package com.landenlabs.all_devtool.shortcuts.util;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.landenlabs.all_devtool.GlobalInfo;

/**
 * Google Analytics helper class.
 *
 * @author Dennis Lang
 */
@SuppressWarnings({"Convert2Lambda", "UnnecessaryLocalVariable"})
public class GoogleAnalyticsHelper {

    private static String GOOGLE_ANALYTICS_KEY = "none";
    private static String sScreen = "";
    private static boolean sGAC_enabled = false;
    private static boolean sGA_tracking = false;

    private static GoogleAnalytics getAnalytics(Activity activity) {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(activity);
        // analytics.enableAutoActivityReports(activity.getApplication());
        // analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
        return analytics;
    }

    public static void init(final Activity activity) {

        int resId = activity.getResources().getIdentifier("google_analytic_key", "string",
                activity.getPackageName());
        GOOGLE_ANALYTICS_KEY = activity.getResources().getString(resId);
        // R.string.google_analytic_key);

        // Create a GoogleApiClient instance
        GoogleApiClient  googleApiClient = new GoogleApiClient.Builder(activity)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    public void onConnected(Bundle connectionHint) {
                        sGAC_enabled = true;
                    }
                    public void onConnectionSuspended(int cause) {
                        sGAC_enabled = false;
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    public void onConnectionFailed(@NonNull ConnectionResult result) {
                        // This callback is important for handling errors that
                        // may occur while attempting to connect with Google.
                        //
                        // More about this in the next section.

                        sGAC_enabled = false;
                        // Toast.makeText(activity, "Need Google Play Services, error " + result.getErrorCode(), Toast.LENGTH_LONG).show();
                    }

                })
                .build();
        googleApiClient.connect();

        if (getTracker(activity) != null)
            sGA_tracking = true;
    }

    // ---------------------------------------------------------------------------------------------

    private static Tracker s_tracker;
    private static Tracker getTracker(Activity activity) {
        if (s_tracker == null) {
            s_tracker = getAnalytics(activity).newTracker(GOOGLE_ANALYTICS_KEY);
            s_tracker.enableAutoActivityTracking(true);
            s_tracker.enableExceptionReporting(true);
        }
        return s_tracker;
    }

    public static void start(Activity activity) {
        // GoogleAnalytics.getInstance(activity).reportActivityStart(activity);
    }

    public static void stop(Activity activity) {
        // GoogleAnalytics.getInstance(activity).reportActivityStop(activity);
    }

    public static void event(Activity activity, String cat, String act, String lbl) {
        if (sGA_tracking) {
            if (TextUtils.isEmpty(sScreen)) {
                sScreen = GlobalInfo.s_globalInfo.pkgName;
                getTracker(activity).setScreenName(sScreen);
            }

            if (TextUtils.isEmpty(cat)) {
                cat = GlobalInfo.s_globalInfo.version;
            }

            // Set screen name.
            // Where path is a String representing the screen name.
            // m_tracker.setScreenName("Dev);

            // Send a screen view.
            // m_tracker.send(new HitBuilders.ScreenViewBuilder().build());

            // This event will also be sent with &cd=Home%20Screen.
            // Build and send an Event.
            getTracker(activity).send(
                    new HitBuilders.EventBuilder().setCategory(cat).setAction(act).setLabel(lbl)
                            .build());

            // Clear the screen name field when we're done.
            // m_tracker.setScreenName(null);
        }
    }
}
