
package com.devtrack.devtrack;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskResponse> getAllTasks(Authentication authentication) {
        return taskService.getAllTasks(authentication.getName())
                .stream()
                .map(this::toTaskResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable Long id,
            Authentication authentication) {

        return taskService.getTaskById(id, authentication.getName())
                .map(this::toTaskResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody TaskRequest request,
            Authentication authentication) {

        Task task = new Task(
                null,
                request.getTitle(),
                request.isCompleted()
        );

        Task savedTask = taskService.createTask(
                task,
                authentication.getName()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toTaskResponse(savedTask));
    }


    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request,
            Authentication authentication) {

        Task updatedTask = new Task(
                id,
                request.getTitle(),
                request.isCompleted()
        );

        Task task = taskService.updateTask(
                id,
                updatedTask,
                authentication.getName()
        );

        if (task == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toTaskResponse(task));
    }


    @PutMapping("/{id}/assign")
    public ResponseEntity<TaskResponse> assignTask(
            @PathVariable Long id,
            @RequestBody AssignTaskRequest request,
            Authentication authentication) {

        try {
            Task task = taskService.assignTask(
                    id,
                    request.getAssigneeUsername(),
                    authentication.getName()
            );

            return ResponseEntity.ok(toTaskResponse(task));

        } catch (IllegalArgumentException exception) {

            if ("Only leaders can assign tasks".equals(exception.getMessage())
                    || "Tasks can only be assigned to members"
                    .equals(exception.getMessage())) {

                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            Authentication authentication) {

        boolean deleted = taskService.deleteTask(
                id,
                authentication.getName()
        );

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    private TaskResponse toTaskResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.isCompleted(),
                task.getOwner() != null
                        ? task.getOwner().getUsername()
                        : null,
                task.getAssignee() != null
                        ? task.getAssignee().getUsername()
                        : null
        );
    }


    @GetMapping("/assigned-to-me")
    public List<TaskResponse> getAssignedTasks(Authentication authentication) {

        return taskService.getAssignedTasks(authentication.getName())
                .stream()
                .map(this::toTaskResponse)
                .toList();
    }


    @GetMapping("/team-progress")
    public ResponseEntity<TeamProgressResponse> getTeamProgress(
            Authentication authentication) {

        try {
            TeamProgressResponse progress =
                    taskService.getTeamProgress(authentication.getName());

            return ResponseEntity.ok(progress);

        } catch (IllegalArgumentException exception) {

            if ("Only leaders can view team progress"
                    .equals(exception.getMessage())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return ResponseEntity.notFound().build();
        }
    }


    @PatchMapping("/{id}/completion")
    public ResponseEntity<Void> updateTaskCompletion(
            @PathVariable Long id,
            @RequestParam boolean completed,
            Authentication authentication) {

        boolean updated = taskService.updateTaskCompletion(
                id,
                completed,
                authentication.getName()
        );

        if (!updated) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }



}
