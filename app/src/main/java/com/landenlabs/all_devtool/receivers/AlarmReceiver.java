package com.landenlabs.all_devtool.receivers;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import androidx.legacy.content.WakefulBroadcastReceiver;

import com.landenlabs.all_devtool.util.LLog;

/**
 * Created by Dennis Lang on 2/27/2015.
 *
 * Look into: android.app.job.JobScheduler
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {
    // Logger - set to LLog.DBG to only log in Debug build, use LLog.On to always log.
    private final LLog m_log = LLog.DBG;

    @Override
    public void onReceive(final Context context, Intent intent) {
        m_log.i("onReceive ");

        // This will update the UI with message
        // inst.setAlarmText("Alarm! Wake up! Wake up!");

        // This will sound the alarm tone
        // this will sound the alarm once, if you wish to
        // raise alarm in loop continuously then use MediaPlayer and setLooping(true)
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        Ringtone ringtone = RingtoneManager.getRingtone(context, alarmUri);
        ringtone.play();

        // This will send a notification message
        ComponentName comp = new ComponentName(context.getPackageName(), AlarmService.class.getName());
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}