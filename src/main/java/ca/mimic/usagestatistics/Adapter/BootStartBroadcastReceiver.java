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

package ca.mimic.usagestatistics.Adapter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import ca.mimic.usagestatistics.Activity.Settings;
import ca.mimic.usagestatistics.Utils.Tools;
import ca.mimic.usagestatistics.Services.WatchfulService;

public class BootStartBroadcastReceiver extends BroadcastReceiver {
    SharedPreferences prefs;

    public void onReceive(Context context, Intent arg1) {
        String action = "";
        prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        try {
            action = arg1.getAction();
        } catch (Exception e) {
        }
        if ((action.equals(Tools.BOOT_ACTION) && prefs.getBoolean(Settings.BOOT_PREFERENCE, Settings.BOOT_DEFAULT)) ||
                action.equals(Tools.REPLACE_ACTION) || action.equals(Tools.REFRESH_ACTION)) {
            Intent intent = new Intent(context, WatchfulService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        } else {
            Tools.USLog("Start on boot [" + prefs.getBoolean(Settings.BOOT_PREFERENCE, Settings.BOOT_DEFAULT) + "] or Notification disabled [" + prefs.getBoolean(Settings.TOGGLE_PREFERENCE, Settings.TOGGLE_DEFAULT) + "]");
        }
    }
}