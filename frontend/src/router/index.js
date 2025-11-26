import { createRouter, createWebHistory } from 'vue-router';
import { authService } from '../services/authService';
import LoginView from '../views/LoginView.vue';
import RegisterView from '../views/RegisterView.vue';
import TaskListView from '../views/TaskListView.vue';

const routes = [
    {
        path: '/',
        redirect: '/tasks'
    },
    {
        path: '/login',
        name: 'Login',
        component: LoginView
    },
    {
        path: '/register',
        name: 'Register',
        component: RegisterView
    },
    {
        path: '/tasks',
        name: 'Tasks',
        component: TaskListView,
        meta: { requiresAuth: true }
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes
});

router.beforeEach((to, from, next) => {
    const isAuthenticated = authService.isAuthenticated();

    if (to.meta.requiresAuth && !isAuthenticated) {
        next('/login');
    } else if ((to.path === '/login' || to.path === '/register') && isAuthenticated) {
        next('/tasks');
    } else {
        next();
    }
});

export default router;
