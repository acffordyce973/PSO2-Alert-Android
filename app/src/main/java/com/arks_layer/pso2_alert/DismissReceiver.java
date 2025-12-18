package com.arks_layer.pso2_alert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class DismissReceiver extends BroadcastReceiver {

    @Override
    public void onReceive (Context context, Intent intent) {
        String action = intent.getAction() ;
        if (action.equals(context.getString(R.string.intent_action_dismissed))) {
            Log.i("DismissReceiver", "Dismiss Received");
            ForegroundService.startService(context);
            Toast.makeText (context , "Manually checking for alerts" , Toast. LENGTH_SHORT ).show() ;
        }
    }
}