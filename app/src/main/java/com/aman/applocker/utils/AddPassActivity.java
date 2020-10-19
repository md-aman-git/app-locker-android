package com.aman.applocker.utils;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.aman.applocker.DBAdapter;
import com.aman.applocker.services.MyService;
import com.aman.applocker.R;

import static android.widget.Toast.makeText;

public class AddPassActivity extends AppCompatActivity {

    EditText editText;
    Button btn;
    boolean isUpdated = false;
    String packagename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_add_pass);
        getSupportActionBar().hide();
        editText = findViewById(R.id.passSet);
        btn = findViewById(R.id.btnSetPass);
        packagename = getIntent().getStringExtra("packagename");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Please Enter The Field", Toast.LENGTH_SHORT).show();
                } else {
                    String pass = editText.getText().toString();
                    savePassToDatabase(packagename, pass);
                    isUpdated = true;
                    service(isUpdated);
                    Toast.makeText(getApplicationContext(), "Password For " + packagename, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    public void savePassToDatabase(String name, String position) {
        DBAdapter dbAdapter = new DBAdapter(this);
        //open database..
        dbAdapter.openDB();

        //Commit..
        dbAdapter.insertData(name, position);

        //check if added to database or not..

        dbAdapter.closeDB();
        //refresh..
    }

    void service(boolean update) {
        Intent intent = new Intent(this, MyService.class);
        intent.putExtra("updated", update);
        startService(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (packagename.equals("com.aman.applocker")) {
            sendToHome();
        }
    }

    private void sendToHome() {
        Intent startHomescreen = new Intent(Intent.ACTION_MAIN);
        startHomescreen.addCategory(Intent.CATEGORY_HOME);
        startHomescreen.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(startHomescreen);
    }

    private void setFullScreen() {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }

    @Override
    protected void onDestroy() {
        System.gc();
        super.onDestroy();
    }
}
