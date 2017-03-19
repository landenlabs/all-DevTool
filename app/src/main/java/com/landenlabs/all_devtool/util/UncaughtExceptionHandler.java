package com.landenlabs.all_devtool.util;

import android.util.Log;

import java.text.SimpleDateFormat;

/**
 * Created by Dennis Lang on 5/1/16.
 */
public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final String APP_VERSION_INFO_ID_FORMAT = "%s; version info";
    private static final String ERROR_REPORT_FORMAT = "yyyy.MM.dd HH:mm:ss z";
    private SimpleDateFormat format = new SimpleDateFormat(ERROR_REPORT_FORMAT);

    private Thread.UncaughtExceptionHandler originalHandler;

    /**
     * Creates a reporter instance
     *
     * @throws NullPointerException if the parameter is null
     */
    public UncaughtExceptionHandler() throws NullPointerException {
        originalHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

        String stackTrace = Log.getStackTraceString(ex);
        Log.d("UncaughtException", stackTrace);
        Log.e("UncaughtException", ex.getLocalizedMessage(), ex);

        if (originalHandler != null) {
            originalHandler.uncaughtException(thread, ex);
        }
    }
}
