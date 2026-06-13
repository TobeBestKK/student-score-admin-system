<script setup lang="ts">
import {
  AlertTriangle,
  BarChart3,
  TrendingUp,
  Trophy,
  User,
  Award,
  CreditCard,
  XCircle,
  Target,
  Users,
} from "@lucide/vue"
import { ref, onMounted, watch, nextTick, computed } from "vue"
import * as echarts from "echarts"
import {
  fetchSemesterOptions,
  fetchStudentProfile,
  fetchStudentScores,
  fetchStudentStats,
  fetchScoreTrend,
  fetchRadarData,
  type SemesterOption,
  type StudentProfile,
  type StudentCourseScore,
  type StudentStats,
  type ScoreTrend,
  type RadarChart,
} from "../../api/dashboard"

const userInfo = ref<{
  id: number
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

const semesterOptions = ref<SemesterOption[]>([])
const selectedSemester = ref("")
const selectedExamType = ref("")

const profile = ref<StudentProfile | null>(null)
const scores = ref<StudentCourseScore[]>([])
const stats = ref<StudentStats | null>(null)
const trend = ref<ScoreTrend | null>(null)
const radarData = ref<RadarChart | null>(null)

const radarChartRef = ref<HTMLElement | null>(null)
const trendChartRef = ref<HTMLElement | null>(null)
let radarChart: echarts.ECharts | null = null
let trendChart: echarts.ECharts | null = null
const radarSelectedSeries = ref<string[]>(["个人成绩", "班级平均", "年级平均"])
const trendSelectedSeries = ref<string[]>(["个人成绩", "班级平均", "年级平均"])

const examTypeOptions = [
  { value: "", label: "全部类型" },
  { value: "期中", label: "期中" },
  { value: "期末", label: "期末" },
]

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

async function loadProfile() {
  if (!userInfo.value?.id) return
  try {
    profile.value = await fetchStudentProfile(userInfo.value.id)
  } catch (e) {
    console.error("Failed to load profile", e)
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

async function loadScoresAndStats() {
  if (!userInfo.value?.id) return
  const semesterParams = getSemesterParams()
  const params = {
    studentId: userInfo.value.id,
    ...semesterParams,
    examType: selectedExamType.value || undefined,
  }

  try {
    const [scoresData, statsData, radarDataRes] = await Promise.all([
      fetchStudentScores(params),
      fetchStudentStats(params),
      fetchRadarData(params),
    ])
    scores.value = scoresData
    stats.value = statsData
    radarData.value = radarDataRes
    await nextTick()
    renderRadarChart()
  } catch (e) {
    console.error("Failed to load scores and stats", e)
  }
}

async function loadTrend() {
  if (!userInfo.value?.id) return
  try {
    trend.value = await fetchScoreTrend(userInfo.value.id)
    await nextTick()
    renderTrendChart()
  } catch (e) {
    console.error("Failed to load trend", e)
  }
}

function renderRadarChart() {
  if (!radarChartRef.value || !radarData.value || radarData.value.courseNames.length === 0) return

  if (!radarChart) {
    radarChart = echarts.init(radarChartRef.value)
  }

  const indicators = radarData.value.courseNames.map((name) => ({
    name: name,
    max: 100,
  }))

  const allSeries = [
    {
      value: radarData.value.studentScores,
      name: "个人成绩",
      lineStyle: { color: "#155e75", width: 2 },
      areaStyle: { color: "rgba(21, 94, 117, 0.15)" },
      itemStyle: { color: "#155e75" },
    },
    {
      value: radarData.value.classAverages,
      name: "班级平均",
      lineStyle: { color: "#15803d", width: 2, type: "dashed" as const },
      areaStyle: { color: "rgba(21, 128, 61, 0.08)" },
      itemStyle: { color: "#15803d" },
    },
    {
      value: radarData.value.gradeAverages,
      name: "年级平均",
      lineStyle: { color: "#b45309", width: 2, type: "dashed" as const },
      areaStyle: { color: "rgba(180, 83, 9, 0.08)" },
      itemStyle: { color: "#b45309" },
    },
  ]

  const filteredSeries = allSeries.filter((s) => radarSelectedSeries.value.includes(s.name))
  const legendData = filteredSeries.map((s) => s.name)

  radarChart.setOption({
    tooltip: { trigger: "item" },
    legend: {
      data: legendData,
      bottom: 0,
      textStyle: { color: "#475569", fontSize: 12 },
    },
    radar: {
      indicator: indicators,
      shape: "polygon",
      radius: "85%",
      splitNumber: 5,
      axisName: { color: "#475569", fontSize: 12 },
      splitLine: { lineStyle: { color: "#e2e8f0" } },
      splitArea: { show: true, areaStyle: { color: ["#f8fafc", "#f1f5f9"] } },
      axisLine: { lineStyle: { color: "#e2e8f0" } },
    },
    series: [{ type: "radar", data: filteredSeries }],
  }, true)
}

function renderTrendChart() {
  if (!trendChartRef.value || !trend.value || trend.value.examLabels.length === 0) return

  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }

  const allSeries = [
    {
      name: "个人成绩",
      type: "line" as const,
      data: trend.value.studentScores,
      lineStyle: { color: "#155e75", width: 2 },
      itemStyle: { color: "#155e75" },
      symbol: "circle",
      symbolSize: 6,
    },
    {
      name: "班级平均",
      type: "line" as const,
      data: trend.value.classAverages,
      lineStyle: { color: "#15803d", width: 2, type: "dashed" as const },
      itemStyle: { color: "#15803d" },
      symbol: "diamond",
      symbolSize: 6,
    },
    {
      name: "年级平均",
      type: "line" as const,
      data: trend.value.gradeAverages,
      lineStyle: { color: "#b45309", width: 2, type: "dashed" as const },
      itemStyle: { color: "#b45309" },
      symbol: "triangle",
      symbolSize: 6,
    },
  ]

  const filteredSeries = allSeries.filter((s) => trendSelectedSeries.value.includes(s.name))
  const legendData = filteredSeries.map((s) => s.name)

  trendChart.setOption({
    tooltip: {
      trigger: "axis",
      axisPointer: { type: "cross" },
    },
    legend: {
      data: legendData,
      top: 0,
      textStyle: { color: "#475569", fontSize: 12 },
    },
    grid: {
      left: "3%",
      right: "4%",
      bottom: "3%",
      top: "15%",
      containLabel: true,
    },
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
    series: filteredSeries,
  }, true)
}

const warnings = computed(() => {
  return scores.value
    .filter((s) => s.scoreValue < 40)
    .map((s) => ({
      courseName: s.courseName,
      examType: s.examType,
      score: s.scoreValue,
      credit: s.credit,
      level: s.scoreValue < 60 ? "fail" : "warning",
      message: s.scoreValue < 60 ? "不及格，需补考" : "低分预警，建议加强",
    }))
})

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

onMounted(async () => {
  await loadSemesters()
  await loadProfile()
  await loadScoresAndStats()
  await loadTrend()

  window.addEventListener("resize", () => {
    radarChart?.resize()
    trendChart?.resize()
  })
})

watch(selectedSemester, () => {
  loadScoresAndStats()
})

watch(selectedExamType, () => {
  loadScoresAndStats()
})

watch(radarSelectedSeries, () => {
  renderRadarChart()
}, { deep: true })

watch(trendSelectedSeries, () => {
  renderTrendChart()
}, { deep: true })
</script>

<template>
  <div class="space-y-6">
    <div class="mb-6 flex items-center gap-4">
      <div class="flex items-center gap-2">
        <label class="text-sm font-medium text-[#475569]">学期</label>
        <select
          v-model="selectedSemester"
          class="h-9 rounded-md border border-[#e2e8f0] bg-white px-3 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75]"
        >
          <option value="">全部学期</option>
          <option
            v-for="opt in semesterOptions"
            :key="opt.academicYear + '-' + opt.semester"
            :value="opt.academicYear + '-' + opt.semester"
          >
            {{ opt.label }}
          </option>
        </select>
      </div>
      <div class="flex items-center gap-2">
        <label class="text-sm font-medium text-[#475569]">考试类型</label>
        <select
          v-model="selectedExamType"
          class="h-9 rounded-md border border-[#e2e8f0] bg-white px-3 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75]"
        >
          <option
            v-for="opt in examTypeOptions"
            :key="opt.value"
            :value="opt.value"
          >
            {{ opt.label }}
          </option>
        </select>
      </div>
    </div>

    <div v-if="profile" class="rounded-lg border border-[#e2e8f0] bg-white p-4">
      <div class="flex items-center gap-6">
        <div class="grid size-14 place-items-center rounded-full bg-[#ccfbf1] text-[#0f766e]">
          <User class="size-7" />
        </div>
        <div class="flex flex-1 items-center gap-8">
          <div>
            <p class="text-xs text-[#64748b]">姓名</p>
            <p class="text-sm font-semibold text-[#0f172a]">{{ profile.name }}</p>
          </div>
          <div>
            <p class="text-xs text-[#64748b]">学号</p>
            <p class="text-sm font-semibold text-[#0f172a]">{{ profile.studentNo }}</p>
          </div>
          <div>
            <p class="text-xs text-[#64748b]">班级</p>
            <p class="text-sm font-semibold text-[#0f172a]">{{ profile.className }}</p>
          </div>
          <div>
            <p class="text-xs text-[#64748b]">专业</p>
            <p class="text-sm font-semibold text-[#0f172a]">{{ profile.major }}</p>
          </div>
          <div>
            <p class="text-xs text-[#64748b]">入学年份</p>
            <p class="text-sm font-semibold text-[#0f172a]">{{ profile.enrollmentYear }}</p>
          </div>
        </div>
      </div>
    </div>

    <div class="grid gap-4 grid-cols-2 lg:grid-cols-3 xl:grid-cols-6">
      <div class="rounded-lg border border-[#e2e8f0] bg-white p-4">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-[#64748b]">总分</p>
            <p class="mt-1 text-2xl font-semibold text-[#0f172a]">{{ stats?.totalScore ?? 0 }}</p>
          </div>
          <div class="grid size-10 place-items-center rounded-md bg-[#ccfbf1]">
            <Award class="size-5 text-[#0f766e]" />
          </div>
        </div>
      </div>

      <div class="rounded-lg border border-[#e2e8f0] bg-white p-4">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-[#64748b]">平均分</p>
            <p class="mt-1 text-2xl font-semibold text-[#0f172a]">{{ stats?.averageScore ?? 0 }}</p>
          </div>
          <div class="grid size-10 place-items-center rounded-md bg-[#e0f2fe]">
            <BarChart3 class="size-5 text-[#155e75]" />
          </div>
        </div>
      </div>

      <div class="rounded-lg border border-[#e2e8f0] bg-white p-4">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-[#64748b]">已获学分</p>
            <p class="mt-1 text-2xl font-semibold text-[#15803d]">{{ stats?.earnedCredit ?? 0 }}</p>
          </div>
          <div class="grid size-10 place-items-center rounded-md bg-[#f0fdf4]">
            <CreditCard class="size-5 text-[#15803d]" />
          </div>
        </div>
      </div>

      <div class="rounded-lg border border-[#e2e8f0] bg-white p-4">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-[#64748b]">不及格</p>
            <p class="mt-1 text-2xl font-semibold text-[#dc2626]">{{ stats?.failCount ?? 0 }} 门</p>
          </div>
          <div class="grid size-10 place-items-center rounded-md bg-[#fef2f2]">
            <XCircle class="size-5 text-[#dc2626]" />
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
            <p class="text-sm text-[#64748b]">年级排名</p>
            <p class="mt-1 text-2xl font-semibold text-[#b45309]">
              {{ stats?.gradeRank ?? 0 }}<span class="text-sm text-[#94a3b8]">/{{ stats?.gradeTotal ?? 0 }}</span>
            </p>
          </div>
          <div class="grid size-10 place-items-center rounded-md bg-[#fef3c7]">
            <Trophy class="size-5 text-[#b45309]" />
          </div>
        </div>
      </div>
    </div>

    <div class="grid gap-6 lg:grid-cols-2">
      <div class="rounded-lg border border-[#e2e8f0] bg-white p-4">
        <h2 class="mb-3 text-sm font-semibold text-[#0f172a]">各科成绩</h2>
        <div class="space-y-3">
          <div
            v-for="course in scores"
            :key="course.courseId"
            class="flex items-center justify-between rounded-md border border-[#f1f5f9] p-3"
          >
            <div class="flex items-center gap-3">
              <div
                :class="[
                  'grid size-10 place-items-center rounded-md text-sm font-semibold',
                  getScoreBg(course.scoreValue),
                  getScoreColor(course.scoreValue),
                ]"
              >
                {{ course.scoreValue }}
              </div>
              <div>
                <p class="text-sm font-medium text-[#475569]">{{ course.courseName }}</p>
                <p class="text-xs text-[#64748b]">{{ course.examType }} · {{ course.credit }} 学分</p>
              </div>
            </div>
            <div class="flex items-center gap-4">
              <div class="text-right">
                <p class="text-xs text-[#155e75]">班级</p>
                <p class="text-sm font-medium text-[#155e75]">
                  {{ course.classRank }}<span class="text-[#94a3b8]">/{{ course.classTotal }}</span>
                </p>
              </div>
              <div class="text-right">
                <p class="text-xs text-[#b45309]">年级</p>
                <p class="text-sm font-medium text-[#b45309]">
                  {{ course.gradeRank }}<span class="text-[#94a3b8]">/{{ course.gradeTotal }}</span>
                </p>
              </div>
            </div>
          </div>
          <div v-if="scores.length === 0" class="py-8 text-center text-sm text-[#94a3b8]">
            暂无成绩数据
          </div>
        </div>
      </div>

      <div class="rounded-lg border border-[#e2e8f0] bg-white p-4">
        <div class="mb-3 flex items-center justify-between">
          <h2 class="text-sm font-semibold text-[#0f172a]">各科成绩雷达图</h2>
          <div class="flex items-center gap-4">
            <label class="flex cursor-pointer items-center gap-1.5 text-xs">
              <input type="checkbox" value="个人成绩" v-model="radarSelectedSeries" class="size-3.5 rounded border-[#e2e8f0]" />
              <span class="size-2 rounded-full bg-[#155e75]"></span>
              <span class="text-[#475569]">个人成绩</span>
            </label>
            <label class="flex cursor-pointer items-center gap-1.5 text-xs">
              <input type="checkbox" value="班级平均" v-model="radarSelectedSeries" class="size-3.5 rounded border-[#e2e8f0]" />
              <span class="size-2 rounded-full bg-[#15803d]"></span>
              <span class="text-[#475569]">班级平均</span>
            </label>
            <label class="flex cursor-pointer items-center gap-1.5 text-xs">
              <input type="checkbox" value="年级平均" v-model="radarSelectedSeries" class="size-3.5 rounded border-[#e2e8f0]" />
              <span class="size-2 rounded-full bg-[#b45309]"></span>
              <span class="text-[#475569]">年级平均</span>
            </label>
          </div>
        </div>
        <div v-if="radarData && radarData.courseNames.length > 0" ref="radarChartRef" class="h-[500px] w-full"></div>
        <div v-else class="flex h-[500px] items-center justify-center">
          <div class="text-center">
            <Target class="mx-auto size-10 text-[#cbd5e1]" />
            <p class="mt-2 text-sm text-[#64748b]">暂无数据</p>
          </div>
        </div>
      </div>
    </div>

    <div class="rounded-lg border border-[#e2e8f0] bg-white p-4">
      <div class="mb-3 flex items-center justify-between">
        <h2 class="text-sm font-semibold text-[#0f172a]">成绩趋势</h2>
        <div class="flex items-center gap-4">
          <label class="flex cursor-pointer items-center gap-1.5 text-xs">
            <input type="checkbox" value="个人成绩" v-model="trendSelectedSeries" class="size-3.5 rounded border-[#e2e8f0]" />
            <span class="size-2 rounded-full bg-[#155e75]"></span>
            <span class="text-[#475569]">个人成绩</span>
          </label>
          <label class="flex cursor-pointer items-center gap-1.5 text-xs">
            <input type="checkbox" value="班级平均" v-model="trendSelectedSeries" class="size-3.5 rounded border-[#e2e8f0]" />
            <span class="size-2 rounded-full bg-[#15803d]"></span>
            <span class="text-[#475569]">班级平均</span>
          </label>
          <label class="flex cursor-pointer items-center gap-1.5 text-xs">
            <input type="checkbox" value="年级平均" v-model="trendSelectedSeries" class="size-3.5 rounded border-[#e2e8f0]" />
            <span class="size-2 rounded-full bg-[#b45309]"></span>
            <span class="text-[#475569]">年级平均</span>
          </label>
        </div>
      </div>
      <div v-if="trend && trend.examLabels.length > 0" ref="trendChartRef" class="h-80 w-full"></div>
      <div v-else class="flex h-80 items-center justify-center">
        <div class="text-center">
          <TrendingUp class="mx-auto size-10 text-[#cbd5e1]" />
          <p class="mt-2 text-sm text-[#64748b]">暂无趋势数据</p>
        </div>
      </div>
    </div>

    <div v-if="stats?.creditWarning" 
         class="rounded-lg border border-[#fed7aa] bg-[#fff7ed] p-4">
      <div class="flex items-center gap-2">
        <AlertTriangle class="size-4 text-[#ea580c]" />
        <h2 class="text-sm font-semibold text-[#9a3412]">学分不足预警</h2>
      </div>
      <p class="mt-2 text-sm text-[#9a3412]">
        当前已获学分：<span class="font-semibold">{{ stats.earnedCredit }}</span> / 
        总学分 <span class="font-semibold">{{ stats.totalCredit }}</span>
        （不足 60%）
      </p>
    </div>

    <div v-if="warnings.length > 0" class="rounded-lg border border-[#e2e8f0] bg-white p-4">
      <div class="mb-3 flex items-center gap-2">
        <AlertTriangle class="size-4 text-[#b45309]" />
        <h2 class="text-sm font-semibold text-[#0f172a]">学业预警</h2>
      </div>
      <div class="space-y-2">
        <div
          v-for="(item, index) in warnings"
          :key="index"
          :class="[
            'flex items-center justify-between rounded-md p-3',
            item.level === 'fail' ? 'bg-[#fef2f2]' : 'bg-[#fef3c7]',
          ]"
        >
          <div class="flex items-center gap-3">
            <AlertTriangle
              :class="[
                'size-4 shrink-0',
                item.level === 'fail' ? 'text-[#dc2626]' : 'text-[#b45309]',
              ]"
            />
            <div>
              <p class="text-sm font-medium text-[#475569]">
                {{ item.courseName }}
                <span class="text-xs text-[#94a3b8]">· {{ item.examType }}</span>
              </p>
              <p
                :class="[
                  'text-xs',
                  item.level === 'fail' ? 'text-[#dc2626]' : 'text-[#b45309]',
                ]"
              >
                {{ item.message }}
              </p>
            </div>
          </div>
          <div class="text-right">
            <p
              :class="[
                'text-lg font-semibold',
                item.level === 'fail' ? 'text-[#dc2626]' : 'text-[#b45309]',
              ]"
            >
              {{ item.score }} 分
            </p>
            <p class="text-xs text-[#94a3b8]">{{ item.credit }} 学分</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
