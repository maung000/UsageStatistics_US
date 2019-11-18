package ca.mimic.usagestatistics.Activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.usage.UsageStats;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Pair;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

//import com.github.omadahealth.lollipin.lib.managers.AppLock;

import ca.mimic.usagestatistics.Adapter.AppsRowAdapter;
import ca.mimic.usagestatistics.Fragment.Usage;
import ca.mimic.usagestatistics.IWatchfulService;
import ca.mimic.usagestatistics.R;
import ca.mimic.usagestatistics.Utils.Tools;
import ca.mimic.usagestatistics.UsPermission;
import ca.mimic.usagestatistics.Utils.Helper.IconHelper;
import ca.mimic.usagestatistics.Utils.SharedPreference;
import ca.mimic.usagestatistics.Database.DBUsage;
import ca.mimic.usagestatistics.Database.TasksDataSource;
import ca.mimic.usagestatistics.Models.AppsRowItem;
import ca.mimic.usagestatistics.Models.TasksModel;
import ca.mimic.usagestatistics.Services.WatchfulService;

public class Settings extends Activity implements ActionBar.TabListener {


    SectionsPagerAdapter mSectionsPagerAdapter;

    ViewPager mViewPager;
    private static GetFragments mGetFragments;
    private static IWatchfulService s;
    private static TasksDataSource db;

    public final static String VERSION_CHECK = "version_check";

    public final static int SETTING_TAB = 1;
    public final static int USAGE_TAB = 2;
    public final static int APPS_TAB = 0;

    public final static String DIVIDER_PREFERENCE = "divider_preference";
    public final static String ROW_DIVIDER_PREFERENCE = "row_divider_preference";
    public final static String APPSNO_PREFERENCE = "appsno_preference";
    public final static String PRIORITY_PREFERENCE = "priority_preference";
    public final static String TOGGLE_PREFERENCE = "toggle_preference";
    public final static String BOOT_PREFERENCE = "boot_preference";
    public final static String WEIGHTED_RECENTS_PREFERENCE = "weighted_recents_preference";
    public final static String WEIGHT_PRIORITY_PREFERENCE = "weight_priority_preference";
    public final static String STATUSBAR_ICON_PREFERENCE = "statusbar_icon_preference";
    public final static String ICON_SIZE_PREFERENCE = "icon_size_preference";
    public final static String ICON_PACK_PREFERENCE = "icon_pack_preference";
    public final static String PASSWORD_FIRST_PREFERENCE = "password";
    public final static String PASSWORD_CHANGE_PREFERENCE = "change_password";
    public final static String SECOND_ROW_PREFERENCE = "second_row_preference";
    public final static String PINNED_SORT_PREFERENCE = "pinned_sort_preference";
    public final static String PINNED_PLACEMENT_PREFERENCE = "pinned_placement_preference";
    public final static String IGNORE_PINNED_PREFERENCE = "ignore_pinned_preference";
    public final static String APPLIST_SORT_PREFERENCE = "applist_sort_preference";
    public final static String SMART_NOTIFICATION_PREFERENCE = "smart_notification_preference";
    public final static String MORE_APPS_PREFERENCE = "more_apps_preference";
    public final static String MORE_APPS_PAGES_PREFERENCE = "more_apps_pages_preference";
    public final static String NOTIFICATION_BG_PREFERENCE = "notification_bg_preference";

    public final static int REQUEST_FIRST_RUN_PIN = 0;


    protected static Settings mInstance;
    protected static AppsRowItem mIconTask;
    protected static boolean isBound = false;
    protected static boolean mLaunchedPaypal = false;
    protected static Display display;

    static AppsRowAdapter mAppRowAdapter;
    protected static boolean completeRedraw;
    protected static ListView lv;


    public static PrefsGet prefs;
    public static Context mContext;
    public static ServiceCall myService;

    public final static String PLAY_STORE_PACKAGENAME = "com.android.vending";

    public final static String MORE_APPS_PACKAGE = "ca.mimic.usagestatistics.MoreApps";
    public final static String MORE_APPS_ACTION = "ca.mimic.usagestatistics.action.MORE_APPS";

    public final static int FLOATING_WINDOWS_INTENT_FLAG = 0x00002000;

    public final static boolean DIVIDER_DEFAULT = false;
    public final static boolean ROW_DIVIDER_DEFAULT = true;
    public final static boolean TOGGLE_DEFAULT = true;
    public final static boolean BOOT_DEFAULT = true;
    public final static boolean WEIGHTED_RECENTS_DEFAULT = true;
    public final static boolean SECOND_ROW_DEFAULT = false;
    public final static boolean IGNORE_PINNED_DEFAULT = false;
    public final static boolean SMART_NOTIFICATION_DEFAULT = true;
    public final static boolean MORE_APPS_DEFAULT = false;

    public final static int WEIGHT_PRIORITY_DEFAULT = 0;
    public final static int APPSNO_DEFAULT = 7;
    public final static int PRIORITY_DEFAULT = 2;
    public final static int PRIORITY_ON_L_DEFAULT = -2;
    public final static int PINNED_SORT_DEFAULT = 0;
    public final static int PINNED_PLACEMENT_DEFAULT = 0;
    public final static int MORE_APPS_PAGES_DEFAULT = 3;

    public final static int TASKLIST_QUEUE_LIMIT = 100;

    public final static String STATUSBAR_ICON_WHITE = "**white**";
    public final static String STATUSBAR_ICON_DEFAULT = STATUSBAR_ICON_WHITE;

    public static final String ACTION_APP_NOTIFICATION_SETTINGS = "android.settings.APP_NOTIFICATION_SETTINGS";
    public final static String EXTRA_APP_UID = "app_uid";
    public final static String EXTRA_APP_PACKAGE = "app_package";


    public final static String NOTIFICATION_BG_DEFAULT_VALUE = "**default**";

    public final static String PINNED_APPS = "pinned_apps";
    public final static String LOCKED_APPS = "locked_apps";
    public final static int PINNED_PLACEMENT_LEFT = 0;

    public final static int ICON_SIZE_DEFAULT = 1;
    public final static int CACHED_ICON_SIZE = 72;
    public final static String ACTION_ADW_PICK_ICON = "org.adw.launcher.icons.ACTION_PICK_ICON";

    public final static int SERVICE_BUILD_TASKS = 0;
    public final static int SERVICE_BUILD_REORDER_LAUNCH = 1;
    public final static int SERVICE_CREATE_NOTIFICATIONS = 2;
    public final static int SERVICE_DESTROY_NOTIFICATIONS = 3;

    public final static int APPLIST_SORT_DEFAULT = 0;

    public final static int START_SERVICE = 0;
    public final static int STOP_SERVICE = 1;

    static boolean mAppsLoaded = false;
    static boolean mIsLollipop;
    static boolean mIsAtLeastLollipop;

    static int displayWidth;
    static DBUsage dbUsage;
    private static String dayStart = "";
    private static String dayEnd = "";
    private static String day_old = "";

    private static int SPLASH_TIME_OUT = 5000;


    static SharedPreference sharedPreference;

    static List<Pair<String, Integer>> time_lock = new ArrayList<>();

    public static List<Pair<String, Integer>> getTime_lock() {
        return time_lock;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Update displayed width bars.
        updateDisplayWidth();

        updateRowItems();

        mAppRowAdapter.reDraw(false);
        updateRowItem();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInstance = this;


        setContentView(R.layout.activity_settings);


        prefs = new PrefsGet(getSharedPreferences(getPackageName(), Context.MODE_MULTI_PROCESS));

        mContext = this;
        mIsLollipop = Tools.isLollipop(true);
        mIsAtLeastLollipop = Tools.isLollipop(false);


        showChangelog(prefs);


        sharedPreference = new SharedPreference();
        String password = sharedPreference.getPassword(mContext);
        boolean checkSetPinCode = sharedPreference.getCheckSetPinCode(mContext);
        if (!checkSetPinCode)
            launchCreatePassword(mContext);

        if (mIsAtLeastLollipop && needsUsPermission()) {
            launchUsPermission(mContext);
        }

        if (Build.VERSION.SDK_INT >= 23) {
            if (!android.provider.Settings.canDrawOverlays(Settings.this)) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1234);
            }
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    startService(new Intent(Settings.this, AppCheckServices.class));
//                }
//            }, SPLASH_TIME_OUT);
        }

//        } else {
//            editor = sharedPreferences.edit();
//            editor.putBoolean(AppLockConstants.IS_PER, true);
//            editor.commit();
//            //startService(new Intent(SplashActivity.this, AppCheckServices.class));
//
//        }
        display = getWindowManager().getDefaultDisplay();
        updateDisplayWidth();

        myService = new ServiceCall(mContext);
        myService.setConnection(mConnection);


        // Set up the action bar.
        final ActionBar actionBar = getActionBar();

        actionBar.setTitle(R.string.title_usage_statistics);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowCustomEnabled(true);


        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(1);

        mGetFragments = new GetFragments();
        mGetFragments.setFm(getFragmentManager());
        mGetFragments.setVp(mViewPager);

        ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        };

        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
        pageChangeListener.onPageSelected(SETTING_TAB);

    }

    @Override
    protected void onResume() {
        super.onResume();
        myService.watchHelper(START_SERVICE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isBound) {
            try {
                unbindService(myService.mConnection);
            } catch (RuntimeException e) {
                Tools.USLog("Could not unbind service!");
            }
            isBound = false;
        }
//        // Khóa ứng dụng
//        dbUsage = new DBUsage(mContext, "Usage.sqlite", null, 1);
//        Calendar c = Calendar.getInstance();
//        int thisyear = c.get(Calendar.YEAR);
//        int thismonth = (c.get(Calendar.MONTH)+1);
//        int today = c.get(Calendar.DATE);
//        List<UsageStats> listStats = Tools.getUsageStats(mContext);
//        if(listStats.size() != 0) {
//            sharedPreference = new SharedPreference();
//            String dayTemp = "";
//            ArrayList<String> locked = sharedPreference.getLocked(mContext);
//            if(today <10) {
//                dayTemp = "0" + today + "/0" + thismonth + "/" + thisyear;
//            }
//            else
//                dayTemp = today + "/0" + thismonth + "/" + thisyear;
//
//            if (!day_old.equals(dayTemp) && !day_old.equals("")) {
//                sharedPreference.removeAllLocked(mContext);
//            }
//            Cursor data = dbUsage.GetData("SELECT TENPK,(SUM(TIME)) as TIME  FROM USAGE_DAY_US WHERE LASTTIME = '" + dayTemp + "' GROUP BY TENPK ");
//
//            try {
//                while (data.moveToNext()) {
//                    String packedName1 = data.getString(0);
//                    long total = data.getLong(1);
//                    dbUsage.QueryData("CREATE TABLE IF NOT EXISTS LOCK_TIME (Id INTEGER PRIMARY KEY AUTOINCREMENT, TENPK VARCHAR(200),TIME_LOCK INTEGER)");
//
//                    Cursor data2 = dbUsage.GetData("SELECT * FROM LOCK_TIME WHERE Id >0");
//                    try {
//                        while (data2.moveToNext()) {
//                            String packedName2 = data2.getString(1);
//                            long lock_time = data2.getLong(2);
//                            ActivityManager am = (ActivityManager) this.getBaseContext().getSystemService(Activity.ACTIVITY_SERVICE);
//
//                            if (packedName2.equals(packedName1)) {
//                                if (lock_time < total) {
//                                    sharedPreference.addLocked(mContext, packedName2);
//                                    am.killBackgroundProcesses(packedName2);
//                                }
//                            }
//                        }
//                    } finally {
//                        data2.close();
//                    }
//
//                }
//            } finally {
//                data.close();
//            }
//            day_old = dayTemp;
//        }
//        //
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isBound = false;
    }


    @TargetApi(17)
    protected void updateDisplayWidth() {
        Point size = new Point();
        try {
            display.getRealSize(size);
            displayWidth = size.x;
        } catch (NoSuchMethodError e) {
            displayWidth = display.getWidth();
        }
    }


    @TargetApi(21)
    static protected void launchUsPermission(Context context) {
        UsPermission usPermission = new UsPermission(context);
        View mUsPermission = usPermission.getView();
        mUsPermission.refreshDrawableState();
        new AlertDialog.Builder(context)
                .setTitle(R.string.us_permission_title)
                .setIcon(R.drawable.ic_launcher)
                .setView(mUsPermission)
                .setPositiveButton(R.string.us_permission_settings_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mContext.startActivity(new Intent(
                                        android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS));
                            }
                        })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mContext.startActivity(new Intent(
                                android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS));
                    }
                })
                .show();
    }

    protected void launchInstructions() {
        startActivity(new Intent(mContext, Instructions.class));
    }


    protected void launchCreatePassword(Context context) {

        UsCreatePassword usCreatePassword = new UsCreatePassword(context);
        View mUsCreatePassword = usCreatePassword.getView();
        mUsCreatePassword.refreshDrawableState();
        new AlertDialog.Builder(context)
                .setTitle(R.string.us_permission_title)
                .setIcon(R.drawable.ic_launcher)
                .setView(mUsCreatePassword)
                .setPositiveButton(R.string.create_password_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
//                                if (!LockManager.getInstance().getAppLock().isPasscodeSet()) {
//                                Intent intent = new Intent(mContext, PasswordSelectLock.class);
//                                intent.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK);
//                                startActivityForResult(intent, REQUEST_FIRST_RUN_PIN);
//                                }
                                startActivity(new Intent(mContext, PasswordSelectLock.class));
                                finish();
                            }
                        })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
//                        Intent intent = new Intent(mContext, PasswordSelectLock.class);
//                        intent.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK);
//                        startActivityForResult(intent, REQUEST_FIRST_RUN_PIN);
//                        startActivity(new Intent(mContext, PasswordSetActivity.class));
                        finish();
                    }
                })
                .show();

    }

    protected boolean showChangelog(PrefsGet prefs) {
        SharedPreferences mPrefs = prefs.prefsGet();
        SharedPreferences.Editor mEditor = prefs.editorGet();

        String USVersion = null;
        try {
            USVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            String[] versionArray = USVersion.split("\\."); // Only get major.minor
            USVersion = versionArray[0] + "." + versionArray[1];
        } catch (PackageManager.NameNotFoundException e) {
        }

        String whichVersion = mPrefs.getString(VERSION_CHECK, null);

        Tools.USLog("savedVer: " + whichVersion + " hangarVersion: " + USVersion);
        if (whichVersion != null) {
            if (whichVersion.equals(USVersion)) {
                return false;
            } else {
                mEditor.putString(VERSION_CHECK, USVersion);
                mEditor.commit();
                return true;
            }
        } else {
            mEditor.putString(VERSION_CHECK, USVersion);
            mEditor.commit();

            launchInstructions();
            return false;
        }
    }

    ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder binder) {
            s = IWatchfulService.Stub.asInterface(binder);
            isBound = true;
            myService.execute(SERVICE_BUILD_TASKS);
        }

        public void onServiceDisconnected(ComponentName className) {
            s = null;
            isBound = false;
        }
    };

    protected boolean needsUsPermission() {
        List<UsageStats> listStats = Tools.getUsageStats(mContext);
        return (listStats.size() == 0);
    }


    protected static class ServiceCall {
        Context mContext;
        ServiceConnection mConnection;

        ServiceCall(Context context) {
            mContext = context;
        }

        protected void setConnection(ServiceConnection connection) {
            mConnection = connection;
        }

        protected void watchHelper(int which) {
            Intent intent = new Intent(mContext, WatchfulService.class);
            switch (which) {
                case 0:
                    mContext.startService(intent);
                    if (!isBound) {
                        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT);
                    }
                    break;
                case 1:
                    mContext.stopService(intent);
                    if (isBound) {
                        mContext.unbindService(mConnection);
                        isBound = false;
                    }
                    break;
            }
        }

        protected void execute(int which) {
            try {
                switch (which) {
                    case SERVICE_BUILD_TASKS:
                        s.buildTasks();
                        return;
                    case SERVICE_BUILD_REORDER_LAUNCH:
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    s.buildReorderAndLaunch();
                                } catch (Exception e) {
                                    Tools.USLog("buildReorderAndLaunch exception: " + e);
                                    e.printStackTrace();
                                }
                            }
                        };
                        new Thread(runnable).start();
                        return;
                    case SERVICE_CREATE_NOTIFICATIONS:
                        Runnable runnable2 = new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    s.createNotification();
                                } catch (RemoteException e) {
                                    Tools.USLog("buildReorderAndLaunch exception: " + e);
                                }
                            }
                        };
                        new Thread(runnable2).start();
                        return;
                    case SERVICE_DESTROY_NOTIFICATIONS:
                        s.destroyNotification();
                        break;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }


    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    public static int[] splitToComponentTimes(int longVal) {
        int hours = longVal / 3600;
        int remainder = longVal - hours * 3600;
        int mins = remainder / 60;
        remainder = remainder - mins * 60;
        int secs = remainder;

        int[] ints = {hours, mins, secs};
        return ints;
    }


    public static class PrefsGet {
        SharedPreferences realPrefs;

        public PrefsGet(SharedPreferences prefs) {
            realPrefs = prefs;
        }

        public SharedPreferences prefsGet() {
            return realPrefs;
        }

        public SharedPreferences.Editor editorGet() {
            return realPrefs.edit();
        }
    }

    public static class GetFragments {
        static ViewPager vp;
        static FragmentManager fm;

        public Fragment getFragmentByPosition(int pos) {
            String tag = "android:switcher:" + vp.getId() + ":" + pos;
            return fm.findFragmentByTag(tag);
        }

        public void setVp(ViewPager mViewPager) {
            vp = mViewPager;
        }

        public void setFm(FragmentManager mFm) {
            fm = mFm;
        }
    }

    public static class PrefsFragment extends PreferenceFragment {
        CheckBoxPreference boot_preference;
        Preference app_pack_preference;
        Preference password_firts;
        Preference password_change;


        public static PrefsFragment newInstance(int prefLayout) {
            PrefsFragment fragment = new PrefsFragment();
            Bundle args = new Bundle();
            args.putInt("layout", prefLayout);
            fragment.setArguments(args);
            return fragment;
        }

        public PrefsFragment() {
        }


        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            final int prefLayout = getArguments().getInt("layout");
            setHasOptionsMenu(true);
            addPreferencesFromResource(prefLayout);
            final SharedPreferences prefs2 = prefs.prefsGet();

            try {
                // *** SETTING ***

                boot_preference = (CheckBoxPreference) findPreference(BOOT_PREFERENCE);
                boot_preference.setChecked(prefs2.getBoolean(BOOT_PREFERENCE, BOOT_DEFAULT));
                boot_preference.setOnPreferenceChangeListener(changeListener);


                String appPackName = Tools.getApplicationName(mContext, prefs2.getString(ICON_PACK_PREFERENCE, null));
                app_pack_preference = findPreference(ICON_PACK_PREFERENCE);
                if (appPackName.isEmpty() || appPackName.equals("")) {
                    appPackName = getResources().getString(R.string.title_add_app_follow);
                }

                app_pack_preference.setSummary(appPackName);
                updateIconPackIcon(mContext);
                app_pack_preference.setOnPreferenceClickListener(
                        new Preference.OnPreferenceClickListener() {
                            @Override
                            public boolean onPreferenceClick(Preference preference) {
                                Intent intent = new Intent(mContext, AddApp.class);
                                startActivity(intent);
                                return false;
                            }
                        }
                );


                sharedPreference = new SharedPreference();
                String password = sharedPreference.getPassword(mContext);
                boolean checkSetPinCode = sharedPreference.getCheckSetPinCode(mContext);

                password_change = findPreference(PASSWORD_CHANGE_PREFERENCE);
//                if (!password.equals("")) {
                if(sharedPreference.getCheckSetPinCode(mContext)){
                    password_change.setOnPreferenceClickListener(
                            new Preference.OnPreferenceClickListener() {
                                @Override
                                public boolean onPreferenceClick(Preference preference) {
                                    Intent intent = new Intent(mContext, PasswordOldActivity.class);
                                    startActivity(intent);
                                    return false;
                                }
                            }
                    );
                } else
                    password_change.setEnabled(false);
            } catch (NullPointerException e) {
            }
        }

        Preference.OnPreferenceChangeListener changeListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference, Object newValue) {
                Tools.USLog("onPreferenceChange pref.getKey=[" + preference.getKey() + "] newValue=[" + newValue + "]");

                final SharedPreferences prefs2 = prefs.prefsGet();
                final SharedPreferences.Editor editor = prefs.editorGet();

                if (preference.getKey().equals(BOOT_PREFERENCE)) {
                    editor.putBoolean(BOOT_PREFERENCE, (Boolean) newValue);
                    editor.commit();
                    return true;
                } else if (preference.getKey().equals(PINNED_SORT_PREFERENCE)) {
                    editor.putString(PINNED_SORT_PREFERENCE, (String) newValue);
                    editor.commit();
                    String pinnedApps = prefs2.getString(PINNED_APPS, null);
                    if (pinnedApps != null && !pinnedApps.isEmpty()) {
                        myService.execute(SERVICE_BUILD_REORDER_LAUNCH);
                    }
                    return true;
                }
                myService.execute(SERVICE_BUILD_REORDER_LAUNCH);
                return true;
            }
        };
    }

    // Create Icon
    static void updateIconPackIcon(Context context) {
        String iconPackPackage = prefs.prefsGet().getString(ICON_PACK_PREFERENCE, null);
        Drawable icon;

        icon = context.getResources().getDrawable(R.drawable.plus_icon);
        try {
            Tools.USLog("iconPackPackage: " + iconPackPackage);
            icon = new BitmapDrawable(context.getResources(), new IconHelper(mContext).cachedResourceIconHelper(iconPackPackage));
        } catch (Exception e) {
        }
        PrefsFragment mGeneralSettings = (PrefsFragment) mGetFragments.getFragmentByPosition(SETTING_TAB);
        mGeneralSettings.app_pack_preference.setIcon(icon);
    }


    private static void launchNotificationSettings() {
        final Intent intent = new Intent(ACTION_APP_NOTIFICATION_SETTINGS);
        final String packageName = mContext.getPackageName();
        final int appUid = Tools.getUid(mContext, packageName);

        intent.putExtra(EXTRA_APP_PACKAGE, packageName);
        intent.putExtra(EXTRA_APP_UID, appUid);
        mInstance.startActivity(intent);
    }

    public static void updateRowItem() {
        int start = lv.getFirstVisiblePosition();
        for (int i = start, j = lv.getLastVisiblePosition(); i <= j; i++) {
            View view = lv.getChildAt(i - start);
            mAppRowAdapter.getView(i, view, lv);
        }
        completeRedraw = false;
        mAppRowAdapter.reDraw(false);
    }

    public synchronized static void updateListView(final boolean setAdapter) {
        Runnable runnable = new Runnable() {
            public void run() {
                if (!mAppsLoaded) {
                    RelativeLayout bg = (RelativeLayout) lv.getParent();
                    bg.findViewById(R.id.loading_text).setVisibility(View.GONE);

                    mAppsLoaded = true;
                }
                if (setAdapter) {
                    lv.setAdapter(mAppRowAdapter);
                }
                lv.invalidateViews();
                mAppRowAdapter.notifyDataSetChanged();
            }
        };
        mInstance.runOnUiThread(runnable);
    }


    public static class AppsFragment extends Fragment implements OnItemClickListener {
        SharedPreference sharedPreference;

        public static Fragment newInstance() {
            return new AppsFragment();
        }

        public AppsFragment() {
        }


        public void onResume() {
            super.onResume();

            if (mAppRowAdapter == null)
                return;

            List<AppsRowItem> mRowItems = createAppTasks();
            if (mAppRowAdapter.mRowItems.size() != mRowItems.size()) {
                mAppRowAdapter.mRowItems = createAppTasks();
                updateListView(true);
            }
            mAppRowAdapter.notifyDataSetChanged();

            mAppsLoaded = false;
            mAppRowAdapter.reDraw(completeRedraw);
            lv.invalidateViews();
        }


        @Override
        public void onStart() {
            super.onStart();

        }


        public void buildList() {
            Runnable runnable = new Runnable() {
                public void run() {
                    if (mAppRowAdapter == null)
                        return;
                    mAppRowAdapter.mRowItems = createAppTasks();
                    updateListView(true);
                }
            };
            new Thread(runnable).start();
        }

        Spinner.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                final SharedPreferences.Editor editor = prefs.editorGet();

                switch (adapterView.getId()) {
                    case R.id.sort_spinner:
                        editor.putInt(APPLIST_SORT_PREFERENCE, i);
                        break;
                }
                editor.commit();

                buildList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        };

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            setHasOptionsMenu(true);

            mAppsLoaded = false;
            View appsView = inflater.inflate(R.layout.apps_settings, container, false);
            lv = (ListView) appsView.findViewById(R.id.list);

            final SharedPreferences prefs2 = prefs.prefsGet();

            Spinner sortSpin = (Spinner) appsView.findViewById(R.id.sort_spinner);
            ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(mContext,
                    R.array.entries_sort_spinner, R.layout.spinner_item);
            sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            sortSpin.setAdapter(sortAdapter);
            sortSpin.setOnItemSelectedListener(spinnerListener);
            sortSpin.setSelection(prefs2.getInt(APPLIST_SORT_PREFERENCE, APPLIST_SORT_DEFAULT));

            ImageView refreshBtn = (ImageView) appsView.findViewById(R.id.refresh);
            refreshBtn.setClickable(true);
            final Animation rotation = AnimationUtils.loadAnimation(getActivity(), R.anim.refresh);
            rotation.setRepeatCount(1);
            rotation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    buildList();
                }
            });
            refreshBtn.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View view, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        view.startAnimation(rotation);
                    }
                    return false;
                }
            });

            return appsView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Tools.USLog("onActivityCreated appsFragment");

            lv.setOnItemClickListener(this);

            Runnable runnable = new Runnable() {
                public void run() {
                    List<AppsRowItem> appTasks = createAppTasks();
                    if (appTasks == null)
                        return;

                    mAppRowAdapter = new AppsRowAdapter(mContext, appTasks);

                    updateListView(true);
                }
            };
            new Thread(runnable).start();
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final AppsRowItem rowItem = (AppsRowItem) parent.getItemAtPosition(position);

            sharedPreference = new SharedPreference();

            PopupMenu popup = new PopupMenu(mContext, view);
            popup.getMenuInflater().inflate(R.menu.app_action, popup.getMenu());
            final MenuItem lockItem = popup.getMenu().getItem(2);

//            final PasswordSelectLock passwordSelectLock = new PasswordSelectLock();

            if (rowItem.getLocked()) lockItem.setTitle(R.string.action_unlock);
            PopupMenu.OnMenuItemClickListener menuAction = new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    db = TasksDataSource.getInstance(mContext);
                    db.open();
                    switch (item.getItemId()) {
                        case R.id.lock:
                            Boolean isLock = rowItem.getLocked();

                            if (lockItem.getTitle().equals("Khóa")) {
                                DialogLockTime(rowItem.getPackageName());
                                rowItem.setLocked(!isLock);
                                new Tools().toggleLock(mContext, rowItem.getPackageName(), prefs.editorGet());
                            } else {

//                                Intent intent = new Intent(mContext, PasswordSelectLock.class);
//                                intent.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK);
////                                startActivityForResult(intent, REQUEST_CODE_ENABLE);
//                                startActivity(intent);

//                                if(passwordSelectLock.isCheck()) {
//                                    rowItem.setLocked(!isLock);
//                                    new Tools().toggleLock(mContext, rowItem.getPackageName(), prefs.editorGet());
//                                    sharedPreference.removeLocked(mContext, rowItem.getPackageName());
//                                    dbUsage = new DBUsage(mContext, "Usage.sqlite", null, 1);
//                                    dbUsage.QueryData("DELETE FROM LOCK_TIME WHERE TENPK = '" + rowItem.getPackageName() + "'");
//                                    dbUsage.close();
//                                }
                            }
//                            passwordSelectLock.setCheck(false);
                            break;
                        case R.id.statitic:
                            String packedName = rowItem.getPackageName();
                            DialogCalculate(packedName);
                            break;
                        case R.id.action_reset_stats:
                            rowItem.setStats(null);
                            rowItem.setBarContWidth(0);
                            db.resetTaskStats(rowItem);
                            dbUsage = new DBUsage(mContext, "Usage.sqlite", null, 1);
                            dbUsage.QueryData("DELETE FROM USAGE_DAY_US WHERE TENPK = '" + rowItem.getPackageName() + "'");

                            dbUsage.close();
                            db.close();
                            break;
                    }
                    lv.invalidateViews();
                    myService.execute(SERVICE_BUILD_REORDER_LAUNCH);
                    return true;
                }
            };
            popup.setOnMenuItemClickListener(menuAction);
            popup.show();
        }

    }

    public static void DialogLockTime(final String packedName) {
        Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_time_lock);


        final EditText edt_hour_LockTime = (EditText) dialog.findViewById(R.id.lock_hour_time);
        edt_hour_LockTime.setCursorVisible(true);
        final EditText edt_min_LockTime = (EditText) dialog.findViewById(R.id.lock_min_time);
        edt_min_LockTime.setCursorVisible(true);
        final Button btn_OK = (Button) dialog.findViewById(R.id.btn_OK);

        btn_OK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String hour = edt_hour_LockTime.getText().toString();
                String min = edt_min_LockTime.getText().toString();
                if (!hour.equals("") && !min.equals("")) {

//                    String[] units = time.split(":");
//                    int hour = Integer.parseInt(units[0]);
//                    int minutes = Integer.parseInt(units[1]);
                    int hour_n = Integer.parseInt(hour);
                    int minutes = Integer.parseInt(min);
                    int seconds = 60 * minutes + hour_n * 3600;

                    dbUsage = new DBUsage(mContext, "Usage.sqlite", null, 1);

                    dbUsage.QueryData("INSERT INTO LOCK_TIME VALUES(null,'" + packedName + "','" + seconds + "')");

                    dbUsage.close();
                    Toast.makeText(mContext, "Thêm thời gian khóa ứng dụng thành công", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.show();

    }

    public static void DialogCalculate(final String packedName) {
        Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_statitics);


        final EditText edtdayStart = (EditText) dialog.findViewById(R.id.dayStart);
        final EditText edtdayEnd = (EditText) dialog.findViewById(R.id.dayEnd);
        final EditText edtCalculate = (EditText) dialog.findViewById(R.id.sum_time);
        final Button buttonUsage = (Button) dialog.findViewById(R.id.btn_statitic);

        edtdayStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Select_day(edtdayStart);


            }
        });
        edtdayEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Select_day(edtdayEnd);
            }
        });
        buttonUsage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dayStart = Get_day(edtdayStart);
                dayEnd = Get_day(edtdayEnd);
                dbUsage = new DBUsage(mContext, "Usage.sqlite", null, 1);
                Cursor data = dbUsage.GetData("SELECT SUM(TIME) FROM USAGE_DAY_US WHERE TENPK ='" + packedName + "' AND LASTTIME >= '" + dayStart + "' AND LASTTIME <= '" + dayEnd + "'");
                try {
                    while (data.moveToNext()) {
                        int sum_time = data.getInt(0);
                        int[] statsTime = splitToComponentTimes(sum_time);
                        String statsString = ((statsTime[0] > 0) ? statsTime[0] + "h " : "") + ((statsTime[1] > 0) ? statsTime[1] + "m " : "") + ((statsTime[2] > 0) ? statsTime[2] + "s " : "");
                        edtCalculate.setText(statsString);

                    }
                } finally {
                    data.close();
                }
            }
        });
        dialog.show();

    }

    public static String Get_day(EditText editText) {

        return editText.getText().toString();
    }

    public static void Select_day(final EditText editText) {
        final Calendar calendar = Calendar.getInstance();
        int ngay = calendar.get(Calendar.DATE);
        int thang = calendar.get(Calendar.MONTH);
        int nam = calendar.get(Calendar.YEAR);
        DatePickerDialog datePickerDialog = new DatePickerDialog(mContext, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(year, month, dayOfMonth);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
                editText.setText(simpleDateFormat.format(calendar.getTime()));
            }
        }, nam, thang, ngay);
        datePickerDialog.show();

    }


    public static List<AppsRowItem> createAppTasks() {
        db = TasksDataSource.getInstance(mContext);
        db.open();
        int highestSeconds;
        List<TasksModel> tasks;

        try {
            highestSeconds = db.getHighestSeconds();

            //
            ArrayList<String> pinnedApps = new ArrayList<String>();

            SharedPreferences settingsPrefs = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_MULTI_PROCESS);
            int pinnedSort = Integer.parseInt(settingsPrefs.getString(Settings.PINNED_SORT_PREFERENCE, Integer.toString(Settings.PINNED_SORT_DEFAULT)));
            boolean ignorePinned = settingsPrefs.getBoolean(Settings.IGNORE_PINNED_PREFERENCE, Settings.IGNORE_PINNED_DEFAULT);

            if (!ignorePinned)
                pinnedApps = new Tools().getPinned(mContext);

            tasks = db.getPinnedTasks(pinnedApps, pinnedSort);
            //
        } catch (Exception e) {
            Tools.USLog("createAppTasks exception: " + e);
            return new ArrayList<AppsRowItem>();
        }

        List<AppsRowItem> appTasks = new ArrayList<AppsRowItem>();

        for (TasksModel task : tasks) {
            try {
                try {
                    ComponentName.unflattenFromString(task.getPackageName() + "/" + task.getClassName());
                } catch (Exception e) {
                    Tools.USLog("Could not find Application info for [" + task.getName() + "]");
                    db.deleteTask(task);
                    continue;
                }
                if (new Tools().cachedImageResolveInfo(mContext, task.getPackageName()) != null)
                    appTasks.add(createAppRowItem(task, highestSeconds));
            } catch (Exception e) {
                Tools.USLog("could not add taskList item " + e);
            }

            SharedPreferences prefs2 = prefs.prefsGet();
            Collections.sort(appTasks, new Tools.AppRowComparator(prefs2.getInt(APPLIST_SORT_PREFERENCE, APPLIST_SORT_DEFAULT)));

        }
        db.close();
        return appTasks;
    }

    public void updateRowItems() {
        List<AppsRowItem> appList = mAppRowAdapter.mRowItems;
        List<AppsRowItem> newAppList = new ArrayList<AppsRowItem>();

        db = TasksDataSource.getInstance(mContext);
        db.open();
        int highestSeconds = db.getHighestSeconds();
        db.close();

        for (AppsRowItem item : appList) {
            AppsRowItem newItem = createAppRowItem(item, highestSeconds);
            newAppList.add(newItem);
        }

        mAppRowAdapter.mRowItems = newAppList;
        updateListView(false);
    }

    public static AppsRowItem createAppRowItem(TasksModel task, int highestSeconds) {
        AppsRowItem appTask = new AppsRowItem(task);
        float secondsRatio = (float) task.getSeconds() / highestSeconds;

        int barColor;
        int secondsColor = (Math.round(secondsRatio * 100));
        if (secondsColor >= 80) {
            barColor = 0xFF34B5E2;
        } else if (secondsColor >= 60) {
            barColor = 0xFFAA66CC;
        } else if (secondsColor >= 40) {
            barColor = 0xFF74C353;
        } else if (secondsColor >= 20) {
            barColor = 0xFFFFBB33;
        } else {
            barColor = 0xFFFF4444;
        }
        int[] statsTime = splitToComponentTimes(task.getSeconds());
        String statsString = ((statsTime[0] > 0) ? statsTime[0] + "h " : "") + ((statsTime[1] > 0) ? statsTime[1] + "m " : "") + ((statsTime[2] > 0) ? statsTime[2] + "s " : "");

        int maxWidth = displayWidth - Tools.dpToPx(mContext, 46 + 14 + 90);
        float adjustedWidth = maxWidth * secondsRatio;

        ComponentName componentTask = ComponentName.unflattenFromString(task.getPackageName() + "/" + task.getClassName());
        appTask.setComponentName(componentTask);
        appTask.setPinned(new Tools().isPinned(mContext, task.getPackageName()));
        appTask.setLocked(new Tools().isLocked(mContext, task.getPackageName()));
        appTask.setStats(statsString);
        appTask.setBarColor(barColor);
        appTask.setBarContWidth(Math.round(adjustedWidth));

        return appTask;
    }


    public static class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(final int position) {
            switch (position) {
                case SETTING_TAB:
                    return PrefsFragment.newInstance(R.layout.setting);
                case USAGE_TAB:
                    return Usage.newInstance();
                case APPS_TAB: {
                    return AppsFragment.newInstance();
                }
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case SETTING_TAB:
                    return mContext.getString(R.string.title_setting).toUpperCase(l);
                case USAGE_TAB:
                    return mContext.getString(R.string.title_statitics_usage).toUpperCase(l);
                case APPS_TAB:
                    return mContext.getString(R.string.title_apps).toUpperCase(l);
            }
            return null;
        }
    }
}
