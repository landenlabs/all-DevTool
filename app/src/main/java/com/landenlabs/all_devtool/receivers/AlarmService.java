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

import android.app.IntentService;
import android.content.Intent;

import com.landenlabs.all_devtool.shortcuts.util.Utils;

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
        Utils.showAlarmNotification(this, Utils.CLOCK_NOTIFICATION_ID, "Alarm!");
        //xx AlarmReceiver.completeWakefulIntent(intent);
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