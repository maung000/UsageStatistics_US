package ca.mimic.usagestatistics;

interface IWatchfulService {
    void createNotification();
    void destroyNotification();
    void buildTasks();
    void buildReorderAndLaunch();

}