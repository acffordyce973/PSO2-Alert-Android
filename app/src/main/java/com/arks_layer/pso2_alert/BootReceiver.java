package com.arks_layer.pso2_alert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BootReceiver extends BroadcastReceiver {
    Context _context;

    private void waitStartService()
    {
        try {
            Log.d("UpdatedReceiver", "Waiting to start service");
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Log.e("UpdatedReceiver", "Wait Start Service: " + e.getMessage());
            e.printStackTrace();
        }

        Log.i("UpdatedReceiver", "Starting service");
        ForegroundService.startService(_context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        _context = context;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(this::waitStartService);
    }
}