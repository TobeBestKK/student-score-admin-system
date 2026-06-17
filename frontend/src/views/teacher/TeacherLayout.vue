<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  BookOpen,
  GraduationCap,
  LogOut,
  User,
  Users,
  AlertTriangle,
  ChevronDown,
  FileText,
  Settings,
} from '@lucide/vue'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const sidebarCollapsed = ref(false)

const userInfo = computed(() => {
  const str = localStorage.getItem('userInfo')
  return str ? JSON.parse(str) : { name: t('teacher.defaultName'), role: 'teacher' }
})

const currentTitle = computed(() => {
  const path = route.path
  if (path.startsWith('/teacher/classes')) return t('nav.classes')
  if (path.startsWith('/teacher/students')) return t('nav.students')
  if (path.startsWith('/teacher/scores')) return t('nav.scoreManage')
  if (path.startsWith('/teacher/warnings')) return t('nav.teacherWarnings')
  if (path.startsWith('/teacher/settings')) return t('nav.settings')
  return t('nav.dashboard')
})

const menuItems = computed(() => [
  { icon: BookOpen, label: t('nav.dashboard'), path: '/teacher/overview' },
  { icon: Users, label: t('nav.classes'), path: '/teacher/classes' },
  { icon: User, label: t('nav.students'), path: '/teacher/students' },
  { icon: FileText, label: t('nav.scoreManage'), path: '/teacher/scores' },
  { icon: AlertTriangle, label: t('nav.teacherWarnings'), path: '/teacher/warnings' },
  { icon: Settings, label: t('nav.settings'), path: '/teacher/settings' },
])

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
  <div class="flex min-h-screen bg-[#f5f8fb] dark:bg-gray-900">
    <aside
      :class="[
        'shrink-0 flex flex-col border-r border-[#e2e8f0] bg-white transition-all duration-300 dark:border-gray-700 dark:bg-gray-800',
        sidebarCollapsed ? 'w-[68px]' : 'w-[220px]',
      ]"
    >
      <div class="flex h-14 items-center justify-between border-b border-[#e2e8f0] px-4 dark:border-gray-700">
        <div class="flex items-center gap-2.5">
          <div class="grid size-8 shrink-0 place-items-center rounded-lg bg-[#155e75] text-white dark:bg-teal-700">
            <GraduationCap class="size-4" />
          </div>
          <span v-if="!sidebarCollapsed" class="text-sm font-semibold text-[#0f172a] dark:text-white">
            {{ t('login.title') }}
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
                  ? 'bg-[#ccfbf1] text-[#0f766e] dark:bg-teal-900/50 dark:text-teal-300'
                  : 'text-[#475569] hover:bg-[#f1f5f9] hover:text-[#0f172a] dark:text-gray-400 dark:hover:bg-gray-700 dark:hover:text-white',
              ]"
            >
              <component :is="item.icon" class="size-[18px] shrink-0" />
              <span v-if="!sidebarCollapsed">{{ item.label }}</span>
            </router-link>
          </li>
        </ul>
      </nav>

      <div class="border-t border-[#e2e8f0] p-2 dark:border-gray-700">
        <button
          class="flex w-full items-center justify-center gap-1.5 rounded-md px-2.5 py-1.5 text-xs text-[#64748b] hover:bg-[#f1f5f9] hover:text-[#475569] dark:text-gray-500 dark:hover:bg-gray-700 dark:hover:text-gray-300"
          @click="sidebarCollapsed = !sidebarCollapsed"
        >
          <ChevronDown
            :class="['size-3.5 transition-transform', sidebarCollapsed ? '-rotate-90' : '']"
          />
          <span v-if="!sidebarCollapsed">{{ t('common.close') }}</span>
        </button>
      </div>
    </aside>

    <div class="flex flex-1 flex-col">
      <header class="flex h-14 items-center justify-between border-b border-[#e2e8f0] bg-white px-6 dark:border-gray-700 dark:bg-gray-800">
        <div class="flex items-center gap-3">
          <h1 class="text-base font-semibold text-[#0f172a] dark:text-white">{{ currentTitle }}</h1>
          <span class="rounded-full bg-[#ccfbf1] px-2.5 py-0.5 text-xs font-medium text-[#0f766e] dark:bg-teal-900/30 dark:text-teal-300">
            {{ t('nav.teacherPortal') }}
          </span>
        </div>
        <div class="flex items-center gap-4">
          <div v-if="userInfo" class="flex items-center gap-2">
            <div class="grid size-7 place-items-center rounded-full bg-[#f1f5f9] text-[#64748b] dark:bg-gray-700 dark:text-gray-400">
              <User class="size-3.5" />
            </div>
            <span class="text-sm text-[#475569] dark:text-gray-300">{{ userInfo.name }}</span>
          </div>
          <button
            class="flex items-center gap-1.5 rounded-md px-3 py-1.5 text-xs font-medium text-[#dc2626] hover:bg-[#fef2f2] hover:text-[#b91c1c] dark:text-red-400 dark:hover:bg-red-900/30 dark:hover:text-red-300"
            @click="handleLogout"
          >
            <LogOut class="size-3.5" />
            <span>{{ t('nav.logout') }}</span>
          </button>
        </div>
      </header>

      <main class="flex-1 overflow-y-auto p-6">
        <router-view />
      </main>
    </div>
  </div>
</template>
