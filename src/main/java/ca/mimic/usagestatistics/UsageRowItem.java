package ca.mimic.usagestatistics;


import android.content.ComponentName;

public class UsageRowItem  {
    private String mDay;
    private String packedName;
    private long timeUsage;
    public ComponentName mComponentName;
    private boolean active;

    UsageRowItem(String packedName,long timeUsage,String mDay){
        this.packedName = packedName;
        this.timeUsage = timeUsage;
        this.mDay = mDay;
    }

    public String getmDay() {
        return mDay;
    }

    public void setmDay(String mDay) {
        this.mDay = mDay;
    }

    public String getPackedName() {
        return packedName;
    }

    public void setPackedName(String packedName) {
        this.packedName = packedName;
    }

    public long getTimeUsage() {
        return timeUsage;
    }

    public void setTimeUsage(long timeUsage) {
        this.timeUsage = timeUsage;
    }
    //    UsageRowItem (TasksModel task) {
//        setId(task.getId());
//        setPackageName(task.getPackageName());
//        setTimeused(task.getTimeused());
//        setDayused(task.getDayused());
//    }
//
//    public void setComponentName(ComponentName componentName) {
//        mComponentName = componentName;
//    }
//
//    public ComponentName getComponentName() {
//        return mComponentName;
//    }
//
//    public void setStats(String stats) {
//        mStats = stats;
//    }
//
//    public String getStats() {
//        return mStats;
//    }
//
//    public boolean isActive() {
//        return active;
//    }
//
//    public void setActive(boolean active) {
//        this.active = active;
//    }
}

