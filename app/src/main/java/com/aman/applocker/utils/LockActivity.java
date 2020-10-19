package com.aman.applocker.utils;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.aman.applocker.AppsPass;
import com.aman.applocker.DBAdapter;
import com.aman.applocker.services.MyService;
import com.aman.applocker.R;
import com.aman.applocker.receiver.Restater;

import java.util.ArrayList;

public class LockActivity extends AppCompatActivity {

    EditText pass;
    Button btnSubmit;
    Restater broadcast;
    boolean isUnlocked = false;
    ArrayList<AppsPass> appsPasses = new ArrayList<>();
    int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_lock);
        pass = findViewById(R.id.password);
        btnSubmit = findViewById(R.id.btnSubmit);
        broadcast = new Restater();
        registerReceiver(broadcast, new IntentFilter("com.android.ServiceStopped"));
        //password retrieved.
        final String pack = getIntent().getStringExtra("packageToLockScreen");
        retrieve();
        for (int i = 0; i < appsPasses.size(); i++) {
            if (appsPasses.get(i).getName().equals(pack))
                id = appsPasses.get(i).getId() - 1;
        }
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pass.getText().toString().equals("")) {
                    if (pass.getText().toString().equals(appsPasses.get(id).getPosition())) {
                        isUnlocked = true;
                        service(isUnlocked);
                        finish();
                    } else {
                        Animation shake = AnimationUtils.loadAnimation(getApplicationContext(),
                                R.anim.shake);
                        pass.startAnimation(shake);
                        pass.setText("");
                        Toast.makeText(LockActivity.this,
                                "Wrong PassWord..", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Animation shake = AnimationUtils.loadAnimation(getApplicationContext(),
                            R.anim.shake);
                    pass.startAnimation(shake);
                }
            }
        });
    }

    void service(boolean lock) {
        Intent intent = new Intent(this, MyService.class);
        intent.putExtra("lockStat", lock);
        startService(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sendToHome();
    }

    private void sendToHome() {
        Intent startHomescreen = new Intent(Intent.ACTION_MAIN);
        startHomescreen.addCategory(Intent.CATEGORY_HOME);
        startHomescreen.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(startHomescreen);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //sendToHome();
//        if (broadcast != null)
//            unregisterReceiver(broadcast);
    }

    @Override
    protected void onDestroy() {
        System.gc();
        super.onDestroy();
        if (broadcast != null)
            unregisterReceiver(broadcast);
    }

    private void setFullScreen() {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

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
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        overridePendingTransition(0, 0);
    }
}
