
package com.devtrack.devtrack;

public class TaskResponse {

    private Long id;
    private String title;
    private boolean completed;
    private String ownerUsername;
    private String assigneeUsername;

    public TaskResponse() {
    }

    public TaskResponse(
            Long id,
            String title,
            boolean completed,
            String ownerUsername,
            String assigneeUsername) {

        this.id = id;
        this.title = title;
        this.completed = completed;
        this.ownerUsername = ownerUsername;
        this.assigneeUsername = assigneeUsername;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public String getAssigneeUsername() {
        return assigneeUsername;
    }
}
