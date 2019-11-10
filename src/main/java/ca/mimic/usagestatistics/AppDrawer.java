/*
 * Copyright © 2014 Jeff Corcoran
 *
 * This file is part of Hangar.
 *
 * Hangar is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Hangar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Hangar.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package ca.mimic.usagestatistics;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.widget.RemoteViews;

import java.util.Random;

import ca.mimic.usagestatistics.Activity.Settings;
import ca.mimic.usagestatistics.Utils.Helper.ColorHelper;
import ca.mimic.usagestatistics.Utils.Helper.IconHelper;
import ca.mimic.usagestatistics.services.WatchfulService;

public class AppDrawer {
    Context mContext;
    RemoteViews mRowView;
    RemoteViews mLastItem;
    IconHelper ih;

    boolean isColorized;
    boolean roundedCorners;
    boolean isFloating;
    int getColor;

    int mImageButtonLayout;
    int mImageContLayout;
    int mRowId;
    int mSize;
    long mTotalMem;

    int pendingNum;
    String mTaskPackage;

    final int LOW_RAM_THRESHOLD = 1000000;

    AppDrawer(String packageName) {
        mTaskPackage = packageName;
    }

    protected void createRow(int rowLayout, int rowId) {
        mRowId = rowId;

        mRowView = new RemoteViews(mTaskPackage, rowLayout);
        mRowView.removeAllViews(mRowId);

        // Generate random number for pendingIntent
        Random r = new Random();
        pendingNum = r.nextInt(99 - 1 + 1) + 1;
    }

    protected void setImageLayouts(int imageButtonLayout, int imageContLayout) {
        mImageButtonLayout = imageButtonLayout;
        mImageContLayout = imageContLayout;
    }

    public void setContext(Context context) {
        mContext = context;
        ih = new IconHelper(context);
        // mSize = Tools.dpToPx(context, Settings.CACHED_ICON_SIZE);
        mSize = Math.round(mContext.getResources().getDimension(android.R.dimen.notification_large_icon_height) * 0.8f);

        ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        actManager.getMemoryInfo(memInfo);
        mTotalMem = memInfo.totalMem;
        Tools.USLog("MemoryInfo.totalMem: " + mTotalMem);
    }

    public boolean needsScaling() {
        return mContext.getResources().getBoolean(R.bool.notification_needs_scaling) | ((mTotalMem / 1024) <= LOW_RAM_THRESHOLD);
    }


    @SuppressLint("WrongConstant")
    protected boolean newItem(Tools.TaskInfo taskItem, int mLastItemLayout) {
        PackageManager pkgm = mContext.getPackageManager();
        Bitmap cachedIcon;

        mLastItem = new RemoteViews(mTaskPackage, mLastItemLayout);

        if (taskItem.packageName == null) {
            // Dummy invisible item
            return true;
        } else if (taskItem.packageName.equals(Settings.MORE_APPS_PACKAGE)) {
            taskItem.appName = mContext.getResources().getString(R.string.title_more_apps);
            // More Apps icon
            cachedIcon = ih.cachedResourceIconHelper(Settings.MORE_APPS_PACKAGE);
        } else {
            try {
                ComponentName componentTask = ComponentName.unflattenFromString(taskItem.packageName + "/" + taskItem.className);

                cachedIcon = ih.cachedIconHelper(componentTask);
                if (cachedIcon == null)
                    return false;

            } catch (Exception e) {
                Tools.USLog("newItem failed! " + e + " app:" + taskItem.appName);
                return false;
            }
        }


        if (isColorized)
            cachedIcon = ColorHelper.getColoredBitmap(cachedIcon, getColor);

        mLastItem.setImageViewBitmap(mImageButtonLayout, Bitmap.createScaledBitmap(cachedIcon, mSize, mSize, true));

        Intent intent;
        if (taskItem.packageName.equals(Settings.MORE_APPS_PACKAGE)) {
            Tools.USLog("newItem: " + Settings.MORE_APPS_PACKAGE);
            intent = new Intent(new Intent(mContext, WatchfulService.class));
            intent.setAction(Settings.MORE_APPS_ACTION);
            PendingIntent activity = PendingIntent.getService(mContext, pendingNum, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            mLastItem.setOnClickPendingIntent(mImageContLayout, activity);
            mLastItem.setContentDescription(mImageButtonLayout, taskItem.appName);
        } else {
            try {
                intent = pkgm.getLaunchIntentForPackage(taskItem.packageName);
                if (intent == null) {
                    Tools.USLog("Couldn't get intent for [" + taskItem.packageName + "] className:" + taskItem.className);
                    throw new PackageManager.NameNotFoundException();
                }
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setAction(Intent.ACTION_MAIN);
                if (isFloating)
                    intent.addFlags(Settings.FLOATING_WINDOWS_INTENT_FLAG);
                PendingIntent activity = PendingIntent.getActivity(mContext, pendingNum, intent,
                        PendingIntent.FLAG_CANCEL_CURRENT);
                mLastItem.setOnClickPendingIntent(mImageContLayout, activity);
                mLastItem.setContentDescription(mImageButtonLayout, taskItem.appName);
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        }

        return true;
    }
    protected void addItem() {
        mRowView.addView(mRowId, mLastItem);
    }
    protected void setItemVisibility(int visibility) {
        mLastItem.setViewVisibility(mImageContLayout, visibility);
    }
    protected RemoteViews getRow() {
        return mRowView;
    }
}
