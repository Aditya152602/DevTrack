
package com.devtrack.devtrack;

public class TeamProgressResponse {

    private long totalTasks;
    private long completedTasks;
    private long pendingTasks;
    private long assignedTasks;

    public TeamProgressResponse(
            long totalTasks,
            long completedTasks,
            long pendingTasks,
            long assignedTasks) {

        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.pendingTasks = pendingTasks;
        this.assignedTasks = assignedTasks;
    }

    public long getTotalTasks() {
        return totalTasks;
    }

    public long getCompletedTasks() {
        return completedTasks;
    }

    public long getPendingTasks() {
        return pendingTasks;
    }

    public long getAssignedTasks() {
        return assignedTasks;
    }
}
