package ca.mimic.usagestatistics;

import android.app.Fragment;
import android.app.usage.UsageStats;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


public class Usage extends Fragment {
    static ListView lv;
    static Context mContext;
    static boolean completeRedraw;
    static TasksDataSource db;
    static DBUsage dbUsage;
    static ListView lvThongKe;
    static List<UsageRowItem> arrayListUsage;
    static List<UsageDay> arrayListDay;
    static UsageRowAdapter adapter;
    static List<TasksModel> listTasks;


    public static Fragment newInstance() {

        return new Usage();
    }

    public Usage() {
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_day_use_app, container, false);
        lvThongKe = (ListView) view.findViewById(R.id.list_day);
        arrayListUsage = new ArrayList<>();
        arrayListDay = new ArrayList<>();
        listTasks = new ArrayList<>();
        // get data app pinned
        db = TasksDataSource.getInstance(view.getContext());
        db.open();
        ArrayList<String> pinnedApps = new ArrayList<String>();

        SharedPreferences settingsPrefs = view.getContext().getSharedPreferences(view.getContext().getPackageName(), Context.MODE_MULTI_PROCESS);
        int pinnedSort = Integer.parseInt(settingsPrefs.getString(Settings.PINNED_SORT_PREFERENCE, Integer.toString(Settings.PINNED_SORT_DEFAULT)));
        boolean ignorePinned = settingsPrefs.getBoolean(Settings.IGNORE_PINNED_PREFERENCE, Settings.IGNORE_PINNED_DEFAULT);

        if (!ignorePinned)
            pinnedApps = new Tools().getPinned(view.getContext());

        listTasks = db.getPinnedTasks(pinnedApps, pinnedSort);

        //
        adapter = new UsageRowAdapter(view.getContext(), arrayListUsage, arrayListDay,listTasks);
        lvThongKe.setAdapter(adapter);

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        dbUsage = new DBUsage(view.getContext(), "Usage.sqlite", null, 1);
        dbUsage.QueryData("CREATE TABLE IF NOT EXISTS USAGE_DAY_US (Id INTEGER PRIMARY KEY AUTOINCREMENT, TENPK VARCHAR(200),TIME INTEGER,LASTTIME VARCHAR(100))");


        Cursor data = dbUsage.GetData("SELECT * FROM USAGE_DAY_US ");
        while (data.moveToNext()) {
            String packedName = data.getString(1);
            long total = data.getLong(2);
            String lastime = data.getString(3);
            arrayListUsage.add(new UsageRowItem(packedName, total, lastime));

        }
        Cursor data1 = dbUsage.GetData("SELECT LASTTIME FROM USAGE_DAY_US GROUP BY LASTTIME ORDER BY LASTTIME DESC");
        while (data1.moveToNext()) {
            String lastime = data1.getString(0);
            arrayListDay.add(new UsageDay(lastime));

        }
        adapter.notifyDataSetChanged();
        return view;
    }


}
