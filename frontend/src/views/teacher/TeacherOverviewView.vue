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
import { ref, onMounted, onUnmounted, watch, nextTick } from "vue"
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
} from "@/api/dashboard"
import Card from "@/components/ui/card/Card.vue"
import CardContent from "@/components/ui/card/CardContent.vue"

const { t } = useI18n()

const semesterOptions = ref<SemesterOption[]>([])
const selectedSemester = ref("")
const courseOptions = ref<CourseOption[]>([])
const selectedCourseId = ref<number | undefined>(undefined)

const userInfo = ref<{ id: number } | null>(null)
try {
  const stored = localStorage.getItem("userInfo")
  if (stored) userInfo.value = JSON.parse(stored)
} catch (e) { /* ignore */ }

const statsData = ref<DashboardStats | null>(null)
const distributionData = ref<ScoreDistribution | null>(null)
const top5Subject = ref<TopStudent[]>([])
const top5Total = ref<TopStudent[]>([])
const warningData = ref<Warning[]>([])
const recentRecords = ref<RecentRecord[]>([])

const chartRef = ref<HTMLElement | null>(null)
let chartInstance: echarts.ECharts | null = null

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
  const params = { ...getSemesterParams(), teacherId: userInfo.value?.id }

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
  if (type === "不及格") return "text-[#dc2626]"
  return "text-[#b45309]"
}

const handleResize = () => {
  chartInstance?.resize()
}

onMounted(async () => {
  await loadSemesters()
  await loadCourses()
  await loadAllData()

  window.addEventListener("resize", handleResize)
})

onUnmounted(() => {
  window.removeEventListener("resize", handleResize)
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
  <div class="space-y-6">
    <div class="flex items-center gap-3">
      <label class="text-sm font-medium text-[#475569] dark:text-gray-300">{{ t('score.semester') }}</label>
      <select
        v-model="selectedSemester"
        class="h-10 rounded-md border border-[#e2e8f0] bg-white px-3 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75] dark:bg-gray-700 dark:border-gray-600 dark:text-gray-300"
      >
        <option value="">{{ t('common.all') }}</option>
        <option v-for="opt in semesterOptions" :key="opt.academicYear + '-' + opt.semester" :value="opt.academicYear + '-' + opt.semester">
          {{ opt.label }}
        </option>
      </select>
    </div>

    <div class="grid gap-4 grid-cols-2 lg:grid-cols-3 xl:grid-cols-6">
      <Card class="py-4 dark:border-gray-700 dark:bg-gray-800">
        <CardContent>
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-[#64748b] dark:text-gray-400">{{ t('overview.teachingCourses') }}</p>
              <p class="mt-1 text-2xl font-semibold text-[#0f172a] dark:text-white">{{ statsData?.courseCount ?? 0 }}</p>
            </div>
            <div class="grid size-10 place-items-center rounded-md bg-[#f0fdf4] dark:bg-green-900/30">
              <BookOpen class="size-5 text-[#15803d] dark:text-green-400" />
            </div>
          </div>
        </CardContent>
      </Card>

      <Card class="py-4 dark:border-gray-700 dark:bg-gray-800">
        <CardContent>
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-[#64748b] dark:text-gray-400">{{ t('overview.teachingStudents') }}</p>
              <p class="mt-1 text-2xl font-semibold text-[#0f172a] dark:text-white">{{ statsData?.studentCount ?? 0 }}</p>
            </div>
            <div class="grid size-10 place-items-center rounded-md bg-[#eff6ff] dark:bg-blue-900/30">
              <Users class="size-5 text-[#2563eb] dark:text-blue-400" />
            </div>
          </div>
        </CardContent>
      </Card>

      <Card class="py-4 dark:border-gray-700 dark:bg-gray-800">
        <CardContent>
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-[#64748b] dark:text-gray-400">{{ t('score.average') }}</p>
              <p class="mt-1 text-2xl font-semibold text-[#0f172a] dark:text-white">{{ statsData?.averageScore ?? 0 }}</p>
            </div>
            <div class="grid size-10 place-items-center rounded-md bg-[#ccfbf1] dark:bg-teal-900/30">
              <BarChart3 class="size-5 text-[#0f766e] dark:text-teal-400" />
            </div>
          </div>
        </CardContent>
      </Card>

      <Card class="py-4 dark:border-gray-700 dark:bg-gray-800">
        <CardContent>
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-[#64748b] dark:text-gray-400">{{ t('overview.failingStudents') }}</p>
              <p class="mt-1 text-2xl font-semibold text-[#dc2626] dark:text-red-400">{{ statsData?.failingCount ?? 0 }}</p>
            </div>
            <div class="grid size-10 place-items-center rounded-md bg-[#fef2f2] dark:bg-red-900/30">
              <XCircle class="size-5 text-[#dc2626] dark:text-red-400" />
            </div>
          </div>
        </CardContent>
      </Card>

      <Card class="py-4 dark:border-gray-700 dark:bg-gray-800">
        <CardContent>
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-[#64748b] dark:text-gray-400">{{ t('overview.maxScore') }}</p>
              <p class="mt-1 text-2xl font-semibold text-[#0f172a] dark:text-white">{{ statsData?.maxScore ?? 0 }}</p>
            </div>
            <div class="grid size-10 place-items-center rounded-md bg-[#fef3c7] dark:bg-orange-900/30">
              <Award class="size-5 text-[#b45309] dark:text-orange-400" />
            </div>
          </div>
        </CardContent>
      </Card>

      <Card class="py-4 dark:border-gray-700 dark:bg-gray-800">
        <CardContent>
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-[#64748b] dark:text-gray-400">{{ t('overview.passRate') }}</p>
              <p class="mt-1 text-2xl font-semibold text-[#15803d] dark:text-green-400">{{ statsData?.passRate ?? 0 }}%</p>
            </div>
            <div class="grid size-10 place-items-center rounded-md bg-[#f0fdf4] dark:bg-green-900/30">
              <CheckCircle class="size-5 text-[#15803d] dark:text-green-400" />
            </div>
          </div>
        </CardContent>
      </Card>
    </div>

    <div class="grid gap-6 lg:grid-cols-3">
      <Card class="lg:col-span-2 py-4 dark:border-gray-700 dark:bg-gray-800">
        <CardContent>
          <div class="mb-4 flex items-center justify-between">
            <h2 class="text-sm font-semibold text-[#0f172a] dark:text-white">{{ t('overview.scoreDistribution') }}</h2>
            <select
              v-model="selectedCourseId"
              class="h-8 rounded-md border border-[#e2e8f0] bg-white px-2.5 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75] dark:bg-gray-700 dark:border-gray-600 dark:text-gray-300"
            >
              <option :value="undefined">{{ t('common.pleaseSelect') }}</option>
              <option v-for="c in courseOptions" :key="c.id" :value="c.id">
                {{ c.courseName }}
              </option>
            </select>
          </div>
          <div ref="chartRef" class="h-72 w-full"></div>
        </CardContent>
      </Card>

      <Card class="py-4 dark:border-gray-700 dark:bg-gray-800">
        <CardContent>
          <h2 class="mb-3 text-sm font-semibold text-[#0f172a] dark:text-white">{{ t('overview.recentRecords') }}</h2>
          <div class="space-y-2">
            <div
              v-for="record in recentRecords"
              :key="record.id"
              class="flex items-center justify-between rounded-md border border-[#f1f5f9] p-2.5 hover:bg-[#f8fafc] dark:border-gray-700 dark:hover:bg-gray-700"
            >
              <div class="min-w-0 flex-1">
                <p class="truncate text-sm font-medium text-[#475569] dark:text-gray-300">{{ record.studentName }} - {{ record.courseName }}</p>
                <p class="text-xs text-[#64748b] dark:text-gray-400">{{ formatDate(record.createTime) }} · {{ record.examType }}</p>
              </div>
              <span class="ml-2 shrink-0 text-sm font-semibold text-[#0f172a] dark:text-white">{{ record.scoreValue }}</span>
            </div>
            <div v-if="recentRecords.length === 0" class="py-8 text-center text-sm text-[#94a3b8] dark:text-gray-500">
              {{ t('common.noData') }}
            </div>
          </div>
        </CardContent>
      </Card>
    </div>

    <div class="grid gap-6 lg:grid-cols-2">
      <Card class="py-4 dark:border-gray-700 dark:bg-gray-800">
        <CardContent>
          <div class="mb-3 flex items-center justify-between">
            <h2 class="text-sm font-semibold text-[#0f172a] dark:text-white">{{ t('ranking.subjectTop5') }}</h2>
            <select
              v-model="selectedCourseId"
              class="h-7 rounded-md border border-[#e2e8f0] bg-white px-2 text-xs text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75] dark:bg-gray-700 dark:border-gray-600 dark:text-gray-300"
            >
              <option :value="undefined">{{ t('common.pleaseSelect') }}</option>
              <option v-for="c in courseOptions" :key="c.id" :value="c.id">
                {{ c.courseName }}
              </option>
            </select>
          </div>
          <table class="w-full">
            <thead>
              <tr class="border-b border-[#e2e8f0] dark:border-gray-700">
                <th class="pb-2 text-left text-xs font-medium text-[#64748b] dark:text-gray-400">{{ t('ranking.rank') }}</th>
                <th class="pb-2 text-left text-xs font-medium text-[#64748b] dark:text-gray-400">{{ t('student.name') }}</th>
                <th class="pb-2 text-left text-xs font-medium text-[#64748b] dark:text-gray-400">{{ t('student.class') }}</th>
                <th class="pb-2 text-left text-xs font-medium text-[#64748b] dark:text-gray-400">{{ t('score.score') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="student in top5Subject"
                :key="student.rank"
                class="border-b border-[#f8fafc] last:border-0 dark:border-gray-700"
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
                <td class="py-2.5 text-sm font-medium text-[#475569] dark:text-gray-300">{{ student.name }}</td>
                <td class="py-2.5 text-sm text-[#64748b] dark:text-gray-400">{{ student.className }}</td>
                <td class="py-2.5 text-sm font-semibold text-[#0f172a] dark:text-white">{{ student.score }}</td>
              </tr>
            </tbody>
          </table>
          <div v-if="top5Subject.length === 0" class="py-6 text-center text-sm text-[#94a3b8] dark:text-gray-500">
            {{ t('ranking.selectCourse') }}
          </div>
        </CardContent>
      </Card>

      <Card class="py-4 dark:border-gray-700 dark:bg-gray-800">
        <CardContent>
          <div class="mb-3 flex items-center justify-between">
            <h2 class="text-sm font-semibold text-[#0f172a] dark:text-white">{{ t('ranking.totalTop5') }}</h2>
          </div>
          <table class="w-full">
            <thead>
              <tr class="border-b border-[#e2e8f0] dark:border-gray-700">
                <th class="pb-2 text-left text-xs font-medium text-[#64748b] dark:text-gray-400">{{ t('ranking.rank') }}</th>
                <th class="pb-2 text-left text-xs font-medium text-[#64748b] dark:text-gray-400">{{ t('student.name') }}</th>
                <th class="pb-2 text-left text-xs font-medium text-[#64748b] dark:text-gray-400">{{ t('student.class') }}</th>
                <th class="pb-2 text-left text-xs font-medium text-[#64748b] dark:text-gray-400">{{ t('score.score') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="student in top5Total"
                :key="student.rank"
                class="border-b border-[#f8fafc] last:border-0 dark:border-gray-700"
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
                <td class="py-2.5 text-sm font-medium text-[#475569] dark:text-gray-300">{{ student.name }}</td>
                <td class="py-2.5 text-sm text-[#64748b] dark:text-gray-400">{{ student.className }}</td>
                <td class="py-2.5 text-sm font-semibold text-[#0f172a] dark:text-white">{{ student.score }}</td>
              </tr>
            </tbody>
          </table>
          <div v-if="top5Total.length === 0" class="py-6 text-center text-sm text-[#94a3b8] dark:text-gray-500">
            {{ t('common.noData') }}
          </div>
        </CardContent>
      </Card>
    </div>

    <Card class="py-4 dark:border-gray-700 dark:bg-gray-800">
      <CardContent>
        <div class="mb-3 flex items-center gap-2">
          <AlertTriangle class="size-4 text-[#b45309] dark:text-orange-400" />
          <h2 class="text-sm font-semibold text-[#0f172a] dark:text-white">{{ t('warning.scoreWarning') }}</h2>
        </div>
        <div class="grid gap-4 grid-cols-2 lg:grid-cols-3">
          <div
            v-for="item in warningData"
            :key="item.type"
            class="rounded-md border border-[#e2e8f0] p-3 dark:border-gray-700"
          >
            <p class="text-xs text-[#64748b] dark:text-gray-400">{{ item.type }}</p>
            <p :class="['mt-1 text-xl font-semibold', getWarningColor(item.type)]">{{ item.count }} 人</p>
          </div>
        </div>
        <div v-if="warningData.length === 0" class="py-6 text-center text-sm text-[#94a3b8] dark:text-gray-500">
          {{ t('common.noData') }}
        </div>
      </CardContent>
    </Card>
  </div>
</template>
