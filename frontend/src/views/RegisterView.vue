<template>
  <div class="auth-container">
    <div class="auth-card">
      <h2>Register</h2>
      
      <div v-if="error" class="error-message">{{ error }}</div>
      
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label for="username">Username</label>
          <input
            type="text"
            id="username"
            v-model="formData.username"
            required
            minlength="3"
          />
        </div>
        
        <div class="form-group">
          <label for="email">Email</label>
          <input
            type="email"
            id="email"
            v-model="formData.email"
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
            minlength="6"
          />
        </div>
        
        <button type="submit" class="btn btn-primary" :disabled="loading">
          {{ loading ? 'Registering...' : 'Register' }}
        </button>
      </form>
      
      <div class="auth-link">
        Already have an account? <router-link to="/login">Login</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { authService } from '../services/authService';

export default {
  name: 'RegisterView',
  data() {
    return {
      formData: {
        username: '',
        email: '',
        password: ''
      },
      error: '',
      loading: false
    };
  },
  methods: {
    async handleRegister() {
      this.error = '';
      this.loading = true;
      
      try {
        await authService.register(this.formData);
        this.$router.push('/tasks');
      } catch (err) {
        this.error = err.response?.data?.message || 'Registration failed. Please try again.';
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>
