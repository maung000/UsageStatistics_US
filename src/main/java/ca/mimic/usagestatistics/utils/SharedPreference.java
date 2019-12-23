package ca.mimic.usagestatistics.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SharedPreference {
    private static final String LOCKED_APP = "locked_app";

    public SharedPreference() {
        super();
    }

    // This four methods are used for maintaining favorites.
    public void saveLocked(Context context, List<String> lockedApp) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;

        settings = context.getSharedPreferences(AppLockConstants.MyPREFERENCES,
                Context.MODE_PRIVATE);
        editor = settings.edit();
        Gson gson = new Gson();
        String jsonLockedApp = gson.toJson(lockedApp);
        editor.putString(LOCKED_APP, jsonLockedApp);
        editor.commit();
    }

    // This four methods are used for maintaining favorites.
    public void savePasswordApp(Context context, String password) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;

        settings = context.getSharedPreferences(AppLockConstants.MyPREFERENCES,
                Context.MODE_PRIVATE);
        editor = settings.edit();
        editor.putString(AppLockConstants.PASSWORD_APP, password);

        // Save.
        editor.apply();
    }

    public void addLocked(final Context context, String app) {
        List<String> lockedApp = getLocked(context);
        if (lockedApp == null)
            lockedApp = new ArrayList<String>();
        lockedApp.add(app);
        saveLocked(context, lockedApp);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                Toast.makeText(context, "App da duoc khoa", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void removeLocked(Context context, String app) {
        ArrayList<String> locked = getLocked(context);
        if (locked != null) {
            locked.remove(app);
            saveLocked(context, locked);
        }
    }

    public void removeAllLocked(Context context) {
        ArrayList<String> locked = getLocked(context);
        if (locked != null) {
            locked.removeAll(locked);
            saveLocked(context, locked);
        }
    }

    public ArrayList<String> getLocked(Context context) {
        SharedPreferences settings;
        List<String> locked;

        settings = context.getSharedPreferences(AppLockConstants.MyPREFERENCES,
                Context.MODE_PRIVATE);

        if (settings.contains(LOCKED_APP)) {
            String jsonLocked = settings.getString(LOCKED_APP, null);
            Gson gson = new Gson();
            String[] lockedItems = gson.fromJson(jsonLocked,
                    String[].class);

            locked = Arrays.asList(lockedItems);
            locked = new ArrayList<String>(locked);
        } else
            return null;
        return (ArrayList<String>) locked;
    }

    public String getPassword(Context context) {
        SharedPreferences passwordPref;
        passwordPref = context.getSharedPreferences(AppLockConstants.MyPREFERENCES, Context.MODE_PRIVATE);
        if (passwordPref.contains(AppLockConstants.PASSWORD)) {
            return passwordPref.getString(AppLockConstants.PASSWORD, "");
        }
        return "";
    }

    public String getPasswordApp(Context context) {
        SharedPreferences checkSetPinCodePref;
        checkSetPinCodePref = context.getSharedPreferences(AppLockConstants.MyPREFERENCES, Context.MODE_PRIVATE);
        return checkSetPinCodePref.getString(AppLockConstants.PASSWORD_APP, "");
    }
}
