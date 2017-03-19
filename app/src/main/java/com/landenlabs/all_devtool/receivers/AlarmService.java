package com.landenlabs.all_devtool.receivers;

import android.app.IntentService;
import android.content.Intent;

import com.landenlabs.all_devtool.util.Utils;

/**
 * Created by Dennis Lang on 2/27/2015.
 */
public class AlarmService extends IntentService {
    // private NotificationManager alarmNotificationManager;

    public AlarmService() {
        super("AlarmService");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        // sendNotification("DevStuff Alarm!");
        Utils.sendNotification(this, Utils.CLOCK_NOTIFICATION_ID, "Alarm!");
        AlarmReceiver.completeWakefulIntent(intent);
    }

    /*
    public void sendNotification(String msg) {
        m_log.i("Preparing to send notification...: " + msg);
        alarmNotificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, DevStuffActivity.class), 0);

        // Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.dev_tool);
        NotificationCompat.Builder alamNotificationBuilder =
                new NotificationCompat.Builder(this)
                        .setContentTitle("DevStuff Alarm")
                        .setSmallIcon(R.drawable.dev_tool)
                       // .setLargeIcon(largeIcon)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                        .setContentText(msg);

        alamNotificationBuilder.setContentIntent(contentIntent);
        alarmNotificationManager.notify(1, alamNotificationBuilder.build());
        m_log.i("Notification sent.");
    }

    */
}