package com.taskmanagement.service;

import com.taskmanagement.dto.TaskRequest;
import com.taskmanagement.dto.TaskResponse;
import com.taskmanagement.entity.Role;
import com.taskmanagement.entity.Task;
import com.taskmanagement.entity.User;
import com.taskmanagement.exception.ResourceNotFoundException;
import com.taskmanagement.repository.TaskRepository;
import com.taskmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private boolean isAdmin() {
        User user = getCurrentUser();
        return user.getRole() == Role.ADMIN;
    }

    public Page<TaskResponse> getAllTasks(int page, int size, String sortBy, String status) {
        Sort sort = Sort.by(Sort.Direction.DESC, sortBy != null ? sortBy : "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);

        User currentUser = getCurrentUser();
        Page<Task> tasks;

        if (isAdmin()) {
            if (status != null && !status.isEmpty()) {
                tasks = taskRepository.findByStatus(status.toUpperCase(), pageable);
            } else {
                tasks = taskRepository.findAll(pageable);
            }
        } else {
            if (status != null && !status.isEmpty()) {
                tasks = taskRepository.findByUserIdAndStatus(currentUser.getId(), status.toUpperCase(), pageable);
            } else {
                tasks = taskRepository.findByUserId(currentUser.getId(), pageable);
            }
        }

        return tasks.map(this::convertToResponse);
    }

    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        User currentUser = getCurrentUser();
        if (!isAdmin() && !task.getUserId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("You don't have permission to view this task");
        }

        return convertToResponse(task);
    }

    public TaskResponse createTask(TaskRequest request) {
        User currentUser = getCurrentUser();

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus() != null ? request.getStatus().toUpperCase() : "PENDING");
        task.setDeadline(request.getDeadline());
        task.setUserId(currentUser.getId());

        Task savedTask = taskRepository.save(task);
        return convertToResponse(savedTask);
    }

    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        User currentUser = getCurrentUser();
        if (!isAdmin() && !task.getUserId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("You don't have permission to update this task");
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus() != null ? request.getStatus().toUpperCase() : task.getStatus());
        task.setDeadline(request.getDeadline());

        Task updatedTask = taskRepository.save(task);
        return convertToResponse(updatedTask);
    }

    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        User currentUser = getCurrentUser();
        if (!isAdmin() && !task.getUserId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("You don't have permission to delete this task");
        }

        taskRepository.delete(task);
    }

    private TaskResponse convertToResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getDeadline(),
                task.getUserId(),
                task.getCreatedAt(),
                task.getUpdatedAt());
    }
}
