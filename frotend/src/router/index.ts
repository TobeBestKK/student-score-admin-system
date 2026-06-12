import { createRouter, createWebHistory } from "vue-router"

import ForgotPasswordView from "@/views/ForgotPasswordView.vue"
import LoginView from "@/views/LoginView.vue"
import StudentDashboardView from "@/views/student/StudentDashboardView.vue"
import TeacherDashboardView from "@/views/teacher/TeacherDashboardView.vue"

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
      path: "/dashboard",
      name: "dashboard",
      component: TeacherDashboardView,
    },
    {
      path: "/student-dashboard",
      name: "student-dashboard",
      component: StudentDashboardView,
    },
    {
      path: "/forgot-password",
      name: "forgot-password",
      component: ForgotPasswordView,
    },
  ],
})