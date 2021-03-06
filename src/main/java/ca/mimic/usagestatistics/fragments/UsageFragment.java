package ca.mimic.usagestatistics.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ca.mimic.usagestatistics.activities.SettingsActivity;
import ca.mimic.usagestatistics.adapters.UsageRowAdapter;
import ca.mimic.usagestatistics.R;
import ca.mimic.usagestatistics.utils.Tools;
import ca.mimic.usagestatistics.database.DBUsage;
import ca.mimic.usagestatistics.database.TasksDataSource;
import ca.mimic.usagestatistics.models.TasksModel;
import ca.mimic.usagestatistics.models.UsageDayModel;
import ca.mimic.usagestatistics.models.UsageRowItemModel;


public class UsageFragment extends Fragment {
    static ListView lv;
    static Context mContext;
    static boolean completeRedraw;
    static TasksDataSource db;
    static DBUsage dbUsage;
    static ListView lvThongKe;
    static List<UsageRowItemModel> arrayListUsage;
    static List<UsageDayModel> arrayListDay;
    static UsageRowAdapter adapter;
    static List<TasksModel> listTasks;
    static UsageFragment mInstance;
    private boolean shouldRefreshOnResume = false;

    public static Fragment newInstance() {

        return new UsageFragment();
    }

    public UsageFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInstance = this;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_day_use_app, container, false);
        mContext = view.getContext();
        lvThongKe = (ListView) view.findViewById(R.id.list_day);
        arrayListUsage = new ArrayList<>();
        arrayListDay = new ArrayList<>();
        listTasks = new ArrayList<>();
        // get data app pinned
        db = TasksDataSource.getInstance(view.getContext());
        db.open();
        try {
            ArrayList<String> pinnedApps = new ArrayList<String>();

            SharedPreferences settingsPrefs = view.getContext().getSharedPreferences(view.getContext().getPackageName(), Context.MODE_MULTI_PROCESS);
            int pinnedSort = Integer.parseInt(settingsPrefs.getString(SettingsActivity.PINNED_SORT_PREFERENCE, Integer.toString(SettingsActivity.PINNED_SORT_DEFAULT)));
            boolean ignorePinned = settingsPrefs.getBoolean(SettingsActivity.IGNORE_PINNED_PREFERENCE, SettingsActivity.IGNORE_PINNED_DEFAULT);

            if (!ignorePinned)
                pinnedApps = new Tools().getPinned(view.getContext());

            listTasks = db.getPinnedTasks(pinnedApps, pinnedSort);
//            for(TasksModel tasksModel:listTasks){
//                if(tasksModel.getSeconds()==0){
//                    listTasks.remove(tasksModel);
//                }
//            }
            for (Iterator<TasksModel> iterator = listTasks.iterator(); iterator.hasNext(); ) {
                int value = iterator.next().getSeconds();
                if (value == 0) {
                    iterator.remove();
                }
            }
        } catch (Exception e) {
            Tools.USLog("createAppTasks exception: " + e);
            listTasks = new ArrayList<TasksModel>();
        }
        db.close();

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        dbUsage = new DBUsage(mContext, "Usage.sqlite", null, 1);
        dbUsage.QueryData("CREATE TABLE IF NOT EXISTS USAGE_DAY_US (Id INTEGER PRIMARY KEY AUTOINCREMENT, TENPK VARCHAR(200),TIME INTEGER,LASTTIME VARCHAR(100))");

        Cursor data = dbUsage.GetData("SELECT * FROM USAGE_DAY_US ");
        try {
            while (data.moveToNext()) {
                String packedName = data.getString(1);
                long total = data.getLong(2);
                String lastime = data.getString(3);
                if(total != 0) {
                    arrayListUsage.add(new UsageRowItemModel(packedName, total, lastime));
                }
            }
        } finally {
            data.close();
        }
        Cursor data1 = dbUsage.GetData("SELECT LASTTIME FROM USAGE_DAY_US GROUP BY LASTTIME ORDER BY LASTTIME DESC");
        try {
            while (data1.moveToNext()) {
                String lastime = data1.getString(0);
                arrayListDay.add(new UsageDayModel(lastime));

            }
        } finally {
            data1.close();
        }
        db.close();
        adapter = new UsageRowAdapter(view.getContext(), arrayListUsage, arrayListDay, listTasks);
        lvThongKe.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
