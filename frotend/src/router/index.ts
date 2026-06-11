import { createRouter, createWebHistory } from "vue-router"

import ForgotPasswordView from "@/views/ForgotPasswordView.vue"
import LoginView from "@/views/LoginView.vue"

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/",
      redirect: "/login",
    },
    {
      path: "/login",
      name: "login",
      component: LoginView,
    },
    {
      path: "/forgot-password",
      name: "forgot-password",
      component: ForgotPasswordView,
    },
  ],
})
