<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick, computed } from "vue"
import * as echarts from "echarts"
import {
  TrendingUp,
  TrendingDown,
  BarChart3,
  ArrowUpRight,
  ArrowDownRight,
  Minus,
  Users,
} from "@lucide/vue"
import {
  fetchSemesterOptions,
  fetchStudentStats,
  fetchScoreTrend,
  fetchScoreTrendDetail,
  type SemesterOption,
  type StudentStats,
  type ScoreTrend,
  type ScoreTrendDetail,
} from "@/api/dashboard"

const userInfo = ref<{ id: number; name: string } | null>(null)
try {
  const stored = localStorage.getItem("userInfo")
  if (stored) userInfo.value = JSON.parse(stored)
} catch (e) { /* ignore */ }

const semesterOptions = ref<SemesterOption[]>([])
const selectedSemester = ref("")
const selectedExamType = ref("")

const stats = ref<StudentStats | null>(null)
const trend = ref<ScoreTrend | null>(null)
const trendDetail = ref<ScoreTrendDetail | null>(null)
const loading = ref(false)

const trendChartRef = ref<HTMLElement | null>(null)
const barChartRef = ref<HTMLElement | null>(null)
let trendChart: echarts.ECharts | null = null
let barChart: echarts.ECharts | null = null

const examTypeOptions = [
  { value: "", label: "全部类型" },
  { value: "期中", label: "期中" },
  { value: "期末", label: "期末" },
]

function getSemesterParams() {
  if (!selectedSemester.value) return {}
  const parts = selectedSemester.value.split("-")
  if (parts.length >= 3) {
    return { academicYear: parts[0] + "-" + parts[1], semester: parts[2] }
  }
  return {}
}

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

async function loadData() {
  if (!userInfo.value?.id) return
  loading.value = true

  // 先清理旧图表
  trendChart?.dispose()
  barChart?.dispose()
  trendChart = null
  barChart = null

  const semesterParams = getSemesterParams()
  const params = {
    studentId: userInfo.value.id,
    ...semesterParams,
    examType: selectedExamType.value || undefined,
  }

  try {
    const [statsData, trendData, detailRes] = await Promise.all([
      fetchStudentStats(params),
      fetchScoreTrend(userInfo.value.id, semesterParams.academicYear, semesterParams.semester, selectedExamType.value || undefined),
      fetchScoreTrendDetail(userInfo.value.id, semesterParams.academicYear, semesterParams.semester, selectedExamType.value || undefined),
    ])
    stats.value = statsData
    trend.value = trendData
    trendDetail.value = detailRes
  } catch (e) {
    console.error("Failed to load trend data", e)
  } finally {
    // 先关闭 loading，让图表容器在 DOM 中显示出来
    loading.value = false

    // 等 DOM 更新完成（容器变为可见）
    await nextTick()

    // 容器可见后再渲染图表
    renderTrendChart()
    renderBarChart()
  }
}

function renderTrendChart() {
  if (!trendChartRef.value || !trend.value || trend.value.examLabels.length === 0) return
  if (!trendChart) trendChart = echarts.init(trendChartRef.value)

  trendChart.setOption({
    tooltip: { trigger: "axis", axisPointer: { type: "cross" } },
    legend: {
      data: ["个人成绩", "班级平均", "年级平均"],
      top: 0,
      textStyle: { color: "#475569", fontSize: 12 },
    },
    grid: { left: "3%", right: "4%", bottom: "3%", top: "15%", containLabel: true },
    xAxis: {
      type: "category",
      data: trend.value.examLabels,
      axisLine: { lineStyle: { color: "#e2e8f0" } },
      axisLabel: { color: "#64748b", fontSize: 11, rotate: 30 },
    },
    yAxis: {
      type: "value",
      min: 0,
      max: 100,
      axisLine: { show: false },
      splitLine: { lineStyle: { color: "#f1f5f9" } },
      axisLabel: { color: "#64748b", fontSize: 12 },
    },
    series: [
      {
        name: "个人成绩",
        type: "line",
        data: trend.value.studentScores,
        lineStyle: { color: "#155e75", width: 3 },
        itemStyle: { color: "#155e75" },
        symbol: "circle",
        symbolSize: 8,
        smooth: true,
      },
      {
        name: "班级平均",
        type: "line",
        data: trend.value.classAverages,
        lineStyle: { color: "#0f766e", width: 2, type: "dashed" },
        itemStyle: { color: "#0f766e" },
        symbol: "diamond",
        symbolSize: 6,
      },
      {
        name: "年级平均",
        type: "line",
        data: trend.value.gradeAverages,
        lineStyle: { color: "#ca8a04", width: 2, type: "dashed" },
        itemStyle: { color: "#ca8a04" },
        symbol: "triangle",
        symbolSize: 6,
      },
    ],
  })
}

function renderBarChart() {
  if (!barChartRef.value || !trendDetail.value || trendDetail.value.courseTrends.length === 0) return
  if (!barChart) barChart = echarts.init(barChartRef.value)

  const courses = trendDetail.value.courseTrends
  const courseNames: string[] = []
  const latestScores: number[] = []
  const prevScores: number[] = []
  const barColors: string[] = []

  for (const c of courses) {
    if (c.exams.length >= 2) {
      courseNames.push(c.courseName)
      latestScores.push(Number(c.exams[c.exams.length - 1].score))
      prevScores.push(Number(c.exams[c.exams.length - 2].score))
      barColors.push(c.trendDirection === "上升" ? "#15803d" : c.trendDirection === "下降" ? "#dc2626" : "#64748b")
    } else if (c.exams.length === 1) {
      courseNames.push(c.courseName)
      latestScores.push(Number(c.exams[0].score))
      prevScores.push(0)
      barColors.push("#64748b")
    }
  }

  barChart.setOption({
    tooltip: { trigger: "axis", axisPointer: { type: "shadow" } },
    legend: {
      data: ["最近成绩", "上次成绩"],
      top: 0,
      textStyle: { color: "#475569", fontSize: 12 },
    },
    grid: { left: "3%", right: "4%", bottom: "3%", top: "15%", containLabel: true },
    xAxis: {
      type: "category",
      data: courseNames,
      axisLine: { lineStyle: { color: "#e2e8f0" } },
      axisLabel: { color: "#64748b", fontSize: 12 },
    },
    yAxis: {
      type: "value",
      min: 0,
      max: 100,
      axisLine: { show: false },
      splitLine: { lineStyle: { color: "#f1f5f9" } },
      axisLabel: { color: "#64748b", fontSize: 12 },
    },
    series: [
      {
        name: "上次成绩",
        type: "bar",
        data: prevScores,
        barWidth: "30%",
        itemStyle: { color: "#e2e8f0", borderRadius: [4, 4, 0, 0] },
      },
      {
        name: "最近成绩",
        type: "bar",
        data: latestScores,
        barWidth: "30%",
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

const improvedCourses = computed(() => {
  if (!trendDetail.value) return []
  return trendDetail.value.courseTrends.filter(c => c.trendDirection === "上升")
})

const declinedCourses = computed(() => {
  if (!trendDetail.value) return []
  return trendDetail.value.courseTrends.filter(c => c.trendDirection === "下降")
})

const bestImprovement = computed(() => {
  if (!trendDetail.value || trendDetail.value.courseTrends.length === 0) return null
  return trendDetail.value.courseTrends.reduce((best, c) =>
    c.latestChange > best.latestChange ? c : best
  , trendDetail.value.courseTrends[0])
})

const worstDecline = computed(() => {
  if (!trendDetail.value || trendDetail.value.courseTrends.length === 0) return null
  return trendDetail.value.courseTrends.reduce((worst, c) =>
    c.latestChange < worst.latestChange ? c : worst
  , trendDetail.value.courseTrends[0])
})

const handleResize = () => {
  trendChart?.resize()
  barChart?.resize()
}

onMounted(async () => {
  await loadSemesters()
  await loadData()
  window.addEventListener("resize", handleResize)
})

onUnmounted(() => {
  window.removeEventListener("resize", handleResize)
})

watch([selectedSemester, selectedExamType], () => {
  loadData()
})
</script>

<template>
  <div class="space-y-6">
    <!-- 筛选区 -->
    <div class="flex flex-wrap items-end gap-4">
      <div class="flex flex-col gap-1.5">
        <label class="text-xs text-[#475569]">学期</label>
        <select
          v-model="selectedSemester"
          class="h-10 rounded-md border border-[#e2e8f0] bg-white px-3 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75]"
        >
          <option value="">全部学期</option>
          <option v-for="opt in semesterOptions" :key="opt.academicYear + '-' + opt.semester" :value="opt.academicYear + '-' + opt.semester">
            {{ opt.label }}
          </option>
        </select>
      </div>
      <div class="flex flex-col gap-1.5">
        <label class="text-xs text-[#475569]">考试类型</label>
        <select
          v-model="selectedExamType"
          class="h-10 rounded-md border border-[#e2e8f0] bg-white px-3 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75]"
        >
          <option v-for="opt in examTypeOptions" :key="opt.value" :value="opt.value">
            {{ opt.label }}
          </option>
        </select>
      </div>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="flex items-center justify-center py-20">
      <div class="size-8 animate-spin rounded-full border-4 border-[#e2e8f0] border-t-[#155e75]"></div>
    </div>

    <template v-else>
      <!-- 结论层：统计卡片 -->
      <div class="grid gap-4 grid-cols-2 lg:grid-cols-4">
        <div class="rounded-lg border border-[#e2e8f0] bg-white p-4">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-[#64748b]">最近平均分</p>
              <p class="mt-1 text-2xl font-semibold text-[#0f172a]">{{ stats?.averageScore ?? 0 }}</p>
            </div>
            <div class="grid size-10 place-items-center rounded-md bg-[#ccfbf1]">
              <BarChart3 class="size-5 text-[#0f766e]" />
            </div>
          </div>
        </div>

        <div class="rounded-lg border border-[#e2e8f0] bg-white p-4">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-[#64748b]">班级排名</p>
              <p class="mt-1 text-2xl font-semibold text-[#155e75]">
                {{ stats?.classRank ?? 0 }}<span class="text-sm text-[#94a3b8]">/{{ stats?.classTotal ?? 0 }}</span>
              </p>
            </div>
            <div class="grid size-10 place-items-center rounded-md bg-[#e0f2fe]">
              <Users class="size-5 text-[#155e75]" />
            </div>
          </div>
        </div>

        <div class="rounded-lg border border-[#e2e8f0] bg-white p-4">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-[#64748b]">进步科目</p>
              <p class="mt-1 text-2xl font-semibold text-[#15803d]">{{ improvedCourses.length }} 门</p>
            </div>
            <div class="grid size-10 place-items-center rounded-md bg-[#f0fdf4]">
              <TrendingUp class="size-5 text-[#15803d]" />
            </div>
          </div>
        </div>

        <div class="rounded-lg border border-[#e2e8f0] bg-white p-4">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-[#64748b]">退步科目</p>
              <p class="mt-1 text-2xl font-semibold text-[#dc2626]">{{ declinedCourses.length }} 门</p>
            </div>
            <div class="grid size-10 place-items-center rounded-md bg-[#fef2f2]">
              <TrendingDown class="size-5 text-[#dc2626]" />
            </div>
          </div>
        </div>
      </div>

      <!-- 进步/退步提示 -->
      <div class="grid gap-4 md:grid-cols-2">
        <div v-if="bestImprovement && bestImprovement.latestChange > 0" class="rounded-lg border border-[#bbf7d0] bg-[#f0fdf4] p-4">
          <div class="flex items-center gap-2">
            <ArrowUpRight class="size-5 text-[#15803d]" />
            <span class="text-sm font-semibold text-[#15803d]">最大进步</span>
          </div>
          <p class="mt-1 text-sm text-[#166534]">
            <span class="font-medium">{{ bestImprovement.courseName }}</span>
            进步 <span class="font-semibold">+{{ bestImprovement.latestChange }}</span> 分
          </p>
        </div>
        <div v-if="worstDecline && worstDecline.latestChange < 0" class="rounded-lg border border-[#fecaca] bg-[#fef2f2] p-4">
          <div class="flex items-center gap-2">
            <ArrowDownRight class="size-5 text-[#dc2626]" />
            <span class="text-sm font-semibold text-[#dc2626]">最大退步</span>
          </div>
          <p class="mt-1 text-sm text-[#991b1b]">
            <span class="font-medium">{{ worstDecline.courseName }}</span>
            退步 <span class="font-semibold">{{ worstDecline.latestChange }}</span> 分
          </p>
        </div>
      </div>

      <!-- 趋势层：图表区 -->
      <div>
        <!-- 各科对比柱状图 -->
        <div class="rounded-lg border border-[#e2e8f0] bg-white p-4">
          <h2 class="mb-3 text-sm font-semibold text-[#0f172a]">各科成绩对比（期中 vs 期末）</h2>
          <div v-if="trendDetail && trendDetail.courseTrends.length > 0" ref="barChartRef" class="h-80 w-full"></div>
          <div v-else class="flex h-80 items-center justify-center">
            <div class="text-center">
              <BarChart3 class="mx-auto size-10 text-[#cbd5e1]" />
              <p class="mt-2 text-sm text-[#64748b]">暂无对比数据</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 明细层：各科成绩变化表 -->
      <div class="rounded-lg border border-[#e2e8f0] bg-white p-4">
        <h2 class="mb-3 text-sm font-semibold text-[#0f172a]">各科成绩变化明细</h2>
        <div v-if="trendDetail && trendDetail.courseTrends.length > 0" class="overflow-x-auto rounded-lg border border-[#e2e8f0]">
          <table class="w-full">
            <thead>
              <tr class="border-b border-[#e2e8f0] bg-[#f8fafc]">
                <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b]">课程</th>
                <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b]">最近成绩</th>
                <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b]">上次成绩</th>
                <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b]">变化幅度</th>
                <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b]">趋势</th>
                <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b]">班级平均</th>
                <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b]">年级平均</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="course in trendDetail.courseTrends"
                :key="course.courseName"
                class="border-b border-[#f1f5f9] last:border-0 hover:bg-[#f8fafc]"
              >
                <td class="px-4 py-3 text-sm font-medium text-[#0f172a]">{{ course.courseName }}</td>
                <td class="px-4 py-3">
                  <span class="text-sm font-semibold text-[#0f172a]">
                    {{ course.exams.length > 0 ? course.exams[course.exams.length - 1].score : '—' }}
                  </span>
                </td>
                <td class="px-4 py-3">
                  <span class="text-sm text-[#475569]">
                    {{ course.exams.length >= 2 ? course.exams[course.exams.length - 2].score : '—' }}
                  </span>
                </td>
                <td class="px-4 py-3">
                  <span
                    v-if="course.exams.length >= 2"
                    :class="[
                      'inline-flex items-center gap-1 rounded-md px-2 py-0.5 text-sm font-semibold',
                      course.latestChange > 0 ? 'bg-[#dcfce7] text-[#15803d]' : '',
                      course.latestChange < 0 ? 'bg-[#fef2f2] text-[#dc2626]' : '',
                      course.latestChange === 0 ? 'bg-[#f1f5f9] text-[#64748b]' : '',
                    ]"
                  >
                    <ArrowUpRight v-if="course.latestChange > 0" class="size-3.5" />
                    <ArrowDownRight v-else-if="course.latestChange < 0" class="size-3.5" />
                    <Minus v-else class="size-3.5" />
                    {{ course.latestChange > 0 ? '+' : '' }}{{ course.latestChange }}
                  </span>
                  <span v-else class="text-sm text-[#94a3b8]">—</span>
                </td>
                <td class="px-4 py-3">
                  <span
                    :class="[
                      'inline-flex items-center gap-1 text-xs font-medium',
                      course.trendDirection === '上升' ? 'text-[#15803d]' : '',
                      course.trendDirection === '下降' ? 'text-[#dc2626]' : '',
                      course.trendDirection === '持平' ? 'text-[#64748b]' : '',
                    ]"
                  >
                    <TrendingUp v-if="course.trendDirection === '上升'" class="size-3.5" />
                    <TrendingDown v-else-if="course.trendDirection === '下降'" class="size-3.5" />
                    <Minus v-else class="size-3.5" />
                    {{ course.trendDirection }}
                  </span>
                </td>
                <td class="px-4 py-3 text-sm text-[#475569]">
                  {{ course.exams.length > 0 ? course.exams[course.exams.length - 1].classAvg : '—' }}
                </td>
                <td class="px-4 py-3 text-sm text-[#475569]">
                  {{ course.exams.length > 0 ? course.exams[course.exams.length - 1].gradeAvg : '—' }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-else class="py-12 text-center text-sm text-[#94a3b8]">
          暂无成绩变化数据
        </div>
      </div>
    </template>
  </div>
</template>
