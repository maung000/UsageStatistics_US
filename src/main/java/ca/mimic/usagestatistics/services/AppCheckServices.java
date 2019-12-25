package ca.mimic.usagestatistics.services;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hanks.passcodeview.CircleView;
import com.hanks.passcodeview.PasscodeView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import ca.mimic.usagestatistics.activities.SettingsActivity;
import ca.mimic.usagestatistics.database.DBUsage;
import ca.mimic.usagestatistics.R;
import ca.mimic.usagestatistics.utils.Tools;
import ca.mimic.usagestatistics.utils.SharedPreference;

import static com.hanks.passcodeview.PasscodeView.PasscodeViewType.TYPE_CHECK_PASSCODE;
import static com.hanks.passcodeview.PasscodeView.PasscodeViewType.TYPE_SET_PASSCODE;


public class AppCheckServices extends Service implements View.OnClickListener {

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
    private static String day_old = "";


    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        sharedPreference = new SharedPreference();
        if (sharedPreference != null) {
            pakageName = sharedPreference.getLocked(context);
        }
        timer = new Timer("AppCheckServices");
        timer.schedule(updateTask, 0, 1000L);


        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        dbUsage = new DBUsage(context, "Usage.sqlite", null, 1);

        imageView = new ImageView(this);
        imageView.setVisibility(View.GONE);
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.CENTER;
        params.x = ((getApplicationContext().getResources().getDisplayMetrics().widthPixels) / 2);
        params.y = ((getApplicationContext().getResources().getDisplayMetrics().heightPixels) / 2);
        windowManager.addView(imageView, params);
    }

    private TimerTask updateTask = new TimerTask() {
        @Override
        public void run() {
            Log.d("<<<<<<<<<<<<","Run service");
            if (sharedPreference != null) {
                pakageName = sharedPreference.getLocked(context);
            }
            if (isConcernedAppIsInForeground()) {
                if (imageView != null) {
                    imageView.post(new Runnable() {
                        public void run() {
                            Log.d("<<<<<<<<<<<<","Run service 1");
                            if (!currentApp.matches(previousApp)) {
                                Log.d("<<<<<<<<<<<<","Run service 2");
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

    public void showUnlockDialog() {
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

    private boolean secondInput;
    private String localPasscode = "";
    private PasscodeView.PasscodeViewListener listener;
    private ViewGroup layout_psd;
    private TextView tv_input_tip;
    private ImageView iv_lock, iv_ok;
    private View cursor;

    private String firstInputTip = "Nhập password gồm 4 chữ số:";
    private String secondInputTip = "Re-enter new passcode";
    private String wrongLengthTip = "Enter a passcode of 4 digits";
    private String wrongInputTip = "Mật khẩu không đúng";
    private String correctInputTip = "Mật khẩu nhập đúng";

    private int passcodeLength = 4;
    private int correctStatusColor = 0xFF61C560; //0xFFFF0000
    private int wrongStatusColor = 0xFFF24055;
    private int normalStatusColor = 0xFFFFFFFF;
    private int numberTextColor = 0xFF747474;
    private int passcodeType = TYPE_CHECK_PASSCODE;

    void showDialog() {
        if (context == null)
            context = getApplicationContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        final View promptsView = layoutInflater.inflate(R.layout.popup_unlock, null);
        layout_psd = (ViewGroup) promptsView.findViewById(R.id.layout_psd);
        tv_input_tip = (TextView) promptsView.findViewById(R.id.tv_input_tip);
        cursor = promptsView.findViewById(R.id.cursor);
        iv_lock = (ImageView) promptsView.findViewById(R.id.iv_lock);
        iv_ok = (ImageView) promptsView.findViewById(R.id.iv_ok);

        tv_input_tip.setText(firstInputTip);

        TextView number0 = (TextView) promptsView.findViewById(R.id.number0);
        TextView number1 = (TextView) promptsView.findViewById(R.id.number1);
        TextView number2 = (TextView) promptsView.findViewById(R.id.number2);
        TextView number3 = (TextView) promptsView.findViewById(R.id.number3);
        TextView number4 = (TextView) promptsView.findViewById(R.id.number4);
        TextView number5 = (TextView) promptsView.findViewById(R.id.number5);
        TextView number6 = (TextView) promptsView.findViewById(R.id.number6);
        TextView number7 = (TextView) promptsView.findViewById(R.id.number7);
        TextView number8 = (TextView) promptsView.findViewById(R.id.number8);
        TextView number9 = (TextView) promptsView.findViewById(R.id.number9);
        ImageView numberOK = (ImageView) promptsView.findViewById(R.id.numberOK);
        ImageView numberB = (ImageView) promptsView.findViewById(R.id.numberB);

        number0.setOnClickListener(this);
        number1.setOnClickListener(this);
        number2.setOnClickListener(this);
        number3.setOnClickListener(this);
        number4.setOnClickListener(this);
        number5.setOnClickListener(this);
        number6.setOnClickListener(this);
        number7.setOnClickListener(this);
        number8.setOnClickListener(this);
        number9.setOnClickListener(this);

        numberB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteChar();
            }
        });
        numberB.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                deleteAllChars();
                return true;
            }
        });
        numberOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                next(promptsView, dialog);
            }
        });

        tintImageView(iv_lock, numberTextColor);
        tintImageView(numberB, numberTextColor);
        tintImageView(numberOK, numberTextColor);
        //tintImageView(iv_ok, correctStatusColor);

        number0.setTag(0);
        number1.setTag(1);
        number2.setTag(2);
        number3.setTag(3);
        number4.setTag(4);
        number5.setTag(5);
        number6.setTag(6);
        number7.setTag(7);
        number8.setTag(8);
        number9.setTag(9);
        number0.setTextColor(numberTextColor);
        number1.setTextColor(numberTextColor);
        number2.setTextColor(numberTextColor);
        number3.setTextColor(numberTextColor);
        number4.setTextColor(numberTextColor);
        number5.setTextColor(numberTextColor);
        number6.setTextColor(numberTextColor);
        number7.setTextColor(numberTextColor);
        number8.setTextColor(numberTextColor);
        number9.setTextColor(numberTextColor);

        dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        } else {
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
        }
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

    private String getPasscodeFromView() {
        StringBuilder sb = new StringBuilder();
        int childCount = layout_psd.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = layout_psd.getChildAt(i);
            int num = (int) child.getTag();
            sb.append(num);
        }
        return sb.toString();
    }

    public void runTipTextAnimation() {
        shakeAnimator(tv_input_tip).start();
    }

    private Animator shakeAnimator(View view) {
        return ObjectAnimator
                .ofFloat(view, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0)
                .setDuration(500);
    }

    private void next(View promptsView, Dialog dialog) {
        String psd = getPasscodeFromView();
        if (passcodeType == TYPE_CHECK_PASSCODE && TextUtils.isEmpty(localPasscode)) {
            SharedPreference sharedPreference;
            sharedPreference = new SharedPreference();
            String passwordLockApp = sharedPreference.getPasswordApp(promptsView.getContext());
            if (psd.equals(passwordLockApp)) {
                runOkAnimation(promptsView, dialog);
            } else {
                runWrongAnimation(promptsView);
            }
        }

        if (psd.length() != passcodeLength) {
            tv_input_tip.setText(wrongLengthTip);
            runTipTextAnimation();
            return;
        }
        if (passcodeType == TYPE_SET_PASSCODE && !secondInput) {
            // second input
            tv_input_tip.setText(secondInputTip);
            localPasscode = psd;
            clearChar();
            secondInput = true;
            return;
        }
    }

    public void runWrongAnimation(final View promptsView) {
        cursor.setTranslationX(0);
        cursor.setVisibility(promptsView.VISIBLE);
        cursor.animate()
                .translationX(layout_psd.getWidth())
                .setDuration(600)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        cursor.setVisibility(promptsView.INVISIBLE);
                        tv_input_tip.setText(wrongInputTip);
                        setPSDViewBackgroundResource(wrongStatusColor);
                        Animator animator = shakeAnimator(layout_psd);
                        animator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                setPSDViewBackgroundResource(normalStatusColor);
                                if (secondInput && listener != null) {
                                    listener.onFail();
                                }
                            }
                        });
                        animator.start();
                    }
                })
                .start();
    }

    private void setPSDViewBackgroundResource(int color) {
        int childCount = layout_psd.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ((CircleView) layout_psd.getChildAt(i)).setColor(color);
        }
    }

    public void runOkAnimation(final View promptsView, final Dialog dialog) {
        cursor.setTranslationX(0);
        cursor.setVisibility(promptsView.VISIBLE);
        cursor.animate()
                .setDuration(600)
                .translationX(layout_psd.getWidth())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        cursor.setVisibility(promptsView.INVISIBLE);
                        setPSDViewBackgroundResource(correctStatusColor);
                        tv_input_tip.setText(correctInputTip);
                        iv_lock.animate().alpha(0).scaleX(0).scaleY(0).setDuration(500).start();
                        iv_ok.animate().alpha(1).scaleX(1).scaleY(1).setDuration(500)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        if (listener != null) {
                                            listener.onSuccess(getPasscodeFromView());
                                        }
                                        dialog.dismiss();
                                    }
                                }).start();
                    }
                })
                .start();

    }

    private void addChar(int number, View view) {
        if (layout_psd.getChildCount() >= passcodeLength) {
            return;
        }
        CircleView psdView = new CircleView(view.getContext());
        int size = dpToPx(8);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(size, 0, size, 0);
        psdView.setLayoutParams(params);
        psdView.setColor(normalStatusColor);
        psdView.setTag(number);
        layout_psd.addView(psdView);
    }

    private int dpToPx(float valueInDp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }

    private void tintImageView(ImageView imageView, int color) {
        if (imageView.getDrawable() != null) {
            imageView.getDrawable().mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        } else {
            imageView.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }

    }

    private void clearChar() {
        layout_psd.removeAllViews();
    }

    private void deleteAllChars() {
        int childCount = layout_psd.getChildCount();
        if (childCount <= 0) {
            return;
        }
        layout_psd.removeAllViews();
    }

    private void deleteChar() {
        int childCount = layout_psd.getChildCount();
        if (childCount <= 0) {
            return;
        }
        layout_psd.removeViewAt(childCount - 1);
    }

    @Override
    public void onClick(View view) {
        int number = (int) view.getTag();
        addChar(number, view);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String NOTIFICATION_CHANNEL_ID = "ca.mimic.usagestatistics";
            String channelName = "My Background Service";
            NotificationChannel chan = null;
            chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            Notification notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("App is running in background")
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(3, notification);
        }
        /* We want this service to continue running until it is explicitly
         * stopped, so return sticky.
         */
        return START_STICKY;
    }
    static Tools.LollipopTaskInfo lollipopTaskInfo;
    static int timeLock = 0;
    public boolean isConcernedAppIsInForeground() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> task = manager.getRunningTasks(5);
        if (task.size() > 0 || task != null) {
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
                if (manager.getRunningAppProcesses() != null) {
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
                    Calendar c = Calendar.getInstance();
                    int thisyear = c.get(Calendar.YEAR);
                    int thismonth = (c.get(Calendar.MONTH) + 1);
                    int today = c.get(Calendar.DATE);
                    List<UsageStats> listStats = Tools.getUsageStats(context);
                    if (listStats.size() != 0) {
                        sharedPreference = new SharedPreference();
                        String dayTemp = "";
                        ArrayList<String> locked = sharedPreference.getLocked(context);
                        if (today < 10) {
                            dayTemp = "0" + today + "/0" + thismonth + "/" + thisyear;
                        } else if (thismonth < 10) {
                            dayTemp = today + "/0" + thismonth + "/" + thisyear;
                        } else {
                            dayTemp = today + "/" + thismonth + "/" + thisyear;
                        }
                        if (!day_old.equals(dayTemp) && !day_old.equals("")) {
                            sharedPreference.removeAllLocked(context);
                        }
                        Cursor data = dbUsage.GetData("SELECT TENPK,(SUM(TIME)) as TIME  FROM USAGE_DAY_US WHERE LASTTIME = '" + dayTemp + "' GROUP BY TENPK ");
                        ArrayList<String> getLocked = sharedPreference.getLocked(context);

                        boolean check = false;
                        try {
                            while (data.moveToNext()) {
                                String packedName1 = data.getString(0);
                                Runnable runnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                                        String mpackageName = manager.getRunningAppProcesses().get(0).processName;
                                        List<UsageStats> listStats = Tools.getUsageStats(context);
                                        if (lollipopTaskInfo != null) {
                                            if(mpackageName.equals(lollipopTaskInfo.packageName)) {
                                                int activityDelta = (int) Math.ceil(lollipopTaskInfo.timeInFGDelta / 1000);
                                                timeLock += activityDelta;
                                            } else {
                                                lollipopTaskInfo = null;
                                            }
                                        } else {
                                            List<UsageStats> listStat = Tools.getUsageStats(context);
                                            if (listStat.size() == 0) {
                                                // Either no permission or nothing new.  Move along
//                                                return false;
                                            }
                                            lollipopTaskInfo = Tools.parseUsageStats(listStats, lollipopTaskInfo);
                                        }
                                    }
                                };
                                long total = data.getLong(1);
                                dbUsage.QueryData("CREATE TABLE IF NOT EXISTS LOCK_TIME (Id INTEGER PRIMARY KEY AUTOINCREMENT, TENPK VARCHAR(200),TIME_LOCK INTEGER)");
                                if (getLocked == null || getLocked.size() == 0) {

                                    Cursor data2 = dbUsage.GetData("SELECT * FROM LOCK_TIME WHERE Id >0");
                                    try {
                                        while (data2.moveToNext()) {
                                            String packedName2 = data2.getString(1);
                                            long lock_time = data2.getLong(2);
                                            if (packedName2.equals(packedName1)) {
                                                if (lock_time < total|lock_time<timeLock) {
                                                    sharedPreference.addLocked(context, packedName2);
                                                    ActivityManager mActivityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
                                                    mActivityManager.killBackgroundProcesses(packedName2);
                                                    Intent dialogIntent = new Intent(this, SettingsActivity.class);
                                                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(dialogIntent);
                                                }
                                            }
                                        }
                                    } finally {
                                        data2.close();
                                    }

                                } else {
                                    Cursor cursorLock = dbUsage.GetData("SELECT TENPK FROM LOCK_TIME");
                                    getLocked = null;
                                    while (cursorLock.moveToNext()) {
                                        getLocked.add(cursorLock.getString(0));
                                    }
                                    for (String s : getLocked) {
                                        if (!packedName1.equals(s)) {
                                            Cursor data2 = dbUsage.GetData("SELECT * FROM LOCK_TIME WHERE Id >0");
                                            try {
                                                while (data2.moveToNext()) {
                                                    String packedName2 = data2.getString(1);
                                                    long lock_time = data2.getLong(2);
                                                    if (packedName2.equals(packedName1)) {
                                                        for (String S : getLocked) {
                                                            if (S.equals(packedName2)) {
                                                                check = true;
                                                                break;
                                                            }
                                                        }
                                                        if (!check) {
                                                            if (lock_time < total|lock_time<timeLock) {
                                                                sharedPreference.addLocked(context, packedName2);
                                                                ActivityManager mActivityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
                                                                mActivityManager.killBackgroundProcesses(packedName2);
                                                                Intent dialogIntent = new Intent(this, SettingsActivity.class);
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
                                        } else
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
