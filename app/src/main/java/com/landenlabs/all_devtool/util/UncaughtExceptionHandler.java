package com.landenlabs.all_devtool.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Looper;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.landenlabs.all_devtool.R;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by Dennis Lang on 5/1/16.
 */
@SuppressWarnings("Convert2Lambda")
public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private final Context context;
    private final SharedPreferences prefs;
    private static final String CRASH_REPORT = "CrashReport";

    private Thread.UncaughtExceptionHandler originalHandler;

    /**
     * Creates a reporter instance
     *
     * @throws NullPointerException if the parameter is null
     */
    public UncaughtExceptionHandler(Context context) throws NullPointerException {
        originalHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);

        this.context = context;

        StrictMode.ThreadPolicy policy = StrictMode.allowThreadDiskReads();
        StrictMode.allowThreadDiskWrites();
        prefs = context.getSharedPreferences(CRASH_REPORT, Context.MODE_PRIVATE);
        String msg = prefs.getString(CRASH_REPORT, "");

        if (!TextUtils.isEmpty(msg)) {
            prefs.edit().remove(CRASH_REPORT).apply();
            showCrashDialog(msg);
        }
        StrictMode.setThreadPolicy(policy);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

        String stackTrace = Log.getStackTraceString(ex);
        Log.d("UncaughtException", stackTrace);
        Log.e("UncaughtException", ex.getLocalizedMessage(), ex);

        saveCrashReport(thread, ex);

        if (originalHandler != null) {
            originalHandler.uncaughtException(thread, ex);
        }
    }

    /**
     * Save stack trace in preferences to display on next app run.
     */
    private void saveCrashReport(Thread thread, Throwable ex) {
        try {
            StringBuilder report = new StringBuilder();
            report.append("Thread:")
                    .append(thread.getName())
                    .append("@")
                    .append(thread.getId())
                    .append("\n");
            report.append("Exception:\n")
                    .append(ex.getMessage())
                    .append("\nCause\n")
                    .append(ex.getCause())
                    .append("\n");
            report.append("Stack:\n");
            final Writer result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);
            ex.printStackTrace(printWriter);
            report.append(result.toString());
            printWriter.close();
            report.append('\n');

            StrictMode.ThreadPolicy policy = StrictMode.allowThreadDiskWrites();
            prefs.edit().putString(CRASH_REPORT, report.toString()).apply();
            StrictMode.setThreadPolicy(policy);
        } catch (Throwable ignore) {
        }
    }

    /**
     * Show crash trace in Alert dialog, pause main app from running.
     */
    private void showCrashDialog(final String msg) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // new Thread() {
        //    @Override
        //    public void run() {
        //        Looper.prepare();

        if (false) {
            builder.setTitle("Crash report:");
            builder.create();
            builder.setNegativeButton("Continue",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    });
            builder.setPositiveButton("Email",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent sendIntent = new Intent(Intent.ACTION_SEND);
                            StringBuilder body = new StringBuilder("Trace\n\n")
                                    .append(msg);
                            //        .append(wsiApp.getAllVersions());

                            // sendIntent.setType("text/plain");
                            sendIntent.setType("message/rfc822");
                            //    sendIntent.putExtra(Intent.EXTRA_EMAIL,
                            //           new String[] { "wsimobile1@gmail.com" });
                            sendIntent.putExtra(Intent.EXTRA_TEXT, body.toString());
                            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Crash report");
                            sendIntent.setType("message/rfc822");
                            context.startActivity(sendIntent);
                            System.exit(0);
                        }
                    });


            builder.setMessage(msg);
            builder.show();
        } else {

            if (Build.VERSION.SDK_INT >= 21) {
                builder.setView(R.layout.crash_dlg);
                AlertDialog dialog = builder.show();

                ((TextView)dialog.findViewById(R.id.crash_text)).setText(msg);

                dialog.findViewById(R.id.crash_cont_btn).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                System.exit(0);
                            }
                        }
                );

                dialog.findViewById(R.id.crash_email_btn).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                                StringBuilder body = new StringBuilder("Trace\n\n")
                                        .append(msg);
                                //        .append(wsiApp.getAllVersions());

                                // sendIntent.setType("text/plain");
                                sendIntent.setType("message/rfc822");
                                //    sendIntent.putExtra(Intent.EXTRA_EMAIL,
                                //           new String[] { "wsimobile1@gmail.com" });
                                sendIntent.putExtra(Intent.EXTRA_TEXT, body.toString());
                                sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Crash report");
                                sendIntent.setType("message/rfc822");
                                context.startActivity(sendIntent);
                                System.exit(0);
                            }
                        }
                );

            }
        }

        Looper.loop();
        //   }
        // }.start();
    }
}
