package com.arks_layer.pso2_alert;

import static android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS;
import static android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.NotificationChannelGroup;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    public static Context contextOfApplication;

    Preferences sharedPref = null;

    List<SelectedServer> selectedServers;
    List<SelectedServer> enabledServers;
    List<SelectedAlert> selectedAlerts;

    public static Context getContextOfApplication(){
        return contextOfApplication;
    }

    public void showToast(Context context, String message, int duration)
    {
        try {
            Handler mainHandler = new Handler(Looper.getMainLooper());
            mainHandler.post(() -> {
                Toast.makeText(context, message, duration).show();
            });
        }
        catch (Exception e) {
            Log.e("MainActivity", "Show Toast: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void triggerKill(Context context)
    {
        ForegroundService.stopService(this);

        AlarmManager alarmManager2 = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(getApplicationContext(),getResources().getInteger(R.integer.alarm_pending_code) ,new Intent(this, AlarmReceiver.class),PendingIntent.FLAG_IMMUTABLE);
        alarmManager2.cancel(pendingIntent2);

        finish();
        System.exit(0);
    }

    public static void triggerRebirth(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        mainIntent.setPackage(context.getPackageName());
        context.startActivity(mainIntent);
        Runtime.getRuntime().exit(0);
    }

    private ActivityResultLauncher<String> requestNotificationPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.i("MainActivity", "Notification Permission Granted");
                    showToast(this, getString(R.string.toast_message_notifications_granted), Toast.LENGTH_LONG);
                    triggerRebirth(this);
                } else {
                    showToast(this, getString(R.string.toast_message_notifications_failed), Toast.LENGTH_LONG);
                    Log.w("MainActivity", "Notification Permission Denied");
                }
            });

    private void configureNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) { return; }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            String permission = android.Manifest.permission.POST_NOTIFICATIONS;
            if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "Notification Permission Granted");
            }
            else
            {
                requestNotificationPermission.launch(permission);
            }
        }

        NotificationManager manager = getSystemService(NotificationManager.class);
        String groupName = getString(R.string.notification_group);

        Log.d("MainActivity", "Creating Notification Channels");
        NotificationChannel glbnChannel = new NotificationChannel(getString(R.string.notification_channel_glbn), getString(R.string.name_global_ngs_quest), NotificationManager.IMPORTANCE_HIGH);
        NotificationChannel glbcChannel = new NotificationChannel(getString(R.string.notification_channel_glbc), getString(R.string.name_global_classic), NotificationManager.IMPORTANCE_HIGH);
        NotificationChannel glbfChannel = new NotificationChannel(getString(R.string.notification_channel_glbf), getString(R.string.name_global_ngs_field), NotificationManager.IMPORTANCE_HIGH);
        NotificationChannel jpnnChannel = new NotificationChannel(getString(R.string.notification_channel_jpnn), getString(R.string.name_japan_ngs_quest), NotificationManager.IMPORTANCE_HIGH);
        NotificationChannel jpncChannel = new NotificationChannel(getString(R.string.notification_channel_jpnc), getString(R.string.name_japan_classic), NotificationManager.IMPORTANCE_HIGH);
        NotificationChannel jpnfChannel = new NotificationChannel(getString(R.string.notification_channel_jpnf), getString(R.string.name_japan_ngs_field), NotificationManager.IMPORTANCE_HIGH);
        NotificationChannel serviceChannel = new NotificationChannel(getString(R.string.notification_channel_service), getString(R.string.name_service), NotificationManager.IMPORTANCE_LOW);

        Log.d("MainActivity", "Creating Notification Channel Groups");
        NotificationChannelGroup groupChannel = new NotificationChannelGroup(groupName, "Alert");
        manager.createNotificationChannelGroup(groupChannel);

        Log.d("MainActivity", "Setting Notification Channel Groups");
        glbnChannel.setGroup(groupName);
        glbcChannel.setGroup(groupName);
        glbfChannel.setGroup(groupName);
        jpnnChannel.setGroup(groupName);
        jpncChannel.setGroup(groupName);
        jpnfChannel.setGroup(groupName);

        Log.d("MainActivity", "Setting Notification Channel Descriptions");
        glbnChannel.setDescription(getString(R.string.channel_global_ngs_quest_description));
        glbcChannel.setDescription(getString(R.string.channel_global_classic_description));
        glbfChannel.setDescription(getString(R.string.channel_global_ngs_field_description));
        jpnnChannel.setDescription(getString(R.string.channel_japan_ngs_quest_description));
        jpncChannel.setDescription(getString(R.string.channel_japan_classic_description));
        jpnfChannel.setDescription(getString(R.string.channel_japan_ngs_field_description));

        Log.d("MainActivity", "Adding Notification Channels");
        manager.createNotificationChannel(serviceChannel);
        manager.createNotificationChannel(glbnChannel);
        manager.createNotificationChannel(glbcChannel);
        manager.createNotificationChannel(glbfChannel);
        manager.createNotificationChannel(jpnnChannel);
        manager.createNotificationChannel(jpncChannel);
        manager.createNotificationChannel(jpnfChannel);
    }

    private void configureUnoptimised()
    {
        PowerManager powerManager = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        String packageName = getApplicationContext().getPackageName();
        if (powerManager.isIgnoringBatteryOptimizations(packageName)) { return; }

        AlertDialog.Builder alert = new AlertDialog.Builder(this, R.style.AlertDialogTheme);

        alert.setTitle(getString(R.string.dialog_title_battery));
        alert.setMessage(getString(R.string.dialog_message_battery));
        alert.setCancelable(false);

        Context context = this;

        alert.setNeutralButton("Ok", (dialog, whichButton) -> {
            showToast(context, getString(R.string.toast_message_battery), Toast.LENGTH_LONG);

            Intent requestIntent = new Intent(ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            requestIntent.setData(Uri.parse("package:" + packageName));
            startActivity(requestIntent);
        });

        alert.show();
    }

    private void configureAlarms()
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) { return; }

        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager.canScheduleExactAlarms()) { return; }

        AlertDialog.Builder alert = new AlertDialog.Builder(this, R.style.AlertDialogTheme);

        alert.setTitle(getString(R.string.dialog_title_alarms));
        alert.setMessage(getString(R.string.dialog_message_alarms));
        alert.setCancelable(false);

        Context context = this;

        alert.setNeutralButton("Ok", (dialog, whichButton) -> {
            showToast(context, getString(R.string.toast_message_alarms), Toast.LENGTH_LONG);

            Intent requestIntent = new Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            requestIntent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(requestIntent);

            triggerKill(context);
        });

        alert.show();
    }

    private List<SelectedServer> getEnabledServers()
    {
        enabledServers = new ArrayList<>();

        for (SelectedServer Server: selectedServers)
        {
            if (Server.isEnabled())
            {
                enabledServers.add(Server);
            }
        }

        return enabledServers;
    }
    private String getEnabledCodes(String serverChoice)
    {
        final String choiceAll = "All";

        List<String> serverCodes = new ArrayList<>();
        for (SelectedServer Server: getEnabledServers())
        {
            if (serverChoice.equals(choiceAll) || Server.getCode().contains(serverChoice))
            {
                serverCodes.add(Server.getCode());
            }
        }

        return String.join("|", serverCodes);
    }

    private void getServerList()
    {
        selectedServers = new ArrayList<>();

        selectedServers.add(new SelectedServer(contextOfApplication, getString(R.string.title_server_global_ngs_quest), getString(R.string.code_global_ngs_quest), sharedPref.getBoolean(getString(R.string.code_global_ngs_quest), true)));
        selectedServers.add(new SelectedServer(contextOfApplication, getString(R.string.title_server_global_ngs_field), getString(R.string.code_global_ngs_field), sharedPref.getBoolean(getString(R.string.code_global_ngs_field), true)));
        selectedServers.add(new SelectedServer(contextOfApplication, getString(R.string.title_server_global_classic), getString(R.string.code_global_classic), sharedPref.getBoolean(getString(R.string.code_global_classic), false)));

        selectedServers.add(new SelectedServer(contextOfApplication, getString(R.string.title_server_japan_ngs_quest), getString(R.string.code_japan_ngs_quest), sharedPref.getBoolean(getString(R.string.code_japan_ngs_quest), false)));
        selectedServers.add(new SelectedServer(contextOfApplication, getString(R.string.title_server_japan_ngs_field), getString(R.string.code_japan_ngs_field), sharedPref.getBoolean(getString(R.string.code_japan_ngs_field), false)));
        selectedServers.add(new SelectedServer(contextOfApplication, getString(R.string.title_server_japan_classic), getString(R.string.code_japan_classic), sharedPref.getBoolean(getString(R.string.code_japan_classic), false)));

        ChipGroup chipGroup = findViewById(R.id.settingServers);
        chipGroup.removeAllViews();

        for (SelectedServer serverEntry: selectedServers)
        {
            Chip newServer = new Chip(this);
            newServer.setText(serverEntry.getTitle());
            newServer.setCloseIconVisible(false);
            newServer.setClickable(true);
            newServer.setFocusable(true);
            newServer.setCheckable(true);
            newServer.setChecked(serverEntry.isEnabled());
            newServer.setChipBackgroundColor(getColorStateList(R.color.bg_chip_state_list));
            newServer.setTextColor(getResources().getColor(R.color.general_text));
            newServer.setId(ViewCompat.generateViewId());

            newServer.setOnCheckedChangeListener((compoundButton, b) -> {
                Log.d("MainActivity", "Setting " + serverEntry.getCode() + " to " + b);
                serverEntry.setEnabled(b);
                showToast(MainActivity.this, getString(R.string.toast_message_server_changed, serverEntry.getTitle(), b ? "enabled" : "disabled"), Toast.LENGTH_LONG);
            });

            chipGroup.addView(newServer);
        }
    }

    private void getAlertList()
    {
		selectedAlerts = new ArrayList<>();

        String serverURL = getString(R.string.uri_main) + "/PSO2-Resources/index.php?server=" + getEnabledCodes("All");
        Log.d("MainActivity", "Using the following server URL at " + serverURL);

        //Download data from server
        String apiData = null;
        try {
            apiData = WebHelper.getStringData(new URL(serverURL));
        } catch (Exception e) {
            Log.e("MainActivity", "Get Resources Data: " + e.getMessage());
            showToast(this, getString(R.string.toast_message_alert_list_failed, e.getMessage()), Toast.LENGTH_LONG);
            e.printStackTrace();
            return;
        }
        //Split by newline
        //String[] apiEntries = apiData.split("\\R");
        String[] apiEntries = apiData.split("<br>");
        Log.d("MainActivity", "Received " + apiEntries.length + " alert titles from the Resources API");

        for (String apiEntry : apiEntries)
        {
            selectedAlerts.add(new SelectedAlert(contextOfApplication, apiEntry));
        }
		
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(() -> {
            ChipGroup chipGroup = findViewById(R.id.settingAlerts);
            chipGroup.removeAllViews();

            for (SelectedAlert alertEntry : selectedAlerts) {
                Chip newAlert = new Chip(MainActivity.this);
                newAlert.setText(alertEntry.getTitle());
                newAlert.setCloseIconVisible(false);
                newAlert.setClickable(true);
                newAlert.setFocusable(true);
                newAlert.setCheckable(true);
                newAlert.setChecked(alertEntry.isEnabled());
                newAlert.setChipBackgroundColor(getColorStateList(R.color.bg_chip_state_list));
                newAlert.setTextColor(getResources().getColor(R.color.general_text));
                newAlert.setId(ViewCompat.generateViewId());

                newAlert.setOnCheckedChangeListener((compoundButton, b) -> {
                    Log.d("MainActivity", "Setting " + alertEntry.getTitle() + " to " + b);
                    alertEntry.setEnabled(b);
                    showToast(MainActivity.this, getString(R.string.toast_message_alert_changed, alertEntry.getTitle(), (b ? "enabled" : "disabled")), Toast.LENGTH_LONG);
                });

                chipGroup.addView(newAlert);
            }
        });
    }

    private void getAppMessage()
    {
        String serverURL = getString(R.string.uri_main) + "/PSO2-Alert/android_message.php";
        Log.d("MainActivity", "Using the following message URL at " + serverURL);

        //Download data from server
        String apiData = null;
        try {
            apiData = WebHelper.getStringData(new URL(serverURL));
        } catch (Exception e) {
            Log.e("MainActivity", "Get Message Data: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        //Dunno why it needs to be final but it does
        String finalApiData = apiData;

        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(() -> {
            TextView lblMessage = findViewById(R.id.lblMessage);
            lblMessage.setText(finalApiData);
        });
    }

    private void configureUserSettings()
    {
        TextView lblServersFail = findViewById(R.id.lblServersFail);
        TextView lblAlertsFail = findViewById(R.id.lblAlertsFail);

        if (WebHelper.isNetworkConnected(this))
        {
            lblServersFail.setVisibility(View.INVISIBLE);
            lblAlertsFail.setVisibility(View.INVISIBLE);

            //Download message on another thread
            ExecutorService msgExecutor = Executors.newSingleThreadExecutor();
            msgExecutor.execute(this::getAppMessage);

            getServerList();
            //Download list of alerts on another thread
            ExecutorService alertExecutor = Executors.newSingleThreadExecutor();
            alertExecutor.execute(this::getAlertList);
        }
        else
        {
            showToast(this, getString(R.string.label_no_connection), Toast.LENGTH_LONG);
            Log.e("MainActivity", getString(R.string.label_no_connection));
            lblServersFail.setVisibility(View.VISIBLE);
            lblAlertsFail.setVisibility(View.VISIBLE);
        }

        Spinner settingInterval = findViewById(R.id.settingInterval);
        ArrayAdapter adapterInterval = ArrayAdapter.createFromResource(this, R.array.intervals, R.layout.spinner_item);
        adapterInterval.setDropDownViewResource(R.layout.spinner_item_dropdown);
        settingInterval.setAdapter(adapterInterval);
        settingInterval.setSelection(adapterInterval.getPosition(String.valueOf(sharedPref.getInt(getString(R.string.setting_interval), 10))), false);

        settingInterval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try
                {
                    Integer newInterval = Integer.valueOf(String.valueOf(parent.getItemAtPosition(position)));
                    sharedPref.putInt(getString(R.string.setting_interval), newInterval);
                    showToast(MainActivity.this, "Check interval has been set to " + newInterval + " minutes!", Toast.LENGTH_LONG);
                    Log.i("MainActivity", "Check interval has been set to " + newInterval + " minutes");
                }
                catch (Exception e)
                {
                    Log.e("MainActivity", "Setting Interval: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) { }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms())
            {
                showToast(this, getString(R.string.toast_message_alarms_failed), Toast.LENGTH_LONG);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        contextOfApplication = getApplicationContext();
        sharedPref = new Preferences(this);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView lblDebug = findViewById(R.id.lblDebug);
        lblDebug.setText(getString(R.string.app_name) + " v" + com.arks_layer.pso2_alert.BuildConfig.VERSION_NAME);

        //Need to ask the user to use alarms
        configureAlarms();

        //Create notification channels and groups
        configureNotificationChannels();

        //Check if the user has opted out of battery optimizations
        configureUnoptimised();

		//Configure and display user settings
        configureUserSettings();

        //Start the service on boot
        BootReceiver bootReceiver = new BootReceiver();
        IntentFilter bootIntentFilter = new IntentFilter("android.intent.action.BOOT_COMPLETED");
        registerReceiver(bootReceiver, bootIntentFilter);

        //Start the service after an update
        UpdatedReceiver updateReceiver = new UpdatedReceiver();
        IntentFilter updateIntentFilter = new IntentFilter("android.intent.action.MY_PACKAGE_REPLACED");
        registerReceiver(updateReceiver, updateIntentFilter);

        //Add the code for our exit button
        FloatingActionButton btnExit = findViewById(R.id.btnExit);
        btnExit.setOnClickListener(view -> {
            AlertDialog.Builder alert = new AlertDialog.Builder(this, R.style.AlertDialogTheme);

            alert.setTitle(getString(R.string.dialog_title_exit));
            alert.setMessage(getString(R.string.dialog_message_exit));
            alert.setCancelable(true);

            Context context = this;

            alert.setPositiveButton("Exit App", (dialog, whichButton) -> {
                showToast(context, getString(R.string.toast_message_exit), Toast.LENGTH_LONG);
                triggerKill(context);
            });
            alert.setNegativeButton("Cancel", (dialog, whichButton) -> {
                dialog.cancel();
            });

            alert.show();
        });

        //Start the service on app start
        ForegroundService.startService(this);
    }

}