package com.arks_layer.pso2_alert;

import static android.app.AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.Objects;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive (Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED))
        {
            Log.d("AlarmReceiver", "Alarm Permission Received");
            Toast.makeText(context, context.getString(R.string.toast_message_alarms_granted), Toast.LENGTH_LONG).show();
            MainActivity.triggerRebirth(context);
            return;
        }

        Log.d("AlarmReceiver", "Alarm Received");
        ForegroundService.startService(context);
    }
}