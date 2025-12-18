package com.arks_layer.pso2_alert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class ScreenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive (Context context, Intent intent) {
        String action = intent.getAction() ;
        if (action.equals(context.getString(R.string.intent_action_dismissed))) {
            Log.i("DismissReceiver", "Dismiss Received");
            ForegroundService.startService(context);
            Toast.makeText(context, context.getString(R.string.toast_message_manual_check), Toast.LENGTH_SHORT).show();
        }
    }
}