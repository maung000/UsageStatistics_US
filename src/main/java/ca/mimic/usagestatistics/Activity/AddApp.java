package ca.mimic.usagestatistics.Activity;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Spinner;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ca.mimic.usagestatistics.Adapter.ProcessListAdapter;
import ca.mimic.usagestatistics.Models.AppsRowItem;
import ca.mimic.usagestatistics.IWatchfulService;
import ca.mimic.usagestatistics.R;
import ca.mimic.usagestatistics.Utils.Tools;
import ca.mimic.usagestatistics.Database.TasksDataSource;
import ca.mimic.usagestatistics.Models.TasksModel;
import ca.mimic.usagestatistics.Services.WatchfulService;

public class AddApp extends Activity implements ActionBar.TabListener{

    SectionsPagerAdapter mSectionsPagerAdapter;

    ViewPager mViewPager;
    private static GetFragments mGetFragments;
    private static IWatchfulService s;
    private static TasksDataSource db;
    private static TasksDataSource db1;

    final static int APPS_TAB = 0;

    final static String ADDAPP_SORT_PREFERENCE = "addapp_sort_preference";

    protected static AddApp mInstance;
    protected static AppsRowItem mIconTask;
    protected static boolean isBound = false;

    static ProcessListAdapter mProcessListAdapter;
    protected static boolean completeRedraw;
    protected static ListView lv;


    static PrefsGet prefs;
    static Context mContext;
    static ServiceCall myService;


    final static int SERVICE_BUILD_TASKS = 0;
    final static int SERVICE_BUILD_REORDER_LAUNCH = 1;
    final static int SERVICE_CREATE_NOTIFICATIONS = 2;
    final static int SERVICE_DESTROY_NOTIFICATIONS = 3;


    final static int APPLIST_TOP_DEFAULT = 2;
    final static int ADDAPP_SORT_DEFAULT = 0;

    final static int START_SERVICE = 0;
    final static int STOP_SERVICE = 1;

    static boolean mAppsLoaded = false;
    static boolean mIsLollipop;
    static boolean mIsAtLeastLollipop;

    static PackageManager packageManager = null;
    static Tools.TaskInfo runningTask;
    static List<ApplicationInfo> list_app= new ArrayList<>();
    static Context context ;
    private SearchView searchView;
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        updateRowItems();

        mProcessListAdapter.reDraw(false);
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


        myService = new ServiceCall(mContext);
        myService.setConnection(mConnection);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        getActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setTitle("    Thêm ứng dụng");

        // get list Installed Applications



                db1 = TasksDataSource.getInstance(mContext);
                db1.open();
                packageManager = getPackageManager();
                List<ApplicationInfo> list_temp = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
                ArrayList<ApplicationInfo> applist = new ArrayList<ApplicationInfo>();
                for (ApplicationInfo info : list_temp) {
                    try {
                        if (null != packageManager.getLaunchIntentForPackage(info.packageName)) {
                            applist.add(info);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                list_app = new ArrayList<>(applist);
                for(int i = 0;i<list_app.size();i++){
                    String temp  = list_app.get(i).packageName;
                    runningTask = new Tools.TaskInfo(temp);
                    String className = null;
                    ResolveInfo resolveInfo;
                    Context context = getApplicationContext();
                    resolveInfo = new Tools().cachedImageResolveInfo(context, temp);
                    className = resolveInfo.activityInfo.name;
                    runningTask.className = className;
                    try {
                        ApplicationInfo appInfo = packageManager.getApplicationInfo(temp, 0);
                        runningTask.appName = appInfo.loadLabel(packageManager).toString();
                        if (!runningTask.appName.isEmpty()) {
                            TasksModel tasksModel = db1.getTask(temp);
                            if(tasksModel == null)
                            {
                                Date date = new Date();
                                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                                db1.createTask(runningTask.appName, runningTask.packageName, runningTask.className, dateFormatter.format(date));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                db1.close();


        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(4);

        mGetFragments = new GetFragments();
        mGetFragments.setFm(getFragmentManager());
        mGetFragments.setVp(mViewPager);

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
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isBound = false;
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

    protected static class ServiceCall  {
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        mContext.startForegroundService(intent);
                    } else {
                        mContext.startService(intent);
                    }
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
                switch(which) {
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent intent = new Intent(this, Settings.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    public static class PrefsGet {
        SharedPreferences realPrefs;
        PrefsGet(SharedPreferences prefs) {
            realPrefs = prefs;
        }
        SharedPreferences prefsGet() {
            return realPrefs;
        }
        SharedPreferences.Editor editorGet() {
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

    public static void updateRowItem() {
        int start = lv.getFirstVisiblePosition();
        for (int i=start, j=lv.getLastVisiblePosition(); i<=j; i++) {
            View view = lv.getChildAt(i - start);
            mProcessListAdapter.getView(i, view, lv);
        }
        completeRedraw = false;
        mProcessListAdapter.reDraw(false);
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
                    lv.setAdapter(mProcessListAdapter);
                }
                lv.invalidateViews();
                mProcessListAdapter.notifyDataSetChanged();
            }
        };
        mInstance.runOnUiThread(runnable);
    }




    public static class AppsFragment extends Fragment implements AdapterView.OnItemClickListener {

        public static Fragment newInstance() {
            return new AppsFragment();
        }

        public AppsFragment() {}

        public void onResume() {
            super.onResume();

            if (mProcessListAdapter == null)
                return;

            mAppsLoaded = false;

            mProcessListAdapter.reDraw(completeRedraw);
            lv.invalidateViews();
        }

        public void buildList() {
            Runnable runnable = new Runnable() {
                public void run() {
                    if (mProcessListAdapter == null)
                        return;
                    mProcessListAdapter.mRowItems = createAppTasks();
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
                    case R.id.sort_spinner_add_app:
                        editor.putInt(ADDAPP_SORT_PREFERENCE, i);
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
            View appsView = inflater.inflate(R.layout.add_app_settings, container, false);
            lv = (ListView) appsView.findViewById(R.id.list_add_app);

            final SharedPreferences prefs2 = prefs.prefsGet();


            Spinner sortSpin = (Spinner) appsView.findViewById(R.id.sort_spinner_add_app);
            ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(mContext,
                    R.array.entries_sort_spinner_add_app, R.layout.spinner_item);
            sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sortSpin.setAdapter(sortAdapter);
            sortSpin.setOnItemSelectedListener(spinnerListener);
            sortSpin.setSelection(prefs2.getInt(ADDAPP_SORT_PREFERENCE, ADDAPP_SORT_DEFAULT));

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

                    mProcessListAdapter = new ProcessListAdapter(mContext, appTasks);
                    updateListView(true);
                }
            };
            new Thread(runnable).start();
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final AppsRowItem rowItem = (AppsRowItem) parent.getItemAtPosition(position);


            Boolean isPinned = rowItem.getPinned();
            rowItem.setPinned(!isPinned);
            new Tools().togglePinned(mContext, rowItem.getPackageName(), prefs.editorGet());

            lv.invalidateViews();
            //myService.execute(SERVICE_BUILD_REORDER_LAUNCH);
        }

    }

    public static List<AppsRowItem> createAppTasks() {
        db = TasksDataSource.getInstance(mContext);
        db.open();
        int highestSeconds;
        List<TasksModel> tasks;
        try {
            highestSeconds = db.getHighestSeconds();
            tasks = db.getAllTasks();
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
            Collections.sort(appTasks, new Tools.AddAppComparator(prefs2.getInt(ADDAPP_SORT_PREFERENCE, ADDAPP_SORT_DEFAULT)));

        }
        db.close();
        return appTasks;
    }

    public void updateRowItems() {
        List<AppsRowItem> appList = mProcessListAdapter.mRowItems;
        List<AppsRowItem> newAppList = new ArrayList<AppsRowItem>();

        db = TasksDataSource.getInstance(mContext);
        db.open();
        int highestSeconds = db.getHighestSeconds();
        db.close();

        for (AppsRowItem item : appList) {
            AppsRowItem newItem = createAppRowItem(item, highestSeconds);
            newAppList.add(newItem);
        }

        mProcessListAdapter.mRowItems = newAppList;
        updateListView(false);
    }

    public static AppsRowItem createAppRowItem(TasksModel task, int highestSeconds){
        AppsRowItem appTask = new AppsRowItem(task);
        float secondsRatio = (float) task.getSeconds() / highestSeconds;

        ComponentName componentTask = ComponentName.unflattenFromString(task.getPackageName() + "/" + task.getClassName());
        appTask.setComponentName(componentTask);
        appTask.setPinned(new Tools().isPinned(mContext, task.getPackageName()));

        return appTask;
    }

    public static class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(final int position) {
            switch (position) {
                case APPS_TAB:
                    return AppsFragment.newInstance();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case APPS_TAB:
                    return mContext.getString(R.string.title_add_apps).toUpperCase(l);
            }
            return null;
        }
    }
}
