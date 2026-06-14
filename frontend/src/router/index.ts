import { createRouter, createWebHistory } from "vue-router"

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/", redirect: "/login" },
    { path: "/login", name: "login", component: () => import("@/views/LoginView.vue") },
    { path: "/forgot-password", name: "forgot-password", component: () => import("@/views/ForgotPasswordView.vue") },
    { path: "/dashboard", redirect: "/teacher/overview" },
    { path: "/student-dashboard", redirect: "/student/overview" },
    {
      path: "/teacher",
      component: () => import("@/views/teacher/TeacherLayout.vue"),
      meta: { requiresAuth: true, role: "teacher" },
      children: [
        { path: "", redirect: "/teacher/overview" },
        { path: "overview", name: "teacher-overview", component: () => import("@/views/teacher/TeacherOverviewView.vue") },
        { path: "classes", name: "teacher-classes", component: () => import("@/views/teacher/ClassListView.vue") },
        { path: "classes/:id", name: "teacher-class-detail", component: () => import("@/views/teacher/ClassDetailView.vue") },
        { path: "students", name: "teacher-students", component: () => import("@/views/teacher/StudentManageView.vue") },
        { path: "warnings", name: "teacher-warnings", component: () => import("@/views/teacher/TeacherWarningsView.vue") },
      ],
    },
    {
      path: "/student",
      component: () => import("@/views/student/StudentLayout.vue"),
      meta: { requiresAuth: true, role: "student" },
      children: [
        { path: "", redirect: "/student/overview" },
        { path: "overview", name: "student-overview", component: () => import("@/views/student/StudentOverviewView.vue") },
        { path: "scores", name: "student-scores", component: () => import("@/views/student/StudentScoresView.vue") },
        { path: "warnings", name: "student-warnings", component: () => import("@/views/student/StudentWarningsView.vue") },
        { path: "ranking", name: "student-ranking", component: () => import("@/views/student/StudentRankingView.vue") },
      ],
    },
  ],
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem("token")
  const userInfoStr = localStorage.getItem("userInfo")
  let userInfo = null
  try {
    userInfo = userInfoStr ? JSON.parse(userInfoStr) : null
  } catch (e) {
    console.error("Failed to parse userInfo from localStorage", e)
    localStorage.removeItem("userInfo")
  }

  if (to.meta.requiresAuth) {
    if (!token) {
      next({ name: "login" })
      return
    }
    if (to.meta.role && userInfo?.role !== to.meta.role) {
      next(userInfo?.role === "student" ? "/student/overview" : "/teacher/overview")
      return
    }
  }
  next()
})

export { router }
export default router
