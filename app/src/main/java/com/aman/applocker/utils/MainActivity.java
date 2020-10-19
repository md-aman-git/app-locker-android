package com.aman.applocker.utils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;

import com.aman.applocker.AppInformation;
import com.aman.applocker.AppsAdapter;
import com.aman.applocker.AppsPass;
import com.aman.applocker.DBAdapter;
import com.aman.applocker.services.MyService;
import com.aman.applocker.R;
import com.aman.applocker.receiver.Restater;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    AppsAdapter appsAdapter;
    ArrayList<AppInformation> informations;
    Restater broadcast;
    String myPack = "com.aman.applocker";
    static ArrayList<AppsPass> appsPasses = new ArrayList<>();
    ArrayList<String> myPackageName = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recyclerView);
        if (broadcast == null) {
            broadcast = new Restater();
            registerReceiver(broadcast, new IntentFilter("com.android.ServiceStopped"));
        }
        final PackageManager pm = getPackageManager();
        //get a list of installed apps.
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        informations = new ArrayList<>();

        for (ApplicationInfo packageInfo : packages) {
            Intent intent = pm.getLaunchIntentForPackage(packageInfo.packageName);
            if (intent != null) {
                AppInformation newInfo = new AppInformation(packageInfo.loadLabel(pm).toString(),
                        packageInfo.packageName, packageInfo.loadIcon(pm), intent);
                informations.add(newInfo);
            }
        }
        retrieve();
        Intent intent = new Intent(MainActivity.this, MyService.class);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        retrieve();
        for (int i = 0; i < appsPasses.size(); i++) {
            myPackageName.add(appsPasses.get(i).getName());
        }
        if (!myPackageName.contains(myPack)) {
            Intent intent = new Intent(this, AddPassActivity.class);
            //below line is must for text password don't delete
            intent.putExtra("packagename", myPack);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        appsAdapter = new AppsAdapter(this, informations, appsPasses);
        recyclerView.setAdapter(appsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                RecyclerView.VERTICAL, false));
        //  send user to access app usage if not.
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        System.gc();
        super.onDestroy();
        if (broadcast != null)
            unregisterReceiver(broadcast);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
