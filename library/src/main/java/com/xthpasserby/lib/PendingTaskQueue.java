package com.xthpasserby.lib;

/**
 * copy from EventBus https://github.com/greenrobot/EventBus
 */
final class PendingTaskQueue {
    private PendingTask head;
    private PendingTask tail;

    synchronized void enqueue(PendingTask pendingTask) {
        if (pendingTask == null) {
            throw new NullPointerException("null cannot be enqueued");
        }
        if (tail != null) {
            tail.next = pendingTask;
            tail = pendingTask;
        } else if (head == null) {
            head = tail = pendingTask;
        } else {
            throw new IllegalStateException("Head present, but no tail");
        }
        notifyAll();
    }

    synchronized PendingTask poll() {
        PendingTask pendingTask = head;
        if (head != null) {
            head = head.next;
            if (head == null) {
                tail = null;
            }
        }
        return pendingTask;
    }

    synchronized PendingTask poll(int maxMillisToWait) throws InterruptedException {
        if (head == null) {
            wait(maxMillisToWait);
        }
        return poll();
    }
}
