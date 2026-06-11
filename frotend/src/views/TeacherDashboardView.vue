<script setup lang="ts">
import {
  AlertTriangle,
  BookOpen,
  ChevronDown,
  GraduationCap,
  Settings,
  TrendingDown,
  TrendingUp,
  User,
  Users,
} from "@lucide/vue"
import { ref } from "vue"

const sidebarCollapsed = ref(false)

const menuItems = ref([
  { icon: BookOpen, label: "数据概览", active: true },
  { icon: GraduationCap, label: "学生管理", active: false },
  { icon: Users, label: "成绩管理", active: false },
  { icon: BookOpen, label: "班级管理", active: false },
  { icon: AlertTriangle, label: "成绩预警", active: false },
  { icon: Settings, label: "系统设置", active: false },
])

const semester = ref("")

const semesterOptions = [
  { value: "2024-2", label: "2024学年 第二学期" },
  { value: "2024-1", label: "2024学年 第一学期" },
  { value: "2023-2", label: "2023学年 第二学期" },
]

const statsData = ref<{
  label: string
  value: string
  trend: string
  trendUp: boolean
  icon: typeof Users
  color: string
  bg: string
}[]>([])

const warningData = ref<{
  type: string
  count: number
  color: string
}[]>([])

const topStudents = ref<{
  rank: number
  name: string
  class: string
  score: number
}[]>([])

const recentExams = ref<{
  name: string
  date: string
  status: string
}[]>([])

function handleMenuClick(item: { label: string; active: boolean }) {
  menuItems.value.forEach((m) => (m.active = false))
  item.active = true
}

function getStatusClass(status: string) {
  switch (status) {
    case "已完成":
      return "bg-[#dcfce7] text-[#15803d]"
    case "进行中":
      return "bg-[#fef3c7] text-[#b45309]"
    default:
      return "bg-[#f1f5f9] text-[#64748b]"
  }
}

function getRankClass(rank: number) {
  switch (rank) {
    case 1:
      return "bg-[#fef3c7] text-[#b45309]"
    case 2:
      return "bg-[#f1f5f9] text-[#64748b]"
    case 3:
      return "bg-[#fed7aa] text-[#c2410c]"
    default:
      return "bg-[#f1f5f9] text-[#64748b]"
  }
}
</script>

<template>
  <div class="flex min-h-screen bg-[#f5f8fb]">
    <!-- 侧边栏 -->
    <aside
      :class="[
        'flex flex-col border-r border-[#e2e8f0] bg-white transition-all duration-300',
        sidebarCollapsed ? 'w-[68px]' : 'w-[220px]',
      ]"
    >
      <!-- Logo -->
      <div class="flex h-14 items-center justify-between border-b border-[#e2e8f0] px-4">
        <div class="flex items-center gap-2.5">
          <div class="grid size-8 shrink-0 place-items-center rounded-lg bg-[#155e75] text-white">
            <GraduationCap class="size-4" />
          </div>
          <span v-if="!sidebarCollapsed" class="text-sm font-semibold text-[#0f172a]">
            成绩分析系统
          </span>
        </div>
      </div>

      <!-- 菜单 -->
      <nav class="flex-1 overflow-y-auto px-3 py-3">
        <ul class="space-y-0.5">
          <li v-for="item in menuItems" :key="item.label">
            <button
              :class="[
                'flex w-full items-center gap-2.5 rounded-md px-2.5 py-2 text-sm font-medium transition-colors',
                item.active
                  ? 'bg-[#ccfbf1] text-[#0f766e]'
                  : 'text-[#475569] hover:bg-[#f1f5f9] hover:text-[#0f172a]',
              ]"
              @click="handleMenuClick(item)"
            >
              <component :is="item.icon" class="size-[18px] shrink-0" />
              <span v-if="!sidebarCollapsed">{{ item.label }}</span>
            </button>
          </li>
        </ul>
      </nav>

      <!-- 折叠 -->
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

    <!-- 主内容 -->
    <div class="flex flex-1 flex-col">
      <!-- 顶部栏 -->
      <header class="flex h-14 items-center justify-between border-b border-[#e2e8f0] bg-white px-6">
        <div class="flex items-center gap-3">
          <h1 class="text-base font-semibold text-[#0f172a]">数据概览</h1>
          <span class="rounded-full bg-[#ccfbf1] px-2.5 py-0.5 text-xs font-medium text-[#0f766e]">
            教师端
          </span>
        </div>
        <div class="flex items-center gap-4">
          <div class="flex items-center gap-2">
            <div class="grid size-7 place-items-center rounded-full bg-[#f1f5f9] text-[#64748b]">
              <User class="size-3.5" />
            </div>
            <span class="text-sm text-[#475569]">张老师</span>
          </div>
        </div>
      </header>

      <!-- 页面内容 -->
      <main class="flex-1 overflow-y-auto p-6">
        <!-- 统计卡片 -->
        <div class="mb-6 grid gap-4 grid-cols-2 lg:grid-cols-4">
          <div
            v-for="stat in statsData"
            :key="stat.label"
            class="rounded-lg border border-[#e2e8f0] bg-white p-4"
          >
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-[#64748b]">{{ stat.label }}</p>
                <p class="mt-1 text-2xl font-semibold text-[#0f172a]">{{ stat.value }}</p>
              </div>
              <div :class="['grid size-10 place-items-center rounded-md', stat.bg]">
                <component :is="stat.icon" :class="['size-5', stat.color]" />
              </div>
            </div>
            <div class="mt-2 flex items-center gap-1 text-xs">
              <component
                :is="stat.trendUp ? TrendingUp : TrendingDown"
                :class="['size-3.5', stat.trendUp ? 'text-[#15803d]' : 'text-[#dc2626]']"
              />
              <span :class="stat.trendUp ? 'text-[#15803d]' : 'text-[#dc2626]'">{{ stat.trend }}</span>
              <span class="text-[#64748b]">较上月</span>
            </div>
          </div>
        </div>

        <!-- 内容区 -->
        <div class="grid gap-6 lg:grid-cols-3">
          <!-- 图表区 -->
          <div class="rounded-lg border border-[#e2e8f0] bg-white p-4 lg:col-span-2">
            <div class="mb-4 flex items-center justify-between">
              <h2 class="text-sm font-semibold text-[#0f172a]">成绩分布统计</h2>
              <div class="flex items-center gap-2">
                <select
                  v-model="semester"
                  class="h-8 rounded-md border border-[#e2e8f0] bg-white px-2.5 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75]"
                >
                  <option v-for="opt in semesterOptions" :key="opt.value" :value="opt.value">
                    {{ opt.label }}
                  </option>
                </select>
              </div>
            </div>
            <div class="flex h-52 items-center justify-center text-[#94a3b8]">
              <div class="text-center">
                <TrendingUp class="mx-auto size-10 text-[#cbd5e1]" />
                <p class="mt-2 text-sm text-[#64748b]">图表区域</p>
                <p class="text-xs text-[#94a3b8]">集成 ECharts</p>
              </div>
            </div>
          </div>

          <!-- 最近考试 -->
          <div class="rounded-lg border border-[#e2e8f0] bg-white p-4">
            <h2 class="mb-3 text-sm font-semibold text-[#0f172a]">最近考试</h2>
            <div class="space-y-2">
              <div
                v-for="exam in recentExams"
                :key="exam.name"
                class="flex items-center justify-between rounded-md border border-[#f1f5f9] p-2.5 hover:bg-[#f8fafc]"
              >
                <div class="min-w-0 flex-1">
                  <p class="truncate text-sm font-medium text-[#475569]">{{ exam.name }}</p>
                  <p class="text-xs text-[#64748b]">{{ exam.date }}</p>
                </div>
                <span
                  :class="[
                    'ml-2 shrink-0 rounded-full px-2 py-0.5 text-xs font-medium',
                    getStatusClass(exam.status),
                  ]"
                >
                  {{ exam.status }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- 年级排名 -->
        <div class="mt-6 rounded-lg border border-[#e2e8f0] bg-white p-4">
          <div class="mb-3 flex items-center justify-between">
            <h2 class="text-sm font-semibold text-[#0f172a]">年级成绩 TOP 5</h2>
            <button class="text-xs text-[#0f766e] hover:text-[#0d5d4f]">查看全部</button>
          </div>
          <table class="w-full">
            <thead>
              <tr class="border-b border-[#e2e8f0]">
                <th class="pb-2 text-left text-xs font-medium text-[#64748b]">排名</th>
                <th class="pb-2 text-left text-xs font-medium text-[#64748b]">姓名</th>
                <th class="pb-2 text-left text-xs font-medium text-[#64748b]">班级</th>
                <th class="pb-2 text-left text-xs font-medium text-[#64748b]">总分</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="student in topStudents"
                :key="student.rank"
                class="border-b border-[#f8fafc] last:border-0"
              >
                <td class="py-2.5">
                  <span
                    :class="[
                      'inline-flex size-5 items-center justify-center rounded-full text-xs font-semibold',
                      getRankClass(student.rank),
                    ]"
                  >
                    {{ student.rank }}
                  </span>
                </td>
                <td class="py-2.5 text-sm font-medium text-[#475569]">{{ student.name }}</td>
                <td class="py-2.5 text-sm text-[#64748b]">{{ student.class }}</td>
                <td class="py-2.5 text-sm font-semibold text-[#0f172a]">{{ student.score }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 成绩预警 -->
        <div class="mt-6 rounded-lg border border-[#e2e8f0] bg-white p-4">
          <div class="mb-3 flex items-center gap-2">
            <AlertTriangle class="size-4 text-[#b45309]" />
            <h2 class="text-sm font-semibold text-[#0f172a]">成绩预警</h2>
          </div>
          <div class="grid gap-4 grid-cols-3">
            <div
              v-for="item in warningData"
              :key="item.type"
              class="rounded-md border border-[#e2e8f0] p-3"
            >
              <p class="text-xs text-[#64748b]">{{ item.type }}</p>
              <p :class="['mt-1 text-xl font-semibold', item.color]">{{ item.count }} 人</p>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>
