package com.arks_layer.pso2_alert;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ForegroundService extends Service {

    public static void startService(Context context) {
        //stopService(context);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms())
            {
                Log.e("ForeServ", "Cannot schedule exact alarms");
                return;
            }
        }

        Intent startIntent = new Intent(context, ForegroundService.class);
        ContextCompat.startForegroundService(context, startIntent);
    }

    public static void stopService(Context context) {
        Intent stopIntent = new Intent(context, ForegroundService.class);
        context.stopService(stopIntent);
    }

    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {
        Preferences sharedPref = new Preferences(this);

        Calendar currentCalendar = Calendar.getInstance();
        Integer currentMinute = currentCalendar.get(Calendar.MINUTE);
        Integer refreshSeconds = 300;

        //if (currentMinute > 54) { refreshSeconds = 4800 - (currentMinute * 60); } //Time until :20 from before the hour
        if (currentMinute < 20) { refreshSeconds = 1200 - (currentMinute * 60); } //Time until :20 after the hour
        else if (currentMinute < 25) { refreshSeconds = 1500 - (currentMinute * 60); } //Time until :25 after the hour
        else if (currentMinute < 30) { refreshSeconds = 1800 - (currentMinute * 60); } //Time until :30 after the hour
        else if (currentMinute < 50) { refreshSeconds = 3000 - (currentMinute * 60); } //Time until :50 after the hour
        else if (currentMinute < 55) { refreshSeconds = 3300 - (currentMinute * 60); } //Time until :55 after the hour
        else if (currentMinute > 54) { refreshSeconds = 3600 - (currentMinute * 60); } //Time until :00

        Long waitMillis = TimeUnit.SECONDS.toMillis(refreshSeconds);

        DateFormat df = new SimpleDateFormat("HH:mm", Locale.getDefault());
        if (!android.text.format.DateFormat.is24HourFormat(this)) { df = new SimpleDateFormat("hh:mm a", Locale.getDefault()); }
        long triggerEpoch = System.currentTimeMillis() + waitMillis;

        Date triggerDate = new java.util.Date(triggerEpoch);

        Log.i("ForeServ", "Next Service refresh is at " + triggerDate);

        Intent servNotifIntent = new Intent(this, MainActivity.class);
        PendingIntent servPendIntent = PendingIntent.getActivity(  this, getResources().getInteger(R.integer.service_notification_pending_code), servNotifIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);

        Intent disIntent = new Intent(this, DismissReceiver.class ) ;
        disIntent.setAction(getString(R.string.intent_action_dismissed)) ;
        PendingIntent disPendIntent = PendingIntent.getBroadcast (this, getResources().getInteger(R.integer.service_dismissed_pending_code) , disIntent , PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE ) ;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.notification_channel_service))
                .setContentTitle(getString(R.string.app_name))
                .setSubText(getString(R.string.label_notification_service_sub))
                .setContentText(getString(R.string.label_notification_service_content, df.format(triggerDate)))
                .setOngoing(true)
                .setSilent(true)
                .setGroup(getString(R.string.notification_group))
                .setSmallIcon(R.drawable.notification)
                .setDeleteIntent(disPendIntent)
                .setContentIntent(servPendIntent);

        Log.d("ForeServ", "Starting Foreground Service");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(getResources().getInteger(R.integer.foreground_service_id), builder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC); //Start the service and its notification
        }
        else
        {
            startForeground(getResources().getInteger(R.integer.foreground_service_id), builder.build());
        }

        //Restart the service after x minutes to check for quests again
		Intent timerNotifIntent = new Intent(this, AlarmReceiver.class);
		PendingIntent timerPendIntent = PendingIntent.getBroadcast(this, getResources().getInteger(R.integer.alarm_pending_code), timerNotifIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
		
		AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        try {
            long triggerMillis = SystemClock.elapsedRealtime() + waitMillis;
            Log.d("ForeServ", "Setting Alarm from " + SystemClock.elapsedRealtime() + " to " + triggerMillis);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerMillis, timerPendIntent);
        }
        catch (SecurityException e)
        {
            Log.e("ForeServ", "Alarm Manager: " + e.getMessage());
            e.printStackTrace();
        }

        //Check for alerts on another thread
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            AlertsCheck alertsCheck = new AlertsCheck(this);
        });

        //Keep the service alive even if the user backs out of the app
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}