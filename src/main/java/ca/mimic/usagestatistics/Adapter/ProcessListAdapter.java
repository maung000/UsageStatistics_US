package ca.mimic.usagestatistics.Adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ca.mimic.usagestatistics.models.AppsRowItem;
import ca.mimic.usagestatistics.R;
import ca.mimic.usagestatistics.Utils.Helper.IconHelper;

public class ProcessListAdapter extends BaseAdapter {
    final int taskNameColor = 0xFFBBBBBB;
    Context mContext;
    public List<AppsRowItem> mRowItems;
    IconHelper ih;
    boolean completeRedraw = false;

    public ProcessListAdapter(Context context, List<AppsRowItem> rowItems) {
        mContext = context;
        mRowItems = rowItems;
        ih = new IconHelper(context);
    }

    private class ViewHolder {
        ImageView taskIcon;
        ImageView pinIcon;
        TextView taskName;
    }

    public void reDraw(boolean which) {
        completeRedraw = which;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        AppsRowItem rowItem = (AppsRowItem) getItem(position);

        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.add_app_row,
                    parent, false);

            holder = new ViewHolder();
            holder.taskIcon = (ImageView) convertView.findViewById(R.id.task_icon_add_app);
            holder.pinIcon = (ImageView) convertView.findViewById(R.id.pin_icon_add_app);
            holder.taskName = (TextView) convertView.findViewById(R.id.task_name_add_app);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.taskName.setText(rowItem.getName());
        holder.pinIcon.setVisibility(rowItem.getPinned() ? ImageView.VISIBLE : ImageView.INVISIBLE);
        holder.taskName.setTextColor(rowItem.getPinned() ? Color.WHITE : taskNameColor);
        holder.taskName.setPaintFlags(holder.taskName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        if (completeRedraw) {
            holder.taskIcon.setImageBitmap(null);
        }
        if (!ih.cachedIconHelper(holder.taskIcon, rowItem.getComponentName())) {
            mRowItems.remove(rowItem);
        }
        holder.taskIcon.setContentDescription(rowItem.getName());

        return convertView;
    }



    @Override
    public int getCount() {
        return mRowItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mRowItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mRowItems.indexOf(getItem(position));
    }
}


