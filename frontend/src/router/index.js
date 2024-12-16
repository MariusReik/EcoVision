import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import MainMenu from '../views/MainMenu.vue'
import Profile from '../views/Profile.vue'

const routes = [
  { path: '/login', component: Login },
  { path: '/main-menu', component: MainMenu },
  { path: '/profile', component: Profile },
  { path: '/', redirect: '/login' }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
