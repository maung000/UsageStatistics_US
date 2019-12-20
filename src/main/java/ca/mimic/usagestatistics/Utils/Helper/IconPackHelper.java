/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Modifications to original by Jeff Corcoran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ca.mimic.usagestatistics.Utils.Helper;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import ca.mimic.usagestatistics.Models.AppsRowItem;
import ca.mimic.usagestatistics.R;
import ca.mimic.usagestatistics.Activity.Settings;
import ca.mimic.usagestatistics.Utils.Tools;

public class IconPackHelper {

    public final static String[] sSupportedActions = new String[]{
            "org.adw.launcher.THEMES",
            "com.gau.go.launcherex.theme"
    };

    public static final String[] sSupportedCategories = new String[]{
            "com.fede.launcher.THEME_ICONPACK",
            "com.anddoes.launcher.THEME",
            "com.teslacoilsw.launcher.THEME"
    };

    // Holds package/class -> drawable
    private Map<String, String> mIconPackResources;
    private final Context mContext;
    private String mLoadedIconPackName;
    private Resources mLoadedIconPackResource;

    protected static Settings mActivity;
    protected static AppsRowItem mTask;
    protected static Settings.PrefsGet prefs;

    IconPackHelper(Context context) {
        mContext = context;
        mIconPackResources = new HashMap<String, String>();

    }

    public static void setActivity(Settings activity) {
        mActivity = activity;
    }

    public static void setTask(AppsRowItem task) {
        mTask = task;
    }

    public static Map<String, IconPackInfo> getSupportedPackages(Context context) {
        Intent i = new Intent();
        Map<String, IconPackInfo> packages = new HashMap<String, IconPackInfo>();
        PackageManager packageManager = context.getPackageManager();
        for (String action : sSupportedActions) {
            i.setAction(action);
            for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
                IconPackInfo info = new IconPackInfo(r, packageManager);
                packages.put(r.activityInfo.packageName, info);
            }
        }
        i = new Intent(Intent.ACTION_MAIN);
        for (String category : sSupportedCategories) {
            i.addCategory(category);
            for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
                IconPackInfo info = new IconPackInfo(r, packageManager);
                packages.put(r.activityInfo.packageName, info);
            }
            i.removeCategory(category);
        }
        return packages;
    }

    public static Map<String, IconPackInfo> getPickerPackages(Context context) {
        Intent i = new Intent();
        Map<String, IconPackInfo> packages = new HashMap<String, IconPackInfo>();
        PackageManager packageManager = context.getPackageManager();
        i.setAction(Settings.ACTION_ADW_PICK_ICON);
        for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
            IconPackInfo info = new IconPackInfo(r, packageManager);
            packages.put(r.activityInfo.packageName, info);
        }
        return packages;
    }

    private static void loadResourcesFromXmlParser(XmlPullParser parser,
                                                   Map<String, String> iconPackResources) throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        do {

            if (eventType != XmlPullParser.START_TAG) {
                continue;
            }

            if (!parser.getName().equalsIgnoreCase("item")) {
                continue;
            }

            String component = parser.getAttributeValue(null, "component");
            String drawable = parser.getAttributeValue(null, "drawable");

            // Validate component/drawable exist
            if (TextUtils.isEmpty(component) || TextUtils.isEmpty(drawable)) {
                continue;
            }

            // Validate format/length of component
            if (!component.startsWith("ComponentInfo{") || !component.endsWith("}")
                    || component.length() < 16) {
                continue;
            }

            // Sanitize stored value
            component = component.substring(14, component.length() - 1).toLowerCase(Locale.getDefault());

            ComponentName name;
            if (!component.contains("/")) {
                // Package icon reference
                iconPackResources.put(component, drawable);
            } else {
                name = ComponentName.unflattenFromString(component);
                if (name != null) {
                    iconPackResources.put(name.getPackageName(), drawable);
                    iconPackResources.put(name.getClassName(), drawable);
                }
            }
        } while ((eventType = parser.next()) != XmlPullParser.END_DOCUMENT);
    }

    private static void loadApplicationResources(Context context,
                                                 Map<String, String> iconPackResources, String packageName) {
        Field[] drawableItems;
        try {
            Context appContext = context.createPackageContext(packageName,
                    Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
            drawableItems = Class.forName(packageName + ".R$drawable",
                    true, appContext.getClassLoader()).getFields();
        } catch (Exception e) {
            return;
        }

        for (Field f : drawableItems) {
            String name = f.getName();

            String icon = name.toLowerCase(Locale.getDefault());
            name = name.replaceAll("_", ".");

            iconPackResources.put(name, icon);

            int activityIndex = name.lastIndexOf(".");
            if (activityIndex <= 0 || activityIndex == name.length() - 1) {
                continue;
            }

            String iconPackage = name.substring(0, activityIndex);
            if (TextUtils.isEmpty(iconPackage)) {
                continue;
            }
            iconPackResources.put(iconPackage, icon);

            String iconActivity = name.substring(activityIndex + 1);
            if (TextUtils.isEmpty(iconActivity)) {
                continue;
            }
            iconPackResources.put(iconPackage + "." + iconActivity, icon);
        }
    }

    public boolean loadIconPack(String packageName) {
        mIconPackResources = getIconPackResources(mContext, packageName);
        Resources res;
        try {
            res = mContext.getPackageManager().getResourcesForApplication(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        mLoadedIconPackResource = res;
        mLoadedIconPackName = packageName;
        return true;
    }

    public static Map<String, String> getIconPackResources(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return null;
        }

        Resources res;
        try {
            res = context.getPackageManager().getResourcesForApplication(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        XmlPullParser parser = null;
        InputStream inputStream = null;
        Map<String, String> iconPackResources = new HashMap<String, String>();

        try {
            inputStream = res.getAssets().open("appfilter.xml");
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            parser = factory.newPullParser();
            parser.setInput(inputStream, "UTF-8");
        } catch (Exception e) {
            // Catch any exception since we want to fall back to parsing the xml/
            // resource in all cases
            int resId = res.getIdentifier("appfilter", "xml", packageName);
            if (resId != 0) {
                parser = res.getXml(resId);
            }
        }

        if (parser != null) {
            try {
                loadResourcesFromXmlParser(parser, iconPackResources);
                return iconPackResources;
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Cleanup resources
                if (parser instanceof XmlResourceParser) {
                    ((XmlResourceParser) parser).close();
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        // Application uses a different theme format (most likely launcher pro)
        int arrayId = res.getIdentifier("theme_iconpack", "array", packageName);
        if (arrayId == 0) {
            arrayId = res.getIdentifier("icon_pack", "array", packageName);
        }

        if (arrayId != 0) {
            String[] iconPack = res.getStringArray(arrayId);
            for (String entry : iconPack) {

                if (TextUtils.isEmpty(entry)) {
                    continue;
                }

                String icon = entry.toLowerCase(Locale.getDefault());
                entry = entry.replaceAll("_", ".");

                iconPackResources.put(entry, icon);

                int activityIndex = entry.lastIndexOf(".");
                if (activityIndex <= 0 || activityIndex == entry.length() - 1) {
                    continue;
                }

                String iconPackage = entry.substring(0, activityIndex);
                if (TextUtils.isEmpty(iconPackage)) {
                    continue;
                }
                iconPackResources.put(iconPackage, icon);

                String iconActivity = entry.substring(activityIndex + 1);
                if (TextUtils.isEmpty(iconActivity)) {
                    continue;
                }
                iconPackResources.put(iconPackage + "." + iconActivity, icon);
            }
        } else {
            loadApplicationResources(context, iconPackResources, packageName);
        }
        return iconPackResources;
    }

    public void unloadIconPack() {
        mLoadedIconPackResource = null;

        mLoadedIconPackName = null;
        if (mIconPackResources != null) {
            mIconPackResources.clear();
        }
    }

    public static IconPackInfo installNewPack(Context context) {
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(Settings.PLAY_STORE_PACKAGENAME);
            ResolveInfo rInfo = context.getPackageManager().resolveActivity(intent, 0);

            Drawable icon = new IconCacheHelper(context).getFullResIcon(rInfo);
            String label = context.getResources().getString(R.string.title_icon_pack_install);
            return new IconPackInfo(label, icon, Settings.PLAY_STORE_PACKAGENAME);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressLint("InflateParams")
    public static void pickIconPack(final Context context, final boolean isPicker, final boolean moreAppIcon) {
        Map<String, IconPackInfo> supportedPackages = (isPicker) ? getPickerPackages(context) : getSupportedPackages(context);
        Boolean noPackages = false;
        if (supportedPackages.isEmpty()) {
            noPackages = true;
        }

        final IconAdapter adapter = new IconAdapter(context, supportedPackages, isPicker, moreAppIcon);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (noPackages) {
            builder.setTitle(R.string.title_icon_pack_not_found);
        } else {
            builder.setTitle(isPicker ? R.string.title_icon_pack_choose_from : R.string.title_add_app_follow);
        }
        AlertDialog alertDialog = builder.create();
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {

                /*String selectedPackage = adapter.getItem(position);
                if (isPicker && !selectedPackage.equals(Settings.PLAY_STORE_PACKAGENAME)) {
                    if (mTask != null && selectedPackage.equals(mTask.getPackageName())) {
                        ComponentName componentName = ComponentName.unflattenFromString(mTask.getPackageName() + "/" + mTask.getClassName());
                        Settings.resetIconComponent(componentName);
                        return;
                    } else if (moreAppIcon && position == 0) {
                        Settings.resetIconCache(Settings.MORE_APPS_PACKAGE);
                        return;
                    }

                    try {
                        Intent intent = new Intent();
                        intent.setPackage(selectedPackage);
                        intent.setAction(Settings.ACTION_ADW_PICK_ICON);
                        mActivity.startActivityForResult(intent, moreAppIcon ? 2 : 1);
                    } catch (Exception e) {
                        Tools.USLog("Change icon intent failed! " + e + " : " + selectedPackage);
                    }
                    return;
                }
                prefs = new Settings.PrefsGet(context.getSharedPreferences(context.getPackageName(), Context.MODE_MULTI_PROCESS));
                SharedPreferences.Editor mEditor = prefs.editorGet();

                Tools.USLog("selectedPAckage: " + selectedPackage);
                if (selectedPackage.equals(Settings.PLAY_STORE_PACKAGENAME)) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(Settings.PLAY_STORE_SEARCH_URI));

                    context.startActivity(intent);
                    return;
                }
                mEditor.putString(Settings.ICON_PACK_PREFERENCE, selectedPackage);
                mEditor.apply();

                // Deleting cached icons
                File[] files = context.getCacheDir().listFiles();
                for (File file : files) {
                    if (file.toString().contains(IconCacheHelper.getResourceName(Settings.MORE_APPS_PACKAGE)))
                        continue;
                    file.delete();
                }

                Settings.iconPackUpdate.iconPackUpdated();*/
            }
        });

        if (isPicker) {
            boolean needsWarning = true;
            String alertTxt;
            String selectedPackage = prefs.prefsGet().getString(Settings.ICON_PACK_PREFERENCE, null);

            if (selectedPackage == null || selectedPackage.isEmpty()) {
                alertTxt = context.getString(R.string.title_icon_pack_no_single_picks);
            } else {
                for (int i = 0; i < supportedPackages.size(); i++) {
                    if (supportedPackages.containsKey(selectedPackage)) {
                        needsWarning = false;
                    }
                }
                alertTxt = String.format(context.getString(R.string.title_icon_pack_no_single_ui),
                        Tools.getApplicationName(context, selectedPackage));
            }
            if (needsWarning) {
                LinearLayout iconPackWarning = (LinearLayout) alertDialog.getLayoutInflater().inflate(R.layout.iconpack_main, null);
                TextView tv = (TextView) iconPackWarning.findViewById(R.id.iconpack_warning);
                tv.setText(alertTxt);
                builder.setView(iconPackWarning);
            }
        }

        builder.show();
    }

    public static void pickIconPack(final Context context) {
        pickIconPack(context, false, false);
    }

    public static void pickIconPicker(final Context context) {
        pickIconPack(context, true, false);
    }

    boolean isIconPackLoaded() {
        return mLoadedIconPackResource != null &&
                mLoadedIconPackName != null &&
                mIconPackResources != null;
    }

    private String replaceActivityName(String activityName) {
        // Hack for Gallery not showing up properly on some icon packs
        activityName = activityName.replace("com.android.gallery3d.app.galleryactivity", "com.android.gallery3d.app.gallery");
        return activityName;
    }

    private int getResourceIdForDrawable(String resource) {
        return mLoadedIconPackResource.getIdentifier(resource, "drawable", mLoadedIconPackName);
    }

    public Resources getIconPackResources() {
        return mLoadedIconPackResource;
    }

    public int getResourceIdForActivityIcon(ActivityInfo info) {
        String activityName = replaceActivityName(info.name.toLowerCase(Locale.getDefault()));
        String drawable = mIconPackResources.get(activityName);
        if (drawable == null) {
            activityName = replaceActivityName(info.name.toLowerCase(Locale.getDefault()));
            drawable = mIconPackResources.get(activityName);
        }
        if (drawable == null) {
            // Icon pack doesn't have an icon for the activity, fallback to package icon
            drawable = mIconPackResources.get(info.packageName.toLowerCase(Locale.getDefault()));
            if (drawable == null) {
                return 0;
            }
        }
        return getResourceIdForDrawable(drawable);
    }

    static class IconPackInfo {
        String packageName;
        CharSequence label;
        Drawable icon;

        IconPackInfo(ResolveInfo r, PackageManager packageManager) {
            packageName = r.activityInfo.packageName;
            icon = r.loadIcon(packageManager);
            label = r.loadLabel(packageManager);
        }

        public IconPackInfo(String label, Drawable icon, String packageName) {
            this.label = label;
            this.icon = icon;
            this.packageName = packageName;
        }
    }

    private static class IconAdapter extends BaseAdapter {
        ArrayList<IconPackInfo> mSupportedPackages;
        LayoutInflater mLayoutInflater;
        String mCurrentIconPack;
        int mCurrentIconPackPosition;

        IconAdapter(Context context, Map<String, IconPackInfo> supportedPackages, boolean isPicker, boolean moreAppIcon) {
            mLayoutInflater = LayoutInflater.from(context);
            mSupportedPackages = new ArrayList<IconPackInfo>(supportedPackages.values());
            Collections.sort(mSupportedPackages, new Comparator<IconPackInfo>() {
                @Override
                public int compare(IconPackInfo lhs, IconPackInfo rhs) {
                    return lhs.label.toString().compareToIgnoreCase(rhs.label.toString());
                }
            });

            if (isPicker && (mTask != null || moreAppIcon)) {
                Resources res = context.getResources();
                String defaultLabel = res.getString(R.string.reset_icon);
                if (!moreAppIcon) {
                    Intent intent = context.getPackageManager().getLaunchIntentForPackage(mTask.getPackageName());
                    ResolveInfo rInfo = context.getPackageManager().resolveActivity(intent, 0);

                    Drawable icon = new IconCacheHelper(context).getFullResIcon(rInfo.activityInfo, true);
                    mSupportedPackages.add(0, new IconPackInfo(defaultLabel, icon, mTask.getPackageName()));
                }
            } else {
                Resources res = context.getResources();
                String defaultLabel = res.getString(R.string.default_icon_pack);
                Drawable icon = res.getDrawable(R.drawable.ic_launcher_home);
                mSupportedPackages.add(0, new IconPackInfo(defaultLabel, icon, ""));
            }

            IconPackInfo installNew = installNewPack(context);
            if (installNew != null)
                mSupportedPackages.add(installNew);


            prefs = new Settings.PrefsGet(context.getSharedPreferences(context.getPackageName(), Context.MODE_MULTI_PROCESS));
            SharedPreferences mPrefs = prefs.prefsGet();
            mCurrentIconPack = mPrefs.getString(Settings.ICON_PACK_PREFERENCE, "");
        }

        @Override
        public int getCount() {
            return mSupportedPackages.size();
        }

        @Override
        public String getItem(int position) {
            return mSupportedPackages.get(position).packageName;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.iconpack_chooser, null);
            }
            IconPackInfo info = mSupportedPackages.get(position);
            TextView txtView = (TextView) convertView.findViewById(R.id.title);
            txtView.setText(info.label);
            ImageView imgView = (ImageView) convertView.findViewById(R.id.icon);
            imgView.setImageDrawable(info.icon);
            ImageView chk = (ImageView) convertView.findViewById(R.id.check);
            boolean isCurrentIconPack = info.packageName.equals(mCurrentIconPack);
            chk.setVisibility(isCurrentIconPack ? View.VISIBLE : View.GONE);
            if (isCurrentIconPack) {
                mCurrentIconPackPosition = position;
            }
            return convertView;
        }

    }

}
