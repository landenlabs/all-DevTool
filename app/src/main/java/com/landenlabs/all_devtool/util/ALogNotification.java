package com.landenlabs.all_devtool.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.landenlabs.all_devtool.DevToolActivity;
import com.landenlabs.all_devtool.R;

import java.util.ArrayList;
import java.util.Arrays;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Manage Notification draw
 */
public class ALogNotification {

    private static final int notificationId = 101;
    private static final String CHANNEL_ID = "AlarmId";
    private static final String channel_name = "Alarm";
    private static String channel_description;

    public static void init(Context context) {
        channel_description = context.getPackageName();

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = channel_name;
            String description = channel_description;
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = SysUtils.getServiceSafe(context, Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    static ArrayList<String> prevMsgs = new ArrayList<>(Arrays.asList("\n"));

    public static void updateNotification(Context context, Object... msgs) {

        String newMsg = TextUtils.join(" ", msgs);
        prevMsgs.add(newMsg);
        if (prevMsgs.size() > 4) {
            prevMsgs.remove(0);
        }

        Bitmap largeBitmap =
        BitmapFactory.decodeResource(context.getResources(), R.drawable.dev_tool_ic, null);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.dev_tool)
                .setLargeIcon(largeBitmap)
                .setContentTitle(channel_description)
                .setContentText(newMsg)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(TextUtils.join("\n", prevMsgs)))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(context, DevToolActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        // Set the intent that will fire when the user taps the notification
        mBuilder.setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

    // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId, mBuilder.build());
    }
}
