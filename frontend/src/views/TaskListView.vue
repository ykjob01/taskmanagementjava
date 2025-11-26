<template>
  <div>
    <nav class="navbar">
      <div class="navbar-content">
        <h1>Task Management</h1>
        <div class="navbar-right">
          <span class="user-info">{{ user?.username }} ({{ user?.role }})</span>
          <button class="btn btn-secondary" @click="handleLogout">Logout</button>
        </div>
      </div>
    </nav>

    <div class="container">
      <div class="task-header">
        <h2>My Tasks</h2>
        
        <div class="task-controls">
          <select v-model="filters.status" @change="loadTasks">
            <option value="">All Status</option>
            <option value="PENDING">Pending</option>
            <option value="COMPLETED">Completed</option>
          </select>
          
          <select v-model="filters.sortBy" @change="loadTasks">
            <option value="createdAt">Sort by Created Date</option>
            <option value="deadline">Sort by Deadline</option>
          </select>
          
          <button class="btn btn-primary" @click="showCreateModal">Create New Task</button>
        </div>
      </div>

      <div v-if="error" class="error-message">{{ error }}</div>
      <div v-if="success" class="success-message">{{ success }}</div>

      <div v-if="loading" style="text-align: center; color: white; padding: 40px;">
        Loading tasks...
      </div>

      <div v-else class="task-list">
        <div v-for="task in tasks" :key="task.id" class="task-card">
          <h3>{{ task.title }}</h3>
          <p>{{ task.description }}</p>
          <span class="task-status" :class="task.status.toLowerCase()">
            {{ task.status }}
          </span>
          <p v-if="task.deadline">
            <strong>Deadline:</strong> {{ formatDate(task.deadline) }}
          </p>
          <p><strong>Created:</strong> {{ formatDate(task.createdAt) }}</p>
          
          <div class="task-actions">
            <button 
              v-if="task.status === 'PENDING'" 
              class="btn btn-complete" 
              @click="completeTask(task.id)"
            >
              ✓ Complete
            </button>
            <button class="btn btn-sm btn-edit" @click="editTask(task)">Edit</button>
            <button class="btn btn-sm btn-delete" @click="deleteTask(task.id)">Delete</button>
          </div>
        </div>

        <div v-if="tasks.length === 0" style="text-align: center; color: white; padding: 40px;">
          No tasks found. Create your first task!
        </div>
      </div>

      <div v-if="totalPages > 1" class="pagination">
        <button @click="changePage(currentPage - 1)" :disabled="currentPage === 0">
          Previous
        </button>
        <span>Page {{ currentPage + 1 }} of {{ totalPages }}</span>
        <button @click="changePage(currentPage + 1)" :disabled="currentPage >= totalPages - 1">
          Next
        </button>
      </div>
    </div>

    <!-- Task Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <h3>{{ editingTask ? 'Edit Task' : 'Create Task' }}</h3>
        
        <form @submit.prevent="saveTask">
          <div class="form-group">
            <label for="title">Title</label>
            <input
              type="text"
              id="title"
              v-model="taskForm.title"
              required
            />
          </div>
          
          <div class="form-group">
            <label for="description">Description</label>
            <input
              type="text"
              id="description"
              v-model="taskForm.description"
            />
          </div>
          
          <div class="form-group">
            <label for="status">Status</label>
            <select id="status" v-model="taskForm.status">
              <option value="PENDING">Pending</option>
              <option value="COMPLETED">Completed</option>
            </select>
          </div>
          
          <div class="form-group">
            <label for="deadline">Deadline</label>
            <input
              type="datetime-local"
              id="deadline"
              v-model="taskForm.deadline"
            />
          </div>
          
          <div class="modal-actions">
            <button type="submit" class="btn btn-primary" :disabled="saving">
              {{ saving ? 'Saving...' : 'Save' }}
            </button>
            <button type="button" class="btn btn-secondary" @click="closeModal">
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { authService } from '../services/authService';
import { taskService } from '../services/taskService';

export default {
  name: 'TaskListView',
  data() {
    return {
      user: null,
      tasks: [],
      loading: false,
      error: '',
      success: '',
      currentPage: 0,
      totalPages: 0,
      pageSize: 10,
      filters: {
        status: '',
        sortBy: 'createdAt'
      },
      showModal: false,
      editingTask: null,
      taskForm: {
        title: '',
        description: '',
        status: 'PENDING',
        deadline: ''
      },
      saving: false
    };
  },
  mounted() {
    this.user = authService.getCurrentUser();
    this.loadTasks();
  },
  methods: {
    async loadTasks() {
      this.loading = true;
      this.error = '';
      
      try {
        const response = await taskService.getTasks(
          this.currentPage,
          this.pageSize,
          this.filters.sortBy,
          this.filters.status
        );
        this.tasks = response.content;
        this.totalPages = response.totalPages;
      } catch (err) {
        this.error = 'Failed to load tasks';
      } finally {
        this.loading = false;
      }
    },
    
    changePage(page) {
      this.currentPage = page;
      this.loadTasks();
    },
    
    showCreateModal() {
      this.editingTask = null;
      this.taskForm = {
        title: '',
        description: '',
        status: 'PENDING',
        deadline: ''
      };
      this.showModal = true;
    },
    
    editTask(task) {
      this.editingTask = task;
      this.taskForm = {
        title: task.title,
        description: task.description || '',
        status: task.status,
        deadline: task.deadline ? this.formatDateForInput(task.deadline) : ''
      };
      this.showModal = true;
    },
    
    async saveTask() {
      this.saving = true;
      this.error = '';
      this.success = '';
      
      try {
        const taskData = {
          ...this.taskForm,
          deadline: this.taskForm.deadline ? new Date(this.taskForm.deadline).toISOString() : null
        };
        
        if (this.editingTask) {
          await taskService.updateTask(this.editingTask.id, taskData);
          this.success = 'Task updated successfully';
        } else {
          await taskService.createTask(taskData);
          this.success = 'Task created successfully';
        }
        
        this.closeModal();
        this.loadTasks();
        
        setTimeout(() => {
          this.success = '';
        }, 3000);
      } catch (err) {
        this.error = err.response?.data?.message || 'Failed to save task';
      } finally {
        this.saving = false;
      }
    },
    
    async completeTask(id) {
      try {
        const task = this.tasks.find(t => t.id === id);
        await taskService.updateTask(id, {
          title: task.title,
          description: task.description,
          status: 'COMPLETED',
          deadline: task.deadline
        });
        this.success = 'Task marked as completed';
        this.loadTasks();
        
        setTimeout(() => {
          this.success = '';
        }, 3000);
      } catch (err) {
        this.error = 'Failed to complete task';
      }
    },
    
    async deleteTask(id) {
      if (!confirm('Are you sure you want to delete this task?')) {
        return;
      }
      
      try {
        await taskService.deleteTask(id);
        this.success = 'Task deleted successfully';
        this.loadTasks();
        
        setTimeout(() => {
          this.success = '';
        }, 3000);
      } catch (err) {
        this.error = 'Failed to delete task';
      }
    },
    
    closeModal() {
      this.showModal = false;
      this.editingTask = null;
    },
    
    handleLogout() {
      authService.logout();
      this.$router.push('/login');
    },
    
    formatDate(dateString) {
      if (!dateString) return 'N/A';
      const date = new Date(dateString);
      return date.toLocaleString();
    },
    
    formatDateForInput(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toISOString().slice(0, 16);
    }
  }
};
</script>
