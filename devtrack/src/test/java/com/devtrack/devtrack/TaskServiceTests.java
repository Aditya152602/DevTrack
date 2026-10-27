
package com.devtrack.devtrack;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class TaskServiceTests {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void getAllTasks_returnsTasksOwnedByUser() {
        User user = new User("aditya_test", "test-password");

        when(userRepository.findByUsername("aditya_test"))
                .thenReturn(Optional.of(user));

        List<Task> tasks = List.of(
                new Task(1L, "Learn Spring Boot", false),
                new Task(2L, "Practice Java", true)
        );

        when(taskRepository.findByOwner(user)).thenReturn(tasks);

        List<Task> result = taskService.getAllTasks("aditya_test");

        assertEquals(2, result.size());
        assertEquals("Learn Spring Boot", result.get(0).getTitle());
        assertEquals("Practice Java", result.get(1).getTitle());

        verify(taskRepository).findByOwner(user);
    }


    @Test
    void getAllTasks_throwsExceptionWhenUserDoesNotExist() {
        when(userRepository.findByUsername("unknown_user"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.getAllTasks("unknown_user")
        );

        assertEquals("User not found", exception.getMessage());
    }


    @Test
    void assignedMemberCanCompleteTheirTask() {
        // Arrange: Create the member and task
        User member = new User("test_user_2", "test-password");
        member.setRole("MEMBER");

        User leader = new User("aditya_test", "test-password");
        leader.setRole("LEADER");

        member.setId(2L);
        leader.setId(1L);

        Task task = new Task(1L, "Learn Spring Boot", false);
        task.setOwner(leader);
        task.setAssignee(member);

        when(userRepository.findByUsername("test_user_2"))
                .thenReturn(Optional.of(member));

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        // Act: Member marks the assigned task as completed
        boolean result = taskService.updateTaskCompletion(
                1L, true, "test_user_2");

        // Assert: Completion succeeds and the task is saved
        assertTrue(result);
        org.junit.jupiter.api.Assertions.assertTrue(task.isCompleted());

        verify(taskRepository).save(task);
    }


    @Test
    void unassignedMemberCannotCompleteTask() {
        // Arrange: Create two members
        User member = new User("test_user_2", "test-password");
        member.setRole("MEMBER");
        member.setId(2L);

        User otherMember = new User("another_member", "test-password");
        otherMember.setRole("MEMBER");
        otherMember.setId(3L);

        // The task is assigned to another member
        Task task = new Task(1L, "Learn Spring Boot", false);
        task.setAssignee(otherMember);

        when(userRepository.findByUsername("test_user_2"))
                .thenReturn(Optional.of(member));

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        // Act: The unassigned member tries to complete the task
        boolean result = taskService.updateTaskCompletion(
                1L, true, "test_user_2");

        // Assert: Access is denied and the task is not saved
        org.junit.jupiter.api.Assertions.assertFalse(result);
        org.junit.jupiter.api.Assertions.assertFalse(task.isCompleted());

        verify(taskRepository, never()).save(task);
    }

    @Test
    void completingNonexistentTaskReturnsFalse() {
        User member = new User("test_user_2", "test-password");
        member.setRole("MEMBER");
        member.setId(2L);

        when(userRepository.findByUsername("test_user_2"))
                .thenReturn(Optional.of(member));

        when(taskRepository.findById(999L))
                .thenReturn(Optional.empty());

        boolean result = taskService.updateTaskCompletion(
                999L, true, "test_user_2");

        org.junit.jupiter.api.Assertions.assertFalse(result);

        verify(taskRepository, never()).save(
                org.mockito.ArgumentMatchers.any(Task.class));
    }

    @Test
    void taskOwnerCanCompleteTheirTask() {
        User owner = new User("aditya_test", "test-password");
        owner.setRole("LEADER");
        owner.setId(1L);

        Task task = new Task(1L, "Learn Spring Boot", false);
        task.setOwner(owner);

        when(userRepository.findByUsername("aditya_test"))
                .thenReturn(Optional.of(owner));

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(taskRepository.save(task))
                .thenReturn(task);

        boolean result = taskService.updateTaskCompletion(
                1L, true, "aditya_test");

        org.junit.jupiter.api.Assertions.assertTrue(result);
        org.junit.jupiter.api.Assertions.assertTrue(task.isCompleted());

        verify(taskRepository).save(task);
    }

    @Test
    void leaderCannotCompleteAnotherUsersTask() {
        User leader = new User("aditya_test", "test-password");
        leader.setRole("LEADER");
        leader.setId(1L);

        User anotherOwner = new User("another_user", "test-password");
        anotherOwner.setId(3L);

        Task task = new Task(2L, "Other user's task", false);
        task.setOwner(anotherOwner);

        when(userRepository.findByUsername("aditya_test"))
                .thenReturn(Optional.of(leader));

        when(taskRepository.findById(2L))
                .thenReturn(Optional.of(task));

        boolean result = taskService.updateTaskCompletion(
                2L, true, "aditya_test");

        org.junit.jupiter.api.Assertions.assertFalse(result);
        org.junit.jupiter.api.Assertions.assertFalse(task.isCompleted());

        verify(taskRepository, never()).save(
                org.mockito.ArgumentMatchers.any(Task.class));
    }

    @Test
    void assignedMemberCanMarkTaskIncomplete() {
        User member = new User("test_user_2", "test-password");
        member.setRole("MEMBER");
        member.setId(2L);

        Task task = new Task(1L, "Learn Spring Boot", true);
        task.setAssignee(member);

        when(userRepository.findByUsername("test_user_2"))
                .thenReturn(Optional.of(member));

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(taskRepository.save(task))
                .thenReturn(task);

        boolean result = taskService.updateTaskCompletion(
                1L, false, "test_user_2");

        org.junit.jupiter.api.Assertions.assertTrue(result);
        org.junit.jupiter.api.Assertions.assertFalse(task.isCompleted());

        verify(taskRepository).save(task);
    }

    @Test
    void unassignedMemberCannotMarkTaskIncomplete() {
        User member = new User("test_user_2", "test-password");
        member.setRole("MEMBER");
        member.setId(2L);

        User otherMember = new User("other_member", "test-password");
        otherMember.setRole("MEMBER");
        otherMember.setId(3L);

        Task task = new Task(1L, "Learn Spring Boot", true);
        task.setAssignee(otherMember);

        when(userRepository.findByUsername("test_user_2"))
                .thenReturn(Optional.of(member));

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        boolean result = taskService.updateTaskCompletion(
                1L, false, "test_user_2");

        org.junit.jupiter.api.Assertions.assertFalse(result);
        org.junit.jupiter.api.Assertions.assertTrue(task.isCompleted());

        verify(taskRepository, never()).save(task);
    }

    @Test
    void memberCannotAssignTask() {
        User member = new User("test_user_2", "password");
        member.setId(2L);
        member.setRole("MEMBER");

        when(userRepository.findByUsername("test_user_2"))
                .thenReturn(Optional.of(member));

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> taskService.assignTask(1L, "another_member", "test_user_2")
        );
    }

    @Test
    void leaderCannotAssignTaskToAnotherLeader() {
        User leader = new User("leader_user", "password");
        leader.setId(1L);
        leader.setRole("LEADER");

        User anotherLeader = new User("another_leader", "password");
        anotherLeader.setId(3L);
        anotherLeader.setRole("LEADER");

        Task task = new Task(1L, "Complete project", false);
        task.setOwner(leader);

        when(userRepository.findByUsername("leader_user"))
                .thenReturn(Optional.of(leader));

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(userRepository.findByUsername("another_leader"))
                .thenReturn(Optional.of(anotherLeader));

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> taskService.assignTask(
                        1L, "another_leader", "leader_user")
        );
    }

    @Test
    void leaderCanAssignTaskToMember() {
        User leader = new User("leader_user", "password");
        leader.setId(1L);
        leader.setRole("LEADER");

        User member = new User("member_user", "password");
        member.setId(2L);
        member.setRole("MEMBER");

        Task task = new Task(1L, "Complete project", false);
        task.setOwner(leader);

        when(userRepository.findByUsername("leader_user"))
                .thenReturn(Optional.of(leader));

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(userRepository.findByUsername("member_user"))
                .thenReturn(Optional.of(member));

        when(taskRepository.save(task)).thenReturn(task);

        Task assignedTask = taskService.assignTask(
                1L, "member_user", "leader_user");

        org.junit.jupiter.api.Assertions.assertEquals(
                member, assignedTask.getAssignee());

        verify(taskRepository).save(task);
    }

    @Test
    void leaderCannotAssignTaskOwnedByAnotherLeader() {
        User leader = new User("leader_user", "password");
        leader.setId(1L);
        leader.setRole("LEADER");

        User anotherLeader = new User("another_leader", "password");
        anotherLeader.setId(3L);
        anotherLeader.setRole("LEADER");

        Task task = new Task(1L, "Complete project", false);
        task.setOwner(anotherLeader);

        when(userRepository.findByUsername("leader_user"))
                .thenReturn(Optional.of(leader));

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> taskService.assignTask(
                        1L, "member_user", "leader_user")
        );
    }

    @Test
    void memberCannotViewTeamProgress() {
        User member = new User("member_user", "password");
        member.setId(2L);
        member.setRole("MEMBER");

        when(userRepository.findByUsername("member_user"))
                .thenReturn(Optional.of(member));

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> taskService.getTeamProgress("member_user")
        );
    }

    @Test
    void leaderCanViewTeamProgress() {
        User leader = new User("leader_user", "password");
        leader.setId(1L);
        leader.setRole("LEADER");

        Task task1 = new Task(1L, "Build API", true);
        task1.setOwner(leader);

        Task task2 = new Task(2L, "Write tests", false);
        task2.setOwner(leader);

        User member = new User("member_user", "password");
        member.setId(2L);
        member.setRole("MEMBER");

        task2.setAssignee(member);

        when(userRepository.findByUsername("leader_user"))
                .thenReturn(Optional.of(leader));

        when(taskRepository.findByOwner(leader))
                .thenReturn(List.of(task1, task2));

        TeamProgressResponse result =
                taskService.getTeamProgress("leader_user");

        org.junit.jupiter.api.Assertions.assertEquals(
                2L, result.getTotalTasks());

        org.junit.jupiter.api.Assertions.assertEquals(
                1L, result.getCompletedTasks());

        org.junit.jupiter.api.Assertions.assertEquals(
                1L, result.getPendingTasks());

        org.junit.jupiter.api.Assertions.assertEquals(
                1L, result.getAssignedTasks());
    }

    @Test
    void leaderCannotViewAnotherLeadersTasks() {
        User leader = new User("leader_user", "password");
        leader.setId(1L);
        leader.setRole("LEADER");

        User anotherLeader = new User("another_leader", "password");
        anotherLeader.setId(3L);
        anotherLeader.setRole("LEADER");

        Task ownTask = new Task(4L, "My team task", false);
        ownTask.setOwner(leader);

        Task otherTask = new Task(5L, "Private team task", false);
        otherTask.setOwner(anotherLeader);

        when(userRepository.findByUsername("leader_user"))
                .thenReturn(Optional.of(leader));

        // The repository returns only tasks owned by this leader.
        when(taskRepository.findByOwner(leader))
                .thenReturn(List.of(ownTask));

        List<Task> result =
                taskService.getLeaderTasks("leader_user");

        org.junit.jupiter.api.Assertions.assertEquals(1, result.size());
        org.junit.jupiter.api.Assertions.assertEquals(
                "My team task", result.get(0).getTitle());

        org.junit.jupiter.api.Assertions.assertFalse(
                result.contains(otherTask));
    }

    @Test
    void leaderCannotDeleteAnotherUsersTask() {
        User leader = new User("leader_user", "password");
        leader.setId(1L);
        leader.setRole("LEADER");

        User anotherLeader = new User("another_leader", "password");
        anotherLeader.setId(3L);
        anotherLeader.setRole("LEADER");

        Task otherTask = new Task(5L, "Private team task", false);
        otherTask.setOwner(anotherLeader);

        when(userRepository.findByUsername("leader_user"))
                .thenReturn(Optional.of(leader));

        when(taskRepository.findById(5L))
                .thenReturn(Optional.of(otherTask));

        boolean result = taskService.deleteTask(5L, "leader_user");

        org.junit.jupiter.api.Assertions.assertFalse(result);

        verify(taskRepository, never()).delete(otherTask);
    }

    @Test
    void leaderCanDeleteTheirOwnTask() {
        User leader = new User("leader_user", "password");
        leader.setId(1L);
        leader.setRole("LEADER");

        Task ownTask = new Task(5L, "My team task", false);
        ownTask.setOwner(leader);

        when(userRepository.findByUsername("leader_user"))
                .thenReturn(Optional.of(leader));

        when(taskRepository.findById(5L))
                .thenReturn(Optional.of(ownTask));

        boolean result = taskService.deleteTask(5L, "leader_user");

        org.junit.jupiter.api.Assertions.assertTrue(result);

        verify(taskRepository).delete(ownTask);
    }

    @Test
    void memberCannotDeleteTaskOwnedByLeader() {
        User leader = new User("leader_user", "password");
        leader.setId(1L);
        leader.setRole("LEADER");

        User member = new User("member_user", "password");
        member.setId(2L);
        member.setRole("MEMBER");

        Task leaderTask = new Task(5L, "Leader's task", false);
        leaderTask.setOwner(leader);

        when(userRepository.findByUsername("member_user"))
                .thenReturn(Optional.of(member));

        when(taskRepository.findById(5L))
                .thenReturn(Optional.of(leaderTask));

        boolean result = taskService.deleteTask(5L, "member_user");

        org.junit.jupiter.api.Assertions.assertFalse(result);

        verify(taskRepository, never()).delete(leaderTask);
    }

    @Test
    void deletingNonexistentTaskReturnsFalse() {
        User user = new User("leader_user", "password");
        user.setId(1L);
        user.setRole("LEADER");

        when(userRepository.findByUsername("leader_user"))
                .thenReturn(Optional.of(user));

        when(taskRepository.findById(999L))
                .thenReturn(Optional.empty());

        boolean result = taskService.deleteTask(999L, "leader_user");

        org.junit.jupiter.api.Assertions.assertFalse(result);

        verify(taskRepository, never()).delete(
                org.mockito.ArgumentMatchers.any(Task.class)
        );
    }

    @Test
    void leaderCannotUpdateAnotherUsersTask() {
        User leader1 = new User("leader_user", "password");
        leader1.setId(1L);
        leader1.setRole("LEADER");

        User leader2 = new User("another_leader", "password");
        leader2.setId(2L);
        leader2.setRole("LEADER");

        Task otherTask = new Task(5L, "Original task", false);
        otherTask.setOwner(leader2);

        Task updatedTask = new Task(null, "Modified task", true);

        when(userRepository.findByUsername("leader_user"))
                .thenReturn(Optional.of(leader1));

        when(taskRepository.findById(5L))
                .thenReturn(Optional.of(otherTask));

        Task result = taskService.updateTask(
                5L, updatedTask, "leader_user");

        org.junit.jupiter.api.Assertions.assertNull(result);

        org.junit.jupiter.api.Assertions.assertEquals(
                "Original task", otherTask.getTitle());

        org.junit.jupiter.api.Assertions.assertFalse(
                otherTask.isCompleted());

        verify(taskRepository, never()).save(otherTask);
    }

    @Test
    void leaderCanUpdateTheirOwnTask() {
        User leader = new User("leader_user", "password");
        leader.setId(1L);
        leader.setRole("LEADER");

        Task ownTask = new Task(5L, "Original task", false);
        ownTask.setOwner(leader);

        Task updatedTask = new Task(null, "Updated task", true);

        when(userRepository.findByUsername("leader_user"))
                .thenReturn(Optional.of(leader));

        when(taskRepository.findById(5L))
                .thenReturn(Optional.of(ownTask));

        when(taskRepository.save(ownTask))
                .thenReturn(ownTask);

        Task result = taskService.updateTask(
                5L, updatedTask, "leader_user");

        org.junit.jupiter.api.Assertions.assertNotNull(result);

        org.junit.jupiter.api.Assertions.assertEquals(
                "Updated task", result.getTitle());

        org.junit.jupiter.api.Assertions.assertTrue(
                result.isCompleted());

        verify(taskRepository).save(ownTask);
    }

    @Test
    void memberCannotUpdateTaskOwnedByLeader() {
        User member = new User("member_user", "password");
        member.setId(2L);
        member.setRole("MEMBER");

        User leader = new User("leader_user", "password");
        leader.setId(1L);
        leader.setRole("LEADER");

        Task leaderTask = new Task(5L, "Leader's task", false);
        leaderTask.setOwner(leader);

        Task updatedTask = new Task(null, "Modified task", true);

        when(userRepository.findByUsername("member_user"))
                .thenReturn(Optional.of(member));

        when(taskRepository.findById(5L))
                .thenReturn(Optional.of(leaderTask));

        Task result = taskService.updateTask(
                5L, updatedTask, "member_user");

        org.junit.jupiter.api.Assertions.assertNull(result);

        org.junit.jupiter.api.Assertions.assertEquals(
                "Leader's task", leaderTask.getTitle());

        org.junit.jupiter.api.Assertions.assertFalse(
                leaderTask.isCompleted());

        verify(taskRepository, never()).save(leaderTask);
    }

    @Test
    void memberCanUpdateTheirOwnTask() {
        User member = new User("member_user", "password");
        member.setId(2L);
        member.setRole("MEMBER");

        Task ownTask = new Task(5L, "Original task", false);
        ownTask.setOwner(member);

        Task updatedTask = new Task(null, "Updated task", true);

        when(userRepository.findByUsername("member_user"))
                .thenReturn(Optional.of(member));

        when(taskRepository.findById(5L))
                .thenReturn(Optional.of(ownTask));

        when(taskRepository.save(ownTask))
                .thenReturn(ownTask);

        Task result = taskService.updateTask(
                5L, updatedTask, "member_user");

        org.junit.jupiter.api.Assertions.assertNotNull(result);

        org.junit.jupiter.api.Assertions.assertEquals(
                "Updated task", result.getTitle());

        org.junit.jupiter.api.Assertions.assertTrue(
                result.isCompleted());

        verify(taskRepository).save(ownTask);
    }

    @Test
    void memberCannotUpdateAnotherUsersTask() {
        User member = new User("member_user", "password");
        member.setId(2L);
        member.setRole("MEMBER");

        User anotherMember = new User("another_member", "password");
        anotherMember.setId(3L);
        anotherMember.setRole("MEMBER");

        Task anotherUsersTask =
                new Task(5L, "Original task", false);

        anotherUsersTask.setOwner(anotherMember);

        Task updatedTask =
                new Task(null, "Hacked task", true);

        when(userRepository.findByUsername("member_user"))
                .thenReturn(Optional.of(member));

        when(taskRepository.findById(5L))
                .thenReturn(Optional.of(anotherUsersTask));

        Task result = taskService.updateTask(
                5L, updatedTask, "member_user");

        org.junit.jupiter.api.Assertions.assertNull(result);

        verify(taskRepository, never()).save(anotherUsersTask);
    }

    @Test
    void memberCannotGetAnotherUsersTaskById() {
        User member = new User("member_user", "password");
        member.setId(2L);
        member.setRole("MEMBER");

        User anotherMember = new User("another_member", "password");
        anotherMember.setId(3L);
        anotherMember.setRole("MEMBER");

        Task anotherUsersTask =
                new Task(5L, "Private task", false);

        anotherUsersTask.setOwner(anotherMember);

        when(userRepository.findByUsername("member_user"))
                .thenReturn(Optional.of(member));

        when(taskRepository.findById(5L))
                .thenReturn(Optional.of(anotherUsersTask));

        Optional<Task> result =
                taskService.getTaskById(5L, "member_user");

        org.junit.jupiter.api.Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void memberCanGetTheirOwnTaskById() {
        User member = new User("member_user", "password");
        member.setId(2L);
        member.setRole("MEMBER");

        Task ownTask = new Task(5L, "My task", false);
        ownTask.setOwner(member);

        when(userRepository.findByUsername("member_user"))
                .thenReturn(Optional.of(member));

        when(taskRepository.findById(5L))
                .thenReturn(Optional.of(ownTask));

        Optional<Task> result =
                taskService.getTaskById(5L, "member_user");

        org.junit.jupiter.api.Assertions.assertTrue(result.isPresent());

        org.junit.jupiter.api.Assertions.assertEquals(
                "My task", result.get().getTitle());
    }

    @Test
    void memberCannotGetAnotherMembersTaskById() {
        User member1 = new User("member_user", "password");
        member1.setId(2L);
        member1.setRole("MEMBER");

        User member2 = new User("member_user_2", "password");
        member2.setId(3L);
        member2.setRole("MEMBER");

        Task otherTask = new Task(6L, "Other member task", false);
        otherTask.setOwner(member2);

        when(userRepository.findByUsername("member_user"))
                .thenReturn(Optional.of(member1));

        when(taskRepository.findById(6L))
                .thenReturn(Optional.of(otherTask));

        Optional<Task> result =
                taskService.getTaskById(6L, "member_user");

        org.junit.jupiter.api.Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void memberCanViewTheirAssignedTasks() {
        User member = new User("member_user", "password");
        member.setId(2L);
        member.setRole("MEMBER");

        Task assignedTask = new Task(7L, "Complete Java practice", false);
        assignedTask.setAssignee(member);

        when(userRepository.findByUsername("member_user"))
                .thenReturn(Optional.of(member));

        when(taskRepository.findByAssignee(member))
                .thenReturn(List.of(assignedTask));

        List<Task> result =
                taskService.getAssignedTasks("member_user");

        org.junit.jupiter.api.Assertions.assertEquals(1, result.size());

        org.junit.jupiter.api.Assertions.assertEquals(
                "Complete Java practice",
                result.get(0).getTitle());
    }

    @Test
    void memberCannotViewAnotherMembersAssignedTasks() {
        User member1 = new User("member_user", "password");
        member1.setId(2L);
        member1.setRole("MEMBER");

        User member2 = new User("member_user_2", "password");
        member2.setId(3L);
        member2.setRole("MEMBER");

        Task otherTask = new Task(8L, "Private assigned task", false);
        otherTask.setAssignee(member2);

        when(userRepository.findByUsername("member_user"))
                .thenReturn(Optional.of(member1));

        when(taskRepository.findByAssignee(member1))
                .thenReturn(List.of());

        List<Task> result =
                taskService.getAssignedTasks("member_user");

        org.junit.jupiter.api.Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void leaderCanViewTeamTasksAssignedToMembers() {
        User leader = new User("leader_user", "password");
        leader.setId(1L);
        leader.setRole("LEADER");

        User member = new User("member_user", "password");
        member.setId(2L);
        member.setRole("MEMBER");

        Task task = new Task(9L, "Build REST API", false);
        task.setOwner(leader);
        task.setAssignee(member);

        when(userRepository.findByUsername("leader_user"))
                .thenReturn(Optional.of(leader));

        when(taskRepository.findByOwner(leader))
                .thenReturn(List.of(task));

        List<Task> result =
                taskService.getLeaderTasks("leader_user");

        org.junit.jupiter.api.Assertions.assertEquals(1, result.size());
        org.junit.jupiter.api.Assertions.assertEquals(
                "Build REST API", result.get(0).getTitle());
        org.junit.jupiter.api.Assertions.assertEquals(
                member, result.get(0).getAssignee());
    }

    @Test
    void leaderTeamProgressCountsTasksCorrectly() {
        User leader = new User("leader_user", "password");
        leader.setId(1L);
        leader.setRole("LEADER");

        User member = new User("member_user", "password");
        member.setId(2L);
        member.setRole("MEMBER");

        Task task1 = new Task(1L, "Build API", true);
        task1.setOwner(leader);
        task1.setAssignee(member);

        Task task2 = new Task(2L, "Write tests", false);
        task2.setOwner(leader);

        Task task3 = new Task(3L, "Fix bugs", false);
        task3.setOwner(leader);
        task3.setAssignee(member);

        when(userRepository.findByUsername("leader_user"))
                .thenReturn(Optional.of(leader));

        when(taskRepository.findByOwner(leader))
                .thenReturn(List.of(task1, task2, task3));

        TeamProgressResponse result =
                taskService.getTeamProgress("leader_user");

        org.junit.jupiter.api.Assertions.assertEquals(3, result.getTotalTasks());
        org.junit.jupiter.api.Assertions.assertEquals(1, result.getCompletedTasks());
        org.junit.jupiter.api.Assertions.assertEquals(2, result.getPendingTasks());
        org.junit.jupiter.api.Assertions.assertEquals(2, result.getAssignedTasks());
    }

    @Test
    void leaderWithNoTasksHasZeroTeamProgress() {
        User leader = new User("leader_user", "password");
        leader.setId(1L);
        leader.setRole("LEADER");

        when(userRepository.findByUsername("leader_user"))
                .thenReturn(Optional.of(leader));

        when(taskRepository.findByOwner(leader))
                .thenReturn(List.of());

        TeamProgressResponse result =
                taskService.getTeamProgress("leader_user");

        org.junit.jupiter.api.Assertions.assertEquals(0, result.getTotalTasks());
        org.junit.jupiter.api.Assertions.assertEquals(0, result.getCompletedTasks());
        org.junit.jupiter.api.Assertions.assertEquals(0, result.getPendingTasks());
        org.junit.jupiter.api.Assertions.assertEquals(0, result.getAssignedTasks());
    }

    @Test
    void leaderProgressRequestFailsWhenUserDoesNotExist() {
        when(userRepository.findByUsername("unknown_user"))
                .thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> taskService.getTeamProgress("unknown_user")
        );
    }

    @Test
    void memberCannotAccessTeamProgress() {
        User member = new User("member_user", "password");
        member.setId(2L);
        member.setRole("MEMBER");

        when(userRepository.findByUsername("member_user"))
                .thenReturn(Optional.of(member));

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> taskService.getTeamProgress("member_user")
        );

        verify(taskRepository, never()).findByOwner(member);
    }
}
