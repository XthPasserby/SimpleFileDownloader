package com.xthpasserby.lib;

import java.util.ArrayList;
import java.util.List;

/**
 * copy from EventBus https://github.com/greenrobot/EventBus
 */
final class PendingTask {
    private final static List<PendingTask> pendingTaskPool = new ArrayList<PendingTask>();

    DownloadTask downloadTask;
    PendingTask next;

    private PendingTask(DownloadTask task) {
        this.downloadTask = task;
    }

    static PendingTask obtainPendingTask(DownloadTask task) {
        synchronized (pendingTaskPool) {
            int size = pendingTaskPool.size();
            if (size > 0) {
                PendingTask pendingTask = pendingTaskPool.remove(size - 1);
                pendingTask.downloadTask = task;
                pendingTask.next = null;
                return pendingTask;
            }
        }
        return new PendingTask(task);
    }

    static void releasePendingTask(PendingTask pendingTask) {
        pendingTask.downloadTask = null;
        pendingTask.next = null;
        synchronized (pendingTaskPool) {
            if (pendingTaskPool.size() < 100) {
                pendingTaskPool.add(pendingTask);
            }
        }
    }
}
