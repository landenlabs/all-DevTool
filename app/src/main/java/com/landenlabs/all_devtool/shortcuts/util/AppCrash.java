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

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.landenlabs.all_devtool.GlobalInfo;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;
import com.microsoft.appcenter.crashes.CrashesListener;
import com.microsoft.appcenter.crashes.ingestion.models.ErrorAttachmentLog;
import com.microsoft.appcenter.crashes.model.ErrorReport;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

/**
 * Utility class to optionally initialize HockeyApp crash reporting
 * <br>
 * Populate description with additional information.
 *
 * @author Dennis Lang on 11/15/16.
 */

public class AppCrash implements CrashesListener {

    private static final String TAG = "AppCrash";
    private final WeakReference<Context> mRefContext;

    private AppCrash(Context context) {
        mRefContext = new WeakReference<>(context);
    }

    public static String getVersion() {
        return AppCenter.getSdkVersion();
    }

    private Context getContext() {
        return mRefContext.get();
    }

    public static void initalize(Application application, boolean isDebug) {
        AppCrash crashListener = new AppCrash(application);
        Crashes.setListener(crashListener);
        Crashes.setEnabled(true);

        AppCenter.setLogLevel(GlobalInfo.s_globalInfo.isDebug ? Log.VERBOSE : Log.ERROR);
        String secret = "67592601-9f95-4956-8d7f-c2c02e96de1f";
        AppCenter.start(application, secret, Analytics.class, Crashes.class);
        AppCenter.setUserId(application.getPackageName());

        //  Use some App Center getters.
        AppCenter.getInstallId().thenAccept(uuid -> Log.d(TAG, "InstallId=" + uuid));

        Crashes.hasCrashedInLastSession().thenAccept(crashed -> Log.d(TAG, "Crashes.hasCrashedInLastSession=" + crashed));

        Crashes.getLastSessionCrashReport().thenAccept(data -> {
            if (data != null) {
                Log.d(TAG, "Crashes.getLastSessionCrashReport().getThrowable()="+ data.getStackTrace());
            }
        });
    }

    /**
     * Please all Log 'errors' in description.
     * @return Crash description
     */
    public String getDescription() {
        String description = "";
        // String eol = System.getProperty("line.separator");

        description = addRes(description, "TargetSDK=", "targetSdkVersion");
        description = addRes(description, "CompilerSDK=", "compileSdkVersion");
        description = addRes(description, "BuildTools=", "buildToolsVersion");

        return description;
    }

    private String addRes(String inStr, String title, String resName) {
        int resId = getContext().getResources().getIdentifier(
                resName, "string", getContext().getPackageName());
        if (resId > 0) {
            inStr += title + getContext().getResources().getString(resId);
        }

        return inStr;
    }

    @Override
    public boolean shouldProcess(ErrorReport report) {
        String str = report.getStackTrace();
        if (str == null)
            return true;

        // Exclude these crash report exceptions
        // android.app.RemoteServiceException: Bad notification posted from package com.wood.android.weather: Couldn't expand RemoteViews for: ...
        boolean hasRemoteViews = str.contains("RemoteViews");
        boolean hasRemoteServiceEx = str.contains("RemoteServiceException");
        return !(hasRemoteViews || hasRemoteServiceEx);
    }

    @Override
    public boolean shouldAwaitUserConfirmation() {
        return false;
    }

    /**
     * You can add one binary and one text attachment to a crash report.
     *
     * The SDK will send it along with the crash so that you can see it in App Center portal.
     * The following callback will be invoked right before sending the stored crash
     * from previous application launches. It will not be invoked when the crash happens.
     * Here is an example of how to attach text and an image to a crash:
     */
    @Override
    public Iterable<ErrorAttachmentLog> getErrorAttachments(ErrorReport report) {
        List<ErrorAttachmentLog> attachments = new LinkedList<>();
        ErrorAttachmentLog textLog = ErrorAttachmentLog.attachmentWithText(getDescription(), "text.txt");
        attachments.add(textLog);
        return attachments;
    }

    @Override
    public void onBeforeSending(ErrorReport report) {
        // Nothing to do
    }

    @Override
    public void onSendingFailed(ErrorReport report, Exception e) {
        // Nothing to do
    }

    @Override
    public void onSendingSucceeded(ErrorReport report) {
        // Nothing to do
    }
}
