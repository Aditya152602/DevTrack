package com.devtrack.devtrack;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TaskRequest {
    @NotBlank(message = "Task title cannot be empty")
    @Size(min = 3, max = 100, message = "Task title must be between 3 and 100 characters")
    private String title;
    private boolean completed;

    public TaskRequest() {}
    public String getTitle() { return title; }
    public boolean isCompleted() { return completed; }
    public void setTitle(String title) { this.title = title; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
