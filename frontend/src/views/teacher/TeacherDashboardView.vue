<script setup lang="ts">
import {
  AlertTriangle,
  BookOpen,
  Users,
  Award,
  BarChart3,
  CheckCircle,
  XCircle,
} from "@lucide/vue"
import { ref, onMounted, watch, nextTick } from "vue"
import { useI18n } from "vue-i18n"
import * as echarts from "echarts"
import {
  fetchCourseOptions,
  fetchSemesterOptions,
  fetchDashboardStats,
  fetchScoreDistribution,
  fetchTop5BySubject,
  fetchTop5Total,
  fetchWarnings,
  fetchRecentRecords,
  type CourseOption,
  type SemesterOption,
  type DashboardStats,
  type ScoreDistribution,
  type TopStudent,
  type Warning,
  type RecentRecord,
} from "../../api/dashboard"

const { t } = useI18n()

const userInfo = ref<{
  name: string
  username: string
  role: string
} | null>(null)

try {
  const stored = localStorage.getItem("userInfo")
  if (stored) {
    userInfo.value = JSON.parse(stored)
  }
} catch (e) {
  console.error("Failed to get user info", e)
}

// ========== 数据状态 ==========
const semesterOptions = ref<SemesterOption[]>([])
const selectedSemester = ref("")
const courseOptions = ref<CourseOption[]>([])
const selectedCourseId = ref<number | undefined>(undefined)

const statsData = ref<DashboardStats | null>(null)
const distributionData = ref<ScoreDistribution | null>(null)
const top5Subject = ref<TopStudent[]>([])
const top5Total = ref<TopStudent[]>([])
const warningData = ref<Warning[]>([])
const recentRecords = ref<RecentRecord[]>([])

const chartRef = ref<HTMLElement | null>(null)
let chartInstance: echarts.ECharts | null = null

// ========== 加载数据 ==========
async function loadSemesters() {
  try {
    semesterOptions.value = await fetchSemesterOptions()
    if (semesterOptions.value.length > 0) {
      const first = semesterOptions.value[0]
      selectedSemester.value = first.academicYear + "-" + first.semester
    }
  } catch (e) {
    console.error("Failed to load semesters", e)
  }
}

async function loadCourses() {
  try {
    courseOptions.value = await fetchCourseOptions()
  } catch (e) {
    console.error("Failed to load courses", e)
  }
}

function getSemesterParams() {
  if (!selectedSemester.value) return {}
  const parts = selectedSemester.value.split("-")
  if (parts.length >= 3) {
    return { academicYear: parts[0] + "-" + parts[1], semester: parts[2] }
  }
  return {}
}

async function loadAllData() {
  const params = getSemesterParams()

  try {
    const [stats, warnings, recent] = await Promise.all([
      fetchDashboardStats(params),
      fetchWarnings(params),
      fetchRecentRecords(5),
    ])
    statsData.value = stats
    warningData.value = warnings
    recentRecords.value = recent
  } catch (e) {
    console.error("Failed to load dashboard data", e)
  }

  try {
    const top5T = await fetchTop5Total(params)
    top5Total.value = top5T
  } catch (e) {
    console.error("Failed to load top5 total", e)
  }

  await loadDistribution()
  await loadTop5Subject()
}

async function loadDistribution() {
  try {
    const dist = await fetchScoreDistribution({ courseId: selectedCourseId.value })
    distributionData.value = dist
    await nextTick()
    renderChart()
  } catch (e) {
    console.error("Failed to load distribution", e)
  }
}

async function loadTop5Subject() {
  try {
    top5Subject.value = await fetchTop5BySubject({ courseId: selectedCourseId.value })
  } catch (e) {
    console.error("Failed to load top5 subject", e)
  }
}

// ========== ECharts ==========
function renderChart() {
  if (!chartRef.value || !distributionData.value) return

  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }

  const { labels, counts } = distributionData.value

  chartInstance.setOption({
    tooltip: {
      trigger: "axis",
      axisPointer: { type: "shadow" },
    },
    grid: {
      left: "3%",
      right: "4%",
      bottom: "3%",
      top: "8%",
      containLabel: true,
    },
    xAxis: {
      type: "category",
      data: labels,
      axisLine: { lineStyle: { color: "#e2e8f0" } },
      axisLabel: { color: "#64748b", fontSize: 12 },
    },
    yAxis: {
      type: "value",
      axisLine: { show: false },
      splitLine: { lineStyle: { color: "#f1f5f9" } },
      axisLabel: { color: "#64748b", fontSize: 12 },
    },
    series: [
      {
        type: "bar",
        data: counts,
        barWidth: "70%",
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "#155e75" },
            { offset: 1, color: "#0f766e" },
          ]),
          borderRadius: [4, 4, 0, 0],
        },
      },
    ],
  })
}

// ========== 工具函数 ==========
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

function formatDate(dateStr: string) {
  if (!dateStr) return ""
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`
}

function getWarningColor(type: string) {
  if (type === t('score.fail')) return "text-[#dc2626]"
  return "text-[#b45309]"
}

// ========== 生命周期 ==========
onMounted(async () => {
  await loadSemesters()
  await loadCourses()
  await loadAllData()

  window.addEventListener("resize", () => {
    chartInstance?.resize()
  })
})

watch(selectedSemester, () => {
  loadAllData()
})

watch(selectedCourseId, () => {
  loadDistribution()
  loadTop5Subject()
})
</script>

<template>
  <div>
    <!-- 学期筛选 -->
    <div class="mb-6 flex items-center gap-3">
      <label class="text-sm font-medium text-[#475569]">{{ t('score.semester') }}</label>
          <select
            v-model="selectedSemester"
            class="h-10 rounded-md border border-[#e2e8f0] bg-white px-3 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75]"
          >
            <option value="">{{ t('student.allSemesters') }}</option>
            <option v-for="opt in semesterOptions" :key="opt.academicYear + '-' + opt.semester" :value="opt.academicYear + '-' + opt.semester">
              {{ opt.label }}
            </option>
          </select>
        </div>

        <!-- 统计卡片 -->
        <div class="mb-6 grid gap-4 grid-cols-2 lg:grid-cols-3 xl:grid-cols-6">
          <div class="rounded-lg border border-[#e2e8f0] bg-white p-4">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-[#64748b]">{{ t('teacher.teachingCourses') }}</p>
                <p class="mt-1 text-2xl font-semibold text-[#0f172a]">{{ statsData?.courseCount ?? 0 }}</p>
              </div>
              <div class="grid size-10 place-items-center rounded-md bg-[#f0fdf4]">
                <BookOpen class="size-5 text-[#15803d]" />
              </div>
            </div>
          </div>

          <div class="rounded-lg border border-[#e2e8f0] bg-white p-4">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-[#64748b]">{{ t('teacher.teachingStudents') }}</p>
                <p class="mt-1 text-2xl font-semibold text-[#0f172a]">{{ statsData?.studentCount ?? 0 }}</p>
              </div>
              <div class="grid size-10 place-items-center rounded-md bg-[#eff6ff]">
                <Users class="size-5 text-[#2563eb]" />
              </div>
            </div>
          </div>

          <div class="rounded-lg border border-[#e2e8f0] bg-white p-4">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-[#64748b]">{{ t('score.average') }}</p>
                <p class="mt-1 text-2xl font-semibold text-[#0f172a]">{{ statsData?.averageScore ?? 0 }}</p>
              </div>
              <div class="grid size-10 place-items-center rounded-md bg-[#ccfbf1]">
                <BarChart3 class="size-5 text-[#0f766e]" />
              </div>
            </div>
          </div>

          <div class="rounded-lg border border-[#e2e8f0] bg-white p-4">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-[#64748b]">{{ t('score.failCount') }}</p>
                <p class="mt-1 text-2xl font-semibold text-[#dc2626]">{{ statsData?.failingCount ?? 0 }}</p>
              </div>
              <div class="grid size-10 place-items-center rounded-md bg-[#fef2f2]">
                <XCircle class="size-5 text-[#dc2626]" />
              </div>
            </div>
          </div>

          <div class="rounded-lg border border-[#e2e8f0] bg-white p-4">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-[#64748b]">{{ t('score.highest') }}</p>
                <p class="mt-1 text-2xl font-semibold text-[#0f172a]">{{ statsData?.maxScore ?? 0 }}</p>
              </div>
              <div class="grid size-10 place-items-center rounded-md bg-[#fef3c7]">
                <Award class="size-5 text-[#b45309]" />
              </div>
            </div>
          </div>

          <div class="rounded-lg border border-[#e2e8f0] bg-white p-4">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-[#64748b]">{{ t('score.passRate') }}</p>
                <p class="mt-1 text-2xl font-semibold text-[#15803d]">{{ statsData?.passRate ?? 0 }}%</p>
              </div>
              <div class="grid size-10 place-items-center rounded-md bg-[#f0fdf4]">
                <CheckCircle class="size-5 text-[#15803d]" />
              </div>
            </div>
          </div>
        </div>

        <!-- 内容区 -->
        <div class="grid gap-6 lg:grid-cols-3">
          <!-- 图表区 -->
          <div class="rounded-lg border border-[#e2e8f0] bg-white p-4 lg:col-span-2">
            <div class="mb-4 flex items-center justify-between">
              <h2 class="text-sm font-semibold text-[#0f172a]">{{ t('dashboard.scoreDistribution') }}</h2>
              <select
                v-model="selectedCourseId"
                class="h-8 rounded-md border border-[#e2e8f0] bg-white px-2.5 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75]"
              >
                <option :value="undefined">{{ t('course.selectCourse') }}</option>
                <option v-for="c in courseOptions" :key="c.id" :value="c.id">
                  {{ c.courseName }}
                </option>
              </select>
            </div>
            <div ref="chartRef" class="h-72 w-full"></div>
          </div>

          <!-- 最近录入 -->
          <div class="rounded-lg border border-[#e2e8f0] bg-white p-4">
            <h2 class="mb-3 text-sm font-semibold text-[#0f172a]">{{ t('dashboard.recentEntry') }}</h2>
            <div class="space-y-2">
              <div
                v-for="record in recentRecords"
                :key="record.id"
                class="flex items-center justify-between rounded-md border border-[#f1f5f9] p-2.5 hover:bg-[#f8fafc]"
              >
                <div class="min-w-0 flex-1">
                  <p class="truncate text-sm font-medium text-[#475569]">{{ record.studentName }} - {{ record.courseName }}</p>
                  <p class="text-xs text-[#64748b]">{{ formatDate(record.createTime) }} · {{ record.examType }}</p>
                </div>
                <span class="ml-2 shrink-0 text-sm font-semibold text-[#0f172a]">{{ record.scoreValue }}</span>
              </div>
              <div v-if="recentRecords.length === 0" class="py-8 text-center text-sm text-[#94a3b8]">
                {{ t('common.noData') }}
              </div>
            </div>
          </div>
        </div>

        <!-- TOP5 排名 -->
        <div class="mt-6 grid gap-6 lg:grid-cols-2">
          <!-- 单科 TOP5 -->
          <div class="rounded-lg border border-[#e2e8f0] bg-white p-4">
            <div class="mb-3 flex items-center justify-between">
              <h2 class="text-sm font-semibold text-[#0f172a]">{{ t('dashboard.singleTop5') }}</h2>
              <select
                v-model="selectedCourseId"
                class="h-7 rounded-md border border-[#e2e8f0] bg-white px-2 text-xs text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75]"
              >
                <option :value="undefined">{{ t('course.selectCourse') }}</option>
                <option v-for="c in courseOptions" :key="c.id" :value="c.id">
                  {{ c.courseName }}
                </option>
              </select>
            </div>
            <table class="w-full">
              <thead>
                <tr class="border-b border-[#e2e8f0]">
                  <th class="pb-2 text-left text-xs font-medium text-[#64748b]">{{ t('dashboard.rank') }}</th>
                  <th class="pb-2 text-left text-xs font-medium text-[#64748b]">{{ t('student.name') }}</th>
                  <th class="pb-2 text-left text-xs font-medium text-[#64748b]">{{ t('student.class') }}</th>
                  <th class="pb-2 text-left text-xs font-medium text-[#64748b]">{{ t('score.score') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="student in top5Subject"
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
                  <td class="py-2.5 text-sm text-[#64748b]">{{ student.className }}</td>
                  <td class="py-2.5 text-sm font-semibold text-[#0f172a]">{{ student.score }}</td>
                </tr>
              </tbody>
            </table>
            <div v-if="top5Subject.length === 0" class="py-6 text-center text-sm text-[#94a3b8]">
              {{ t('dashboard.selectCourseHint') }}
            </div>
          </div>

          <!-- 总分 TOP5 -->
          <div class="rounded-lg border border-[#e2e8f0] bg-white p-4">
            <div class="mb-3 flex items-center justify-between">
              <h2 class="text-sm font-semibold text-[#0f172a]">{{ t('dashboard.totalTop5') }}</h2>
            </div>
            <table class="w-full">
              <thead>
                <tr class="border-b border-[#e2e8f0]">
                  <th class="pb-2 text-left text-xs font-medium text-[#64748b]">{{ t('dashboard.rank') }}</th>
                  <th class="pb-2 text-left text-xs font-medium text-[#64748b]">{{ t('student.name') }}</th>
                  <th class="pb-2 text-left text-xs font-medium text-[#64748b]">{{ t('student.class') }}</th>
                  <th class="pb-2 text-left text-xs font-medium text-[#64748b]">{{ t('dashboard.totalScore') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="student in top5Total"
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
                  <td class="py-2.5 text-sm text-[#64748b]">{{ student.className }}</td>
                  <td class="py-2.5 text-sm font-semibold text-[#0f172a]">{{ student.score }}</td>
                </tr>
              </tbody>
            </table>
            <div v-if="top5Total.length === 0" class="py-6 text-center text-sm text-[#94a3b8]">
              {{ t('common.noData') }}
            </div>
          </div>
        </div>

        <!-- 成绩预警 -->
        <div class="mt-6 rounded-lg border border-[#e2e8f0] bg-white p-4">
          <div class="mb-3 flex items-center gap-2">
            <AlertTriangle class="size-4 text-[#b45309]" />
            <h2 class="text-sm font-semibold text-[#0f172a]">{{ t('warning.scoreWarning') }}</h2>
          </div>
          <div class="grid gap-4 grid-cols-2 lg:grid-cols-3">
            <div
              v-for="item in warningData"
              :key="item.type"
              class="rounded-md border border-[#e2e8f0] p-3"
            >
              <p class="text-xs text-[#64748b]">{{ item.type }}</p>
              <p :class="['mt-1 text-xl font-semibold', getWarningColor(item.type)]">{{ item.count }} {{ t('unit.person') }}</p>
            </div>
          </div>
          <div v-if="warningData.length === 0" class="py-6 text-center text-sm text-[#94a3b8]">
            {{ t('warning.noWarningData') }}
          </div>
        </div>
      </div> 
</template>
