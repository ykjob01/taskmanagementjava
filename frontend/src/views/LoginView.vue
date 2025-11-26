<template>
  <div class="auth-container">
    <div class="auth-card">
      <h2>Login</h2>
      
      <div v-if="error" class="error-message">{{ error }}</div>
      
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="username">Username</label>
          <input
            type="text"
            id="username"
            v-model="formData.username"
            required
          />
        </div>
        
        <div class="form-group">
          <label for="password">Password</label>
          <input
            type="password"
            id="password"
            v-model="formData.password"
            required
          />
        </div>
        
        <button type="submit" class="btn btn-primary" :disabled="loading">
          {{ loading ? 'Logging in...' : 'Login' }}
        </button>
      </form>
      
      <div class="auth-link">
        Don't have an account? <router-link to="/register">Register</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { authService } from '../services/authService';

export default {
  name: 'LoginView',
  data() {
    return {
      formData: {
        username: '',
        password: ''
      },
      error: '',
      loading: false
    };
  },
  methods: {
    async handleLogin() {
      this.error = '';
      this.loading = true;
      
      try {
        await authService.login(this.formData);
        this.$router.push('/tasks');
      } catch (err) {
        this.error = err.response?.data?.message || 'Login failed. Please try again.';
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>
