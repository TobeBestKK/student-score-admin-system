<script setup lang="ts">
import {
  AlertTriangle,
  BarChart3,
  BookOpen,
  ChevronDown,
  Clock,
  GraduationCap,
  LogOut,
  TrendingDown,
  TrendingUp,
  Trophy,
  User,
} from "@lucide/vue"
import { ref } from "vue"
import { useRouter } from "vue-router"

const router = useRouter()
const sidebarCollapsed = ref(false)

// 从登录响应中获取的用户信息
const userInfo = ref<{
  name: string
  username: string
  role: string
} | null>(null)

// 模拟用户信息（实际应从 store 或 localStorage 获取）
try {
  const stored = localStorage.getItem("userInfo")
  if (stored) {
    userInfo.value = JSON.parse(stored)
  }
} catch (e) {
  console.error("Failed to get user info", e)
}

function handleLogout() {
  localStorage.removeItem("userInfo")
  localStorage.removeItem("token")
  router.push("/login")
}

const menuItems = ref([
  { icon: BarChart3, label: "学业概览", active: true },
  { icon: BookOpen, label: "课程成绩", active: false },
  { icon: TrendingUp, label: "成绩趋势", active: false },
  { icon: AlertTriangle, label: "预警中心", active: false },
  { icon: Clock, label: "考试安排", active: false },
  { icon: Trophy, label: "我的排名", active: false },
])

const semester = ref("")

const semesterOptions = [
  { value: "2024-2", label: "2024学年 第二学期" },
  { value: "2024-1", label: "2024学年 第一学期" },
]

const statsData = ref<{
  label: string
  value: string
  trend: string
  trendUp: boolean
  icon: typeof TrendingUp
  color: string
  bg: string
}[]>([])

const courseScores = ref<{
  name: string
  score: number
  fullScore: number
  rank: number
}[]>([])

const recentExams = ref<{
  name: string
  date: string
  status: string
  score: number | null
}[]>([])

const warnings = ref<{
  type: string
  message: string
  level: string
}[]>([])

function handleMenuClick(item: { label: string; active: boolean }) {
  menuItems.value.forEach((m) => (m.active = false))
  item.active = true
}

function getScoreColor(score: number) {
  if (score >= 90) return "text-[#15803d]"
  if (score >= 80) return "text-[#155e75]"
  if (score >= 70) return "text-[#b45309]"
  if (score >= 60) return "text-[#dc2626]"
  return "text-[#64748b]"
}

function getScoreBg(score: number) {
  if (score >= 90) return "bg-[#dcfce7]"
  if (score >= 80) return "bg-[#e0f2fe]"
  if (score >= 70) return "bg-[#fef3c7]"
  if (score >= 60) return "bg-[#fef2f2]"
  return "bg-[#f1f5f9]"
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
            学生成绩分析
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
          <h1 class="text-base font-semibold text-[#0f172a]">学业概览</h1>
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

      <!-- 页面内容 -->
      <main class="flex-1 overflow-y-auto p-6">
        <!-- 学期选择 -->
        <div class="mb-4 flex items-center justify-between">
          <h2 class="text-sm text-[#64748b]"></h2>
          <select
            v-model="semester"
            class="h-8 rounded-md border border-[#e2e8f0] bg-white px-2.5 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75]"
          >
            <option v-for="opt in semesterOptions" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </option>
          </select>
        </div>

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

        <!-- 课程成绩 -->
        <div class="rounded-lg border border-[#e2e8f0] bg-white p-4">
          <h2 class="mb-3 text-sm font-semibold text-[#0f172a]">课程成绩</h2>
          <div class="space-y-3">
            <div
              v-for="course in courseScores"
              :key="course.name"
              class="flex items-center justify-between rounded-md border border-[#f1f5f9] p-3"
            >
              <div class="flex items-center gap-3">
                <div
                  :class="[
                    'grid size-10 place-items-center rounded-md text-sm font-semibold',
                    getScoreBg(course.score),
                    getScoreColor(course.score),
                  ]"
                >
                  {{ course.score }}
                </div>
                <div>
                  <p class="text-sm font-medium text-[#475569]">{{ course.name }}</p>
                  <p class="text-xs text-[#64748b]">满分 {{ course.fullScore }}</p>
                </div>
              </div>
              <div class="text-right">
                <p class="text-xs text-[#64748b]">班级排名</p>
                <p class="text-sm font-medium text-[#475569]">第 {{ course.rank }} 名</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 最近考试 & 预警 -->
        <div class="mt-6 grid gap-6 lg:grid-cols-2">
          <!-- 最近考试 -->
          <div class="rounded-lg border border-[#e2e8f0] bg-white p-4">
            <h2 class="mb-3 text-sm font-semibold text-[#0f172a]">最近考试</h2>
            <div class="space-y-2">
              <div
                v-for="exam in recentExams"
                :key="exam.name"
                class="flex items-center justify-between rounded-md border border-[#f1f5f9] p-2.5"
              >
                <div>
                  <p class="text-sm font-medium text-[#475569]">{{ exam.name }}</p>
                  <p class="text-xs text-[#64748b]">{{ exam.date }}</p>
                </div>
                <div class="text-right">
                  <span
                    v-if="exam.status === '已发布' && exam.score !== null"
                    :class="[
                      'rounded-full px-2 py-0.5 text-xs font-medium',
                      getScoreBg(exam.score),
                      getScoreColor(exam.score),
                    ]"
                  >
                    {{ exam.score }} 分
                  </span>
                  <span
                    v-else
                    class="rounded-full bg-[#f1f5f9] px-2 py-0.5 text-xs font-medium text-[#64748b]"
                  >
                    {{ exam.status }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- 预警信息 -->
          <div class="rounded-lg border border-[#e2e8f0] bg-white p-4">
            <h2 class="mb-3 text-sm font-semibold text-[#0f172a]">学业提示</h2>
            <div class="space-y-2">
              <div
                v-for="(item, index) in warnings"
                :key="index"
                :class="[
                  'flex items-start gap-2.5 rounded-md p-3',
                  item.level === 'success'
                    ? 'bg-[#dcfce7]'
                    : item.level === 'warning'
                      ? 'bg-[#fef3c7]'
                      : 'bg-[#e0f2fe]',
                ]"
              >
                <AlertTriangle
                  v-if="item.level === 'warning'"
                  class="mt-0.5 size-4 shrink-0 text-[#b45309]"
                />
                <TrendingUp
                  v-else-if="item.level === 'success'"
                  class="mt-0.5 size-4 shrink-0 text-[#15803d]"
                />
                <BookOpen v-else class="mt-0.5 size-4 shrink-0 text-[#155e75]" />
                <div>
                  <p class="text-sm font-medium text-[#475569]">{{ item.type }}</p>
                  <p class="text-xs text-[#64748b]">{{ item.message }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 成绩趋势 -->
        <div class="mt-6 rounded-lg border border-[#e2e8f0] bg-white p-4">
          <div class="mb-4 flex items-center justify-between">
            <h2 class="text-sm font-semibold text-[#0f172a]">成绩趋势</h2>
            <div class="flex items-center gap-4 text-xs text-[#64748b]">
              <span class="flex items-center gap-1">
                <span class="size-2 rounded-full bg-[#155e75]"></span>
                平均分
              </span>
              <span class="flex items-center gap-1">
                <span class="size-2 rounded-full bg-[#0f766e]"></span>
                年级排名
              </span>
            </div>
          </div>
          <div class="flex h-40 items-center justify-center text-[#94a3b8]">
            <div class="text-center">
              <TrendingUp class="mx-auto size-10 text-[#cbd5e1]" />
              <p class="mt-2 text-sm text-[#64748b]">成绩趋势图表</p>
              <p class="text-xs text-[#94a3b8]">集成 ECharts</p>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>
