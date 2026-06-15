<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  GraduationCap,
  LogOut,
  User,
  BarChart3,
  AlertTriangle,
  ChevronDown,
  Trophy,
  BookOpen,
  TrendingUp,
} from '@lucide/vue'

const router = useRouter()
const route = useRoute()
const sidebarCollapsed = ref(false)

const userInfo = computed(() => {
  const str = localStorage.getItem('userInfo')
  return str ? JSON.parse(str) : { name: '学生', role: 'student' }
})

const currentTitle = computed(() => {
  if (route.path.startsWith('/student/scores')) return '课程成绩'
  if (route.path.startsWith('/student/trend')) return '成绩趋势'
  if (route.path.startsWith('/student/warnings')) return '预警中心'
  if (route.path.startsWith('/student/ranking')) return '我的排名'
  return '学业概览'
})

const menuItems = [
  { icon: BarChart3, label: '学业概览', path: '/student/overview' },
  { icon: BookOpen, label: '课程成绩', path: '/student/scores' },
  { icon: TrendingUp, label: '成绩趋势', path: '/student/trend' },
  { icon: AlertTriangle, label: '预警中心', path: '/student/warnings' },
  { icon: Trophy, label: '我的排名', path: '/student/ranking' },
]

function isActive(path: string) {
  return route.path === path || route.path.startsWith(path + '/')
}

function handleLogout() {
  localStorage.removeItem('userInfo')
  localStorage.removeItem('token')
  router.push('/login')
}
</script>

<template>
  <div class="flex min-h-screen bg-[#f5f8fb]">
    <aside
      :class="[
        'flex flex-col border-r border-[#e2e8f0] bg-white transition-all duration-300',
        sidebarCollapsed ? 'w-[68px]' : 'w-[220px]',
      ]"
    >
      <div class="flex h-14 items-center justify-between border-b border-[#e2e8f0] px-4">
        <div class="flex items-center gap-2.5">
          <div class="grid size-8 shrink-0 place-items-center rounded-lg bg-[#155e75] text-white">
            <GraduationCap class="size-4" />
          </div>
          <span v-if="!sidebarCollapsed" class="text-sm font-semibold text-[#0f172a]">
            学生成绩分析
          </span>
        </div>
      </div>

      <nav class="flex-1 overflow-y-auto px-3 py-3">
        <ul class="space-y-0.5">
          <li v-for="item in menuItems" :key="item.path">
            <router-link
              :to="item.path"
              :class="[
                'flex w-full items-center gap-2.5 rounded-md px-2.5 py-2 text-sm font-medium transition-colors',
                isActive(item.path)
                  ? 'bg-[#ccfbf1] text-[#0f766e]'
                  : 'text-[#475569] hover:bg-[#f1f5f9] hover:text-[#0f172a]',
              ]"
            >
              <component :is="item.icon" class="size-[18px] shrink-0" />
              <span v-if="!sidebarCollapsed">{{ item.label }}</span>
            </router-link>
          </li>
        </ul>
      </nav>

      <div class="border-t border-[#e2e8f0] p-2">
        <button
          class="flex w-full items-center justify-center gap-1.5 rounded-md px-2.5 py-1.5 text-xs text-[#64748b] hover:bg-[#f1f5f9] hover:text-[#475569]"
          @click="sidebarCollapsed = !sidebarCollapsed"
        >
          <ChevronDown
            :class="['size-3.5 transition-transform', sidebarCollapsed ? '-rotate-90' : '']"
          />
          <span v-if="!sidebarCollapsed">收起侧边栏</span>
        </button>
      </div>
    </aside>

    <div class="flex flex-1 flex-col">
      <header class="flex h-14 items-center justify-between border-b border-[#e2e8f0] bg-white px-6">
        <div class="flex items-center gap-3">
          <h1 class="text-base font-semibold text-[#0f172a]">{{ currentTitle }}</h1>
          <span class="rounded-full bg-[#e0f2fe] px-2.5 py-0.5 text-xs font-medium text-[#155e75]">
            学生端
          </span>
        </div>
        <div class="flex items-center gap-4">
          <div v-if="userInfo" class="flex items-center gap-2">
            <div class="grid size-7 place-items-center rounded-full bg-[#f1f5f9] text-[#64748b]">
              <User class="size-3.5" />
            </div>
            <span class="text-sm text-[#475569]">{{ userInfo.name }}</span>
          </div>
          <button
            class="flex items-center gap-1.5 rounded-md px-3 py-1.5 text-xs font-medium text-[#dc2626] hover:bg-[#fef2f2] hover:text-[#b91c1c]"
            @click="handleLogout"
          >
            <LogOut class="size-3.5" />
            <span>退出登录</span>
          </button>
        </div>
      </header>

      <main class="flex-1 overflow-y-auto p-6">
        <router-view />
      </main>
    </div>
  </div>
</template>
