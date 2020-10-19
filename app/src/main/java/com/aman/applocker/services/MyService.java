package com.aman.applocker.services;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.aman.applocker.AppsPass;
import com.aman.applocker.DBAdapter;
import com.aman.applocker.receiver.Restater;
import com.aman.applocker.utils.LockActivity;
import com.aman.applocker.utils.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class MyService extends Service {

    private int mInterval = 800;
    private Handler mHandler = new Handler();
    String app;
    ArrayList<AppsPass> appsPasses = new ArrayList<>();
    ArrayList<String> application = new ArrayList<>();
    boolean isScreenAwake, isStarted = false,
            isUnlocked = false, temp = false, isUpdated = false;
    Restater broadcast;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // mInterval = intent.getIntExtra("time", 1000);
        isUnlocked = intent.getBooleanExtra("lockStat", false);
        isUpdated = intent.getBooleanExtra("updated", false);
        retrieve();
        for (int i = 0; i < appsPasses.size(); i++) {
            application.add(appsPasses.get(i).getName());
        }
        if (!isStarted)
            startRepeatingTask();
        isStarted = true;
        return START_STICKY;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName,
                NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder
                .setOngoing(true)
                .setContentTitle("App Locker")
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(2, notification);
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                PowerManager powerManager = (PowerManager) getBaseContext().getSystemService(Context.POWER_SERVICE);
                isScreenAwake = (Build.VERSION.SDK_INT < 20 ? powerManager.isScreenOn() : powerManager.isInteractive());
                if (isScreenAwake) {
                    if (isUpdated) {
                        retrieve();
                        for (int i = 0; i < appsPasses.size(); i++) {
                            application.add(appsPasses.get(i).getName());
                        }
                    }
                    app = getForegroundApp(); //this function can change value of mInterval.
                    temp = application.contains(app);
                    if (temp) {
                        if (!isUnlocked) {

                            Intent intent = new Intent(getBaseContext(), LockActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
                                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("packageToLockScreen", app);
                            startActivity(intent);
                        }
                    }
                }
                if (!temp || !isScreenAwake) {
                    isUnlocked = false;
                }
                if (broadcast == null) {
                    broadcast = new Restater();
                    registerReceiver(broadcast, new IntentFilter("com.android.ServiceStopped"));
                }
            } finally {
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };


    public String getForegroundApp() {
        String currentApp = "NULL";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            @SuppressLint("WrongConstant") UsageStatsManager mUsageStatsManager =
                    (UsageStatsManager) getBaseContext().getSystemService("usagestats");
            long time = System.currentTimeMillis();
// We get usage stats for the last 1 seconds
            List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                    time - 1000 * 10000, time);
// Sort the stats by the last time used
            if (stats != null) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
                for (UsageStats usageStats : stats) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (!mySortedMap.isEmpty()) {
                    return mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }
        } else {
            ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> RunningTask = mActivityManager.getRunningTasks(1);
            ActivityManager.RunningTaskInfo ar = RunningTask.get(0);
            return ar.topActivity.getClassName();
        }
        return currentApp;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopRepeatingTask();
        Intent intent = new Intent("com.android.ServiceStopped");
        getApplicationContext().sendBroadcast(intent);
//        if (broadcast != null)
//        unregisterReceiver(broadcast);
    }

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
        isStarted = false;
    }

    @Override
    public void onDestroy() {
        startStopedForcely();
        super.onDestroy();
        unregisterReceiver(broadcast);
        stopRepeatingTask();
    }

    void startStopedForcely() {
        AlarmManager alarmMgr = (AlarmManager) getBaseContext().getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(getApplicationContext(), MyService.class);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, i, 0);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 100, pendingIntent);
        //maybe 200 if error change to 400
    }


    private void retrieve() {
        appsPasses.clear();
        DBAdapter dbAdapter = new DBAdapter(this);
        dbAdapter.openDB();
        //retrieving..
        Cursor cursor = dbAdapter.getPass();
        //loop and add data to the arraylist..
        try {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String position = cursor.getString(2);

                AppsPass pdfFile = new AppsPass(id, name, position);
                appsPasses.add(pdfFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
        dbAdapter.closeDB();
        //check if array list is empty
    }
}
