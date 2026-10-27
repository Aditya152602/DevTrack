
package com.devtrack.devtrack;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(
            TaskRepository taskRepository,
            UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public List<Task> getAllTasks(String username) {
        User user = findUser(username);
        return taskRepository.findByOwner(user);
    }

    public Optional<Task> getTaskById(Long id, String username) {
        User user = findUser(username);

        return taskRepository.findById(id)
                .filter(task -> task.getOwner() != null
                        && task.getOwner().getId().equals(user.getId()));
    }

    public Task createTask(Task task, String username) {
        User user = findUser(username);

        Task newTask = new Task(
                null,
                task.getTitle(),
                task.isCompleted()
        );
        newTask.setOwner(user);

        return taskRepository.save(newTask);
    }

    public Task updateTask(Long id, Task updatedTask, String username) {
        return getTaskById(id, username)
                .map(task -> {
                    task.setTitle(updatedTask.getTitle());
                    task.setCompleted(updatedTask.isCompleted());
                    return taskRepository.save(task);
                })
                .orElse(null);
    }


    public boolean updateTaskCompletion(
            Long id,
            boolean completed,
            String username) {

        User user = findUser(username);

        Optional<Task> optionalTask = taskRepository.findById(id);

        if (optionalTask.isEmpty()) {
            return false;
        }

        Task task = optionalTask.get();

        boolean isOwner = task.getOwner() != null
                && task.getOwner().getId().equals(user.getId());

        boolean isAssignedMember = task.getAssignee() != null
                && task.getAssignee().getId().equals(user.getId())
                && "MEMBER".equals(user.getRole());

        if (!isOwner && !isAssignedMember) {
            return false;
        }

        task.setCompleted(completed);
        taskRepository.save(task);

        return true;
    }


    public boolean deleteTask(Long id, String username) {
        return getTaskById(id, username)
                .map(task -> {
                    taskRepository.delete(task);
                    return true;
                })
                .orElse(false);
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));
    }


    public Task assignTask(Long taskId, String assigneeUsername, String leaderUsername) {

        User leader = findUser(leaderUsername);

        if (!"LEADER".equals(leader.getRole())) {
            throw new IllegalArgumentException("Only leaders can assign tasks");
        }

        Task task = getTaskById(taskId, leaderUsername)
                .orElseThrow(() ->
                        new IllegalArgumentException("Task not found or not owned by leader"));

        User member = findUser(assigneeUsername);

        if (!"MEMBER".equals(member.getRole())) {
            throw new IllegalArgumentException("Tasks can only be assigned to members");
        }

        task.setAssignee(member);

        return taskRepository.save(task);
    }


    public List<Task> getAssignedTasks(String username) {

        User user = findUser(username);

        return taskRepository.findByAssignee(user);
    }


    public List<Task> getLeaderTasks(String leaderUsername) {

        User leader = findUser(leaderUsername);

        if (!"LEADER".equals(leader.getRole())) {
            throw new IllegalArgumentException("Only leaders can view team progress");
        }

        return taskRepository.findByOwner(leader);
    }


    public TeamProgressResponse getTeamProgress(String leaderUsername) {

        List<Task> tasks = getLeaderTasks(leaderUsername);

        long totalTasks = tasks.size();

        long completedTasks = tasks.stream()
                .filter(Task::isCompleted)
                .count();

        long pendingTasks = totalTasks - completedTasks;

        long assignedTasks = tasks.stream()
                .filter(task -> task.getAssignee() != null)
                .count();

        return new TeamProgressResponse(
                totalTasks,
                completedTasks,
                pendingTasks,
                assignedTasks
        );
    }

}
