package ca.mimic.usagestatistics.Services;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.hanks.passcodeview.PasscodeView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import ca.mimic.usagestatistics.Activity.Settings;
import ca.mimic.usagestatistics.Database.DBUsage;
import ca.mimic.usagestatistics.R;
import ca.mimic.usagestatistics.Utils.SharedPreference;
import ca.mimic.usagestatistics.Utils.Tools;


public class AppCheckServices1 extends Service {

    public static final String TAG = "AppCheckServices";
    private Context context = null;
    private Timer timer;
    ImageView imageView;
    private WindowManager windowManager;
    private Dialog dialog;
    public static String currentApp = "";
    public static String previousApp = "";
    SharedPreference sharedPreference;
    List<String> pakageName;
    static DBUsage dbUsage;
    private static String day_old="";


    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        sharedPreference = new SharedPreference();
        if (sharedPreference != null) {
            pakageName = sharedPreference.getLocked(context);
        }
        timer = new Timer("AppCheckServices");
        timer.schedule(updateTask, 1000L, 1000L);


        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        imageView = new ImageView(this);
        imageView.setVisibility(View.GONE);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.CENTER;
        params.x = ((getApplicationContext().getResources().getDisplayMetrics().widthPixels) / 2);
        params.y = ((getApplicationContext().getResources().getDisplayMetrics().heightPixels) / 2);
        windowManager.addView(imageView, params);

//        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
//
//        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
//                WindowManager.LayoutParams.WRAP_CONTENT,
//                WindowManager.LayoutParams.WRAP_CONTENT,
//                WindowManager.LayoutParams.TYPE_PHONE,
//                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
//                PixelFormat.TRANSLUCENT);
//
//        params.gravity = Gravity.TOP | Gravity.CENTER;
//        params.x = ((getApplicationContext().getResources().getDisplayMetrics().widthPixels) / 2);
//        params.y = ((getApplicationContext().getResources().getDisplayMetrics().heightPixels) / 2);
//
//
//
//        windowManager.addView(imageView, params);
    }

    private TimerTask updateTask = new TimerTask() {
        @Override
        public void run() {
            if (sharedPreference != null) {
                pakageName = sharedPreference.getLocked(context);
            }
            if (isConcernedAppIsInForeground()) {
                if (imageView != null) {
                    imageView.post(new Runnable() {
                        public void run() {
                            if (!currentApp.matches(previousApp)) {
                                showUnlockDialog();
                                previousApp = currentApp;
                            }
                        }
                    });
                }
            } else {
                if (imageView != null) {
                    imageView.post(new Runnable() {
                        public void run() {
                            hideUnlockDialog();
                        }
                    });
                }
            }
        }
    };

    void showUnlockDialog() {
        showDialog();
    }

    void hideUnlockDialog() {
        previousApp = "";
        try {
            if (dialog != null) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void showDialog() {
        if (context == null)
            context = getApplicationContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View promptsView = layoutInflater.inflate(R.layout.popup_unlock, null);
        PasscodeView passcodeView = (PasscodeView) promptsView.findViewById(R.id.passcodeView);
        passcodeView.setListener(new PasscodeView.PasscodeViewListener(){
            @Override
            public void onFail() {
                Toast.makeText(getApplicationContext(), "Mật khẩu không đúng. Thử lại", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }

            @Override
            public void onSuccess(String number) {
                Toast.makeText(getApplicationContext(), "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
            }
        });

        dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        dialog.setContentView(promptsView);
        dialog.getWindow().setGravity(Gravity.CENTER);

        dialog.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_BACK
                        && event.getAction() == KeyEvent.ACTION_UP) {
                    Intent startMain = new Intent(Intent.ACTION_MAIN);
                    startMain.addCategory(Intent.CATEGORY_HOME);
                    startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(startMain);
                }
                return true;
            }
        });

        dialog.show();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {

        }
        /* We want this service to continue running until it is explicitly
        * stopped, so return sticky.
        */
        return START_STICKY;
    }

    public boolean isConcernedAppIsInForeground() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> task = manager.getRunningTasks(5);
        if (Build.VERSION.SDK_INT <= 20) {
            if (task.size() > 0) {
                ComponentName componentInfo = task.get(0).topActivity;
                for (int i = 0; pakageName != null && i < pakageName.size(); i++) {
                    if (componentInfo.getPackageName().equals(pakageName.get(i))) {
                        currentApp = pakageName.get(i);
                        return true;
                    }
                }
            }
        } else {
            String mpackageName = manager.getRunningAppProcesses().get(0).processName;
            UsageStatsManager usage = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> stats = usage.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, 0, time);
            if (stats != null) {
                SortedMap<Long, UsageStats> runningTask = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : stats) {
                    runningTask.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (runningTask.isEmpty()) {
                    Log.d(TAG, "isEmpty Yes");
                    mpackageName = "";
                } else {
                    mpackageName = runningTask.get(runningTask.lastKey()).getPackageName();
                    Log.d(TAG, "isEmpty No : " + mpackageName);
                }
            }

//      Khóa ứng dụng
        dbUsage = new DBUsage(context, "Usage.sqlite", null, 1);
        Calendar c = Calendar.getInstance();
        int thisyear = c.get(Calendar.YEAR);
        int thismonth = (c.get(Calendar.MONTH)+1);
        int today = c.get(Calendar.DATE);
        List<UsageStats> listStats = Tools.getUsageStats(context);
        if(listStats.size() != 0) {
            sharedPreference = new SharedPreference();
            String dayTemp = "";
            ArrayList<String> locked = sharedPreference.getLocked(context);
            if(today <10) {
                dayTemp = "0" + today + "/0" + thismonth + "/" + thisyear;
            }
            else
                dayTemp = today + "/0" + thismonth + "/" + thisyear;

            if (!day_old.equals(dayTemp) && !day_old.equals("")) {
                sharedPreference.removeAllLocked(context);
            }
            Cursor data = dbUsage.GetData("SELECT TENPK,(SUM(TIME)) as TIME  FROM USAGE_DAY_US WHERE LASTTIME = '" + dayTemp + "' GROUP BY TENPK ");
            ArrayList<String> getLocked = sharedPreference.getLocked(context);
            boolean check = false;
            try {
                while (data.moveToNext()) {
                    String packedName1 = data.getString(0);
                    long total = data.getLong(1);
                    dbUsage.QueryData("CREATE TABLE IF NOT EXISTS LOCK_TIME (Id INTEGER PRIMARY KEY AUTOINCREMENT, TENPK VARCHAR(200),TIME_LOCK INTEGER)");
                    if (getLocked == null || getLocked.size() == 0) {

                        Cursor data2 = dbUsage.GetData("SELECT * FROM LOCK_TIME WHERE Id >0");
                        try {
                            while (data2.moveToNext()) {
                                String packedName2 = data2.getString(1);
                                long lock_time = data2.getLong(2);
                                if (packedName2.equals(packedName1)) {
                                    if (lock_time < total) {
                                        sharedPreference.addLocked(context, packedName2);
                                        ActivityManager mActivityManager = (ActivityManager)getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
                                        mActivityManager.killBackgroundProcesses(packedName2);
                                        Intent dialogIntent = new Intent(this, Settings.class);
                                        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(dialogIntent);
                                    }
                                }

                            }
                        } finally {
                            data2.close();
                        }

                    } else {
                        for (String s : getLocked) {
                            if (!packedName1.equals(s)) {
                                Cursor data2 = dbUsage.GetData("SELECT * FROM LOCK_TIME WHERE Id >0");
                                try {
                                    while (data2.moveToNext()) {
                                        String packedName2 = data2.getString(1);
                                        long lock_time = data2.getLong(2);
                                        if (packedName2.equals(packedName1) ) {
                                            for (String S : getLocked) {
                                                if (S.equals(packedName2)) {
                                                    check = true;
                                                    break;
                                                }
                                            }
                                            if (!check) {
                                                if (lock_time < total) {
                                                    sharedPreference.addLocked(context, packedName2);
                                                    ActivityManager mActivityManager = (ActivityManager)getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
                                                    mActivityManager.killBackgroundProcesses(packedName2);
                                                    Intent dialogIntent = new Intent(this, Settings.class);
                                                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(dialogIntent);

//                                                    final ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//                                                    am.forceStopPackage(PACKAGE_NAME);

                                                }
                                            }
                                        }
                                    }
                                } finally {
                                    data2.close();
                                }
                            }
                            else
                                break;
                        }
                    }

                }
            } finally {
                data.close();
            }
            day_old = dayTemp;
        }
        //
            for (int i = 0; pakageName != null && i < pakageName.size(); i++) {
                if (mpackageName.equals(pakageName.get(i))) {
                    currentApp = pakageName.get(i);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        timer = null;
        if (imageView != null) {
            windowManager.removeView(imageView);
        }
        /**** added to fix the bug of view not attached to window manager ****/
        try {
            if (dialog != null) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
