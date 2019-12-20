package ca.mimic.usagestatistics.Adapter;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.List;

import ca.mimic.usagestatistics.Utils.Helper.IconHelper;
import ca.mimic.usagestatistics.R;
import ca.mimic.usagestatistics.Database.TasksDataSource;
import ca.mimic.usagestatistics.Models.TasksModel;
import ca.mimic.usagestatistics.Models.UsageDay;
import ca.mimic.usagestatistics.Models.UsageRowItem;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {
    private List<UsageRowItem> listDayUsed;
    private List<UsageDay> mDay;
    private boolean completeRedraw = false;
    private Context mContext;
    private IconHelper ih;
    private TasksDataSource db;
    private List<TasksModel> listTasks;


    RecyclerViewAdapter(Context context, List<UsageRowItem> usageList, List<UsageDay> mDay, List<TasksModel> listTasks) {
        listDayUsed = usageList;
        mContext = context;
        this.mDay = mDay;
        ih = new IconHelper(context);
        this.listTasks = listTasks;
        db = TasksDataSource.getInstance(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        view = mInflater.inflate(R.layout.cardview_item_usage, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {


        if (completeRedraw) {
            holder.usageIcon.setImageBitmap(null);
        }
        db.open();
        for (int i = 0; i < listDayUsed.size(); i++) {
            if (listTasks.get(position).getPackageName().equals(listDayUsed.get(i).getPackedName())) {
                TasksModel task = db.getTask(listDayUsed.get(i).getPackedName());
                try {
                    Drawable d = mContext.getPackageManager().getApplicationIcon(listDayUsed.get(i).getPackedName());
                    holder.usageIcon.setImageDrawable(d);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                holder.usageName.setText(task.getName());

                long[] statsTime = splitToComponentTimes(listDayUsed.get(i).getTimeUsage());

                String statsString = ((statsTime[0] > 0) ? statsTime[0] + "h " : "") + ((statsTime[1] > 0) ? statsTime[1] + "m " : "") + ((statsTime[2] > 0) ? statsTime[2] + "s " : "");
                holder.usageTime.setText(statsString);
                break;
            } else {
                TasksModel task = db.getTask(listTasks.get(position).getPackageName());
                try {
                    Drawable d = mContext.getPackageManager().getApplicationIcon(listTasks.get(position).getPackageName());
                    holder.usageIcon.setImageDrawable(d);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                holder.usageName.setText(task.getName());
                holder.usageTime.setText("0");
            }

        }
        db.close();
    }

    @Override
    public int getItemCount() {
        return listTasks.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView usageName;
        TextView usageTime;
        ImageView usageIcon;
        CardView cardView;

        MyViewHolder(View itemView) {
            super(itemView);

            usageName = (TextView) itemView.findViewById(R.id.usage_name);
            usageTime = (TextView) itemView.findViewById(R.id.usage_time);
            usageIcon = (ImageView) itemView.findViewById(R.id.usage_icon);
            cardView = (CardView) itemView.findViewById(R.id.cardview_id);

            cardView.setCardBackgroundColor(Color.GRAY);
        }
    }

    private static long[] splitToComponentTimes(long longVal) {
        long hours = longVal / 3600;
        long remainder = longVal - hours * 3600;
        long mins = remainder / 60;
        remainder = remainder - mins * 60;
        long secs = remainder;

        return new long[]{hours, mins, secs};
    }
}