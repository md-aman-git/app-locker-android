package com.aman.applocker;

import android.app.AppOpsManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aman.applocker.utils.AddPassActivity;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.MyViewHolder> {

    private Context mContext;
    private ArrayList<AppInformation> appInformations;
    private ArrayList<AppsPass> appsPasses;

    public AppsAdapter(Context mContext, ArrayList<AppInformation> appInformations, ArrayList<AppsPass> appsPasses) {
        this.mContext = mContext;
        this.appInformations = appInformations;
        this.appsPasses = appsPasses;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.apps_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.app_name.setText(appInformations.get(position).getAppName());
        ArrayList<String> appcontains = new ArrayList<>();
        for (int i = 0; i < appsPasses.size(); i++) {
            appcontains.add(appsPasses.get(i).getName());
        }
        if (!appcontains.contains(appInformations.get(position).getPackageName())) {
            holder.lock.setImageResource(R.drawable.ic_lock_open);
        }
        else {
            holder.lock.setImageResource(R.drawable.ic_lock_closed);
        }
        if (appInformations.get(position).getIcon() != null)
        {
            Glide.with(mContext).load(appInformations.get(position).getIcon())
                    .into(holder.iconApp);
        }
        else
        {
            Glide.with(mContext).load(R.mipmap.ic_launcher_round)
                    .into(holder.iconApp);
        }
        holder.appitem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.e(TAG, "Intent : " + appInformations.get(position).getMintent());
                    mContext.startActivity(appInformations.get(position).getMintent());
                }
                catch (ActivityNotFoundException | NullPointerException e)
                {
                    Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        holder.lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAccessGranted()) {
                    Intent intent = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                    }
                    CustomDialogClass cdd=new CustomDialogClass(mContext, intent);
                    cdd.show();
                }
                else
                    {
                    ArrayList<String> appcontains = new ArrayList<>();
                    for (int i = 0; i < appsPasses.size(); i++) {
                        appcontains.add(appsPasses.get(i).getName());
                    }
                    if (!appcontains.contains(appInformations.get(position).getPackageName())) {
                        Intent intent = new Intent(mContext, AddPassActivity.class);
                        intent.putExtra("packagename", appInformations.get(position).getPackageName());
                        mContext.startActivity(intent);
                    } else {
                        Toast.makeText(mContext, "Password Is Already Set.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return appInformations.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView iconApp, lock;
        TextView app_name;
        RelativeLayout appitem;
        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            iconApp = itemView.findViewById(R.id.icon_apps);
            app_name = itemView.findViewById(R.id.apps_name);
            appitem = itemView.findViewById(R.id.app_item);
            lock = itemView.findViewById(R.id.lock);
        }
    }

    private boolean isAccessGranted() {
        try {
            PackageManager packageManager = mContext.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(mContext.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) mContext.getSystemService(Context.APP_OPS_SERVICE);
            int mode = 0;
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (appOpsManager != null) {
                    mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                            applicationInfo.uid, applicationInfo.packageName);
                }
            }
            return (mode == AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
