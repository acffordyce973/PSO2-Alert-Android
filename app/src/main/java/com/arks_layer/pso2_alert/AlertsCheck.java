package com.arks_layer.pso2_alert;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.zone.ZoneRules;
import java.util.Arrays;
import java.util.Locale;

public class AlertsCheck
{
    Preferences sharedPref = null;

    private boolean isAlertEnabled(String alertTitle)
    {

        return false;
    }

    private void checkAlertServer(Context context, Integer notifID, String notifChannel, String serverCode, String alertTitle)
    {
        //https://github.com/acffordyce973/PSO2-Alert/blob/master/PSO2-Alert/PSO2-Alert.Xamarin/CheckForEmergencyQuests.cs
        if (!WebHelper.isNetworkConnected(context))
        {
            Log.e("AlertsCheck", "No internet connection");
            return;
        }

		String timeFormat = "H";
        DateFormat df = new SimpleDateFormat("HH:mm", Locale.getDefault());
        if (!android.text.format.DateFormat.is24HourFormat(context))
        {
            timeFormat = "h";
            df = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        }

        ZoneId z = ZoneId.systemDefault() ;
        ZoneRules rules = z.getRules() ;
        ZoneOffset offset = rules.getOffset( Instant.now() ) ;
        int offsetSeconds = offset.getTotalSeconds();
        int OffsetHours = offsetSeconds / 3600;

        String serverURL = context.getString(R.string.uri_main) + "/PSO2-API/eq_viewer.php?api=" + serverCode + "&offset=" + OffsetHours + "&format=" + timeFormat;
        Log.d("AlertsCheck", "Using the following server URL at " + serverURL);

        //Download data from server
        String apiData = null;
        try {
            apiData = WebHelper.getStringData(new URL(serverURL));
        } catch (Exception e) {
            Log.e("AlertsCheck", "Get API Data: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        //Split by newline
        //String[] apiEntries = apiData.split("\\R");
        String[] apiEntries = apiData.split("<br>");
        Log.d("AlertsCheck", "API Entries (" + apiEntries.length + "):" + System.lineSeparator() + String.join(System.lineSeparator(), Arrays.asList(apiEntries)));

        //Set our notification variables
        String notifTitle = alertTitle;
        String notifContent = "";
        String notifSub = df.format(new java.util.Date(System.currentTimeMillis()));

        //Loop each line
            for (String apiEntry : apiEntries) {
                try {
                    if (notifContent.length() > 200) {
                        break;
                    }
                    if (apiEntry.contains("Current Time: ") || apiEntry.length() < 10 || apiEntry.contains("upcoming alerts")) {
                        Log.d("AlertsCheck", "Skipping Web API title string " + apiEntry);
                        continue;
                    }

                    String eventTitle = apiEntry.split(" - ", 2)[1].trim();

                    SelectedAlert entryAlert = new SelectedAlert(context, eventTitle);
                    if (!entryAlert.isEnabled())
                    {
                        Log.d("AlertsCheck", "Skipping disabled alert named " + apiEntry);
                        continue;
                    }

                    notifContent = notifContent + Html.fromHtml(apiEntry, Html.FROM_HTML_MODE_COMPACT).toString() + System.lineSeparator();
                } catch (Exception e) {
                    Log.e("AlertsCheck", "Loop Entries: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            String previousAlertKey = context.getString(R.string.setting_previous, serverCode);
            String previousAlertHash = sharedPref.getString(previousAlertKey, "");
            String currentAlertHash = String.valueOf(notifContent.hashCode());
            if (notifContent.isEmpty())
            {
                Log.i("AlertsCheck", "There were " + apiEntries.length + " entries but no alerts were found");
                return;
            }
            else if (previousAlertHash.equals(currentAlertHash))
            {
                Log.i("AlertsCheck", "Previous alert hash is the same as the current alert");
                return;
            }

        if (notifContent.endsWith(System.lineSeparator())) { notifContent = notifContent.substring(0, notifContent.length() - 1); }
        Log.d("AlertsCheck", "Building notification to send");

        Intent servNotifIntent = new Intent(context, MainActivity.class);
        PendingIntent servPendIntent = PendingIntent.getActivity(  context, 0, servNotifIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, notifChannel)
                .setContentTitle(notifTitle)
                .setSubText(notifSub)
                .setContentText(notifContent)
                .setGroup(context.getString(R.string.notification_group))
                .setSmallIcon(R.drawable.notification)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notifContent.trim()))
                .setContentIntent(servPendIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Log.i("AlertsCheck", "Sending notification for " + serverCode + " with " + notifContent.length() + " characters");
        notificationManager.notify(notifID, builder.build());

        sharedPref.putString(previousAlertKey, currentAlertHash);
    }

    public AlertsCheck(Context context)
    {
        super();

        sharedPref = new Preferences(context);

        if (sharedPref.getBoolean(context.getString(R.string.code_global_ngs_quest), true))
        {
            checkAlertServer(context, context.getResources().getInteger(R.integer.glbn_notification_id),context.getString(R.string.notification_channel_glbn), context.getString(R.string.code_global_ngs_quest), context.getString(R.string.name_global_ngs_quest));
        }
        if (sharedPref.getBoolean(context.getString(R.string.code_global_ngs_field), true))
        {
            checkAlertServer(context, context.getResources().getInteger(R.integer.glbf_notification_id), context.getString(R.string.notification_channel_glbf), context.getString(R.string.code_global_ngs_field), context.getString(R.string.name_global_ngs_field));
        }
        if (sharedPref.getBoolean(context.getString(R.string.code_global_classic), false))
        {
            checkAlertServer(context, context.getResources().getInteger(R.integer.glbc_notification_id), context.getString(R.string.notification_channel_glbc), context.getString(R.string.code_global_classic), context.getString(R.string.name_global_classic));
        }
        if (sharedPref.getBoolean(context.getString(R.string.code_japan_ngs_quest), false))
        {
            checkAlertServer(context, context.getResources().getInteger(R.integer.jpnn_notification_id), context.getString(R.string.notification_channel_jpnn), context.getString(R.string.code_japan_ngs_quest), context.getString(R.string.name_japan_ngs_quest));
        }
        if (sharedPref.getBoolean(context.getString(R.string.code_japan_ngs_field), false))
        {
            checkAlertServer(context, context.getResources().getInteger(R.integer.jpnf_notification_id), context.getString(R.string.notification_channel_jpnf), context.getString(R.string.code_japan_ngs_field), context.getString(R.string.name_japan_ngs_field));
        }
        if (sharedPref.getBoolean(context.getString(R.string.code_japan_classic), false))
        {
            checkAlertServer(context, context.getResources().getInteger(R.integer.jpnc_notification_id), context.getString(R.string.notification_channel_jpnc), context.getString(R.string.code_japan_classic), context.getString(R.string.name_japan_classic));
        }
    }
}
