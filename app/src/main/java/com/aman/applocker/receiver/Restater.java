package com.aman.applocker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.aman.applocker.services.MyService;

public class Restater extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        intent.putExtra("time", 1000);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(new Intent(context, MyService.class));
            } else {
                context.startService(new Intent(context, MyService.class));
            }
        }
}
