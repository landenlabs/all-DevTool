package com.landenlabs.all_devtool.util;

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
import com.microsoft.appcenter.utils.async.AppCenterConsumer;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Utility class to optionally initialize HockeyApp crash reporting
 * <br>
 * Populate description with additional information.
 *
 *
 *
 * @author Dennis Lang on 11/15/16.
 */

@SuppressWarnings("UnnecessaryLocalVariable")
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

    @SuppressWarnings("unused")
    public static void initalize(Application application, boolean isDebug) {
        Context context = application;


        int keyResId = context.getResources().getIdentifier("hockeyapp_key", "string", context.getPackageName());
        int pkgResId = context.getResources().getIdentifier("hockeyapp_pkg", "string", context.getPackageName());
        if (keyResId > 0 && pkgResId > 0) {
            final String HOCKEY_APP_ID = context.getResources().getString(keyResId);
            final String HOCKEY_APP_PKG = context.getResources().getString(pkgResId);

            AppCrash crashListener = new AppCrash(application);
            Crashes.setListener(crashListener);
            Crashes.setEnabled(true);

            AppCenter.setLogLevel(GlobalInfo.s_globalInfo.isDebug ? Log.VERBOSE : Log.ERROR);
            String secret = String.format("appsecret=%s;target=%s", HOCKEY_APP_ID, HOCKEY_APP_PKG);
            AppCenter.start(application, secret, Analytics.class, Crashes.class);  //  , Distribute.class, Push.class, Auth.class, Data.class);
            // AppCenter.start(application, HOCKEY_APP_ID, Analytics.class, Crashes.class);
            AppCenter.setUserId(application.getPackageName());

            /* Use some App Center getters. */
            AppCenter.getInstallId().thenAccept(new AppCenterConsumer<UUID>() {

                @Override
                public void accept(UUID uuid) {
                    Log.d(TAG, "InstallId=" + uuid);
                }
            });

            Crashes.hasCrashedInLastSession().thenAccept(new AppCenterConsumer<Boolean>() {
                @Override
                public void accept(Boolean crashed) {
                    Log.d(TAG, "Crashes.hasCrashedInLastSession=" + crashed);
                }
            });
            Crashes.getLastSessionCrashReport().thenAccept(new AppCenterConsumer<ErrorReport>() {
                @Override
                public void accept(ErrorReport data) {
                    if (data != null) {
                        Log.d(TAG, "Crashes.getLastSessionCrashReport().getThrowable()=", data.getThrowable());
                    }
                }
            });
        }
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
        Throwable tr = report.getThrowable();
        if (tr == null || tr.getMessage() == null)
            return true;

        // Exclude these crash report exceptions
        // android.app.RemoteServiceException: Bad notification posted from package com.wood.android.weather: Couldn't expand RemoteViews for: ...
        boolean hasRemoteViews = tr.getMessage().contains("RemoteViews");
        boolean hasRemoteServiceEx = tr.getMessage().contains("RemoteServiceException");
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
