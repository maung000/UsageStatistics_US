package ca.mimic.usagestatistics.models;


public class UsageRowItem {
    private String mDay;
    private String packedName;
    private long timeUsage;

    public UsageRowItem(String packedName, long timeUsage, String mDay) {
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
}

