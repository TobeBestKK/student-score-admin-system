<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from "vue"
import { useRoute } from "vue-router"
import { useI18n } from "vue-i18n"
import * as echarts from "echarts"
import {
  Users,
  BarChart3,
  AlertTriangle,
  Search,
} from "@lucide/vue"
import { fetchClassInfo, fetchClassStudents, fetchClassScoreStats, type ClassInfo, type ClassStudent, type ClassScoreStats } from "@/api/class"
import { fetchClassWarnings, type StudentWarnings } from "@/api/warning"
import Card from "@/components/ui/card/Card.vue"
import CardContent from "@/components/ui/card/CardContent.vue"
import CardHeader from "@/components/ui/card/CardHeader.vue"
import CardTitle from "@/components/ui/card/CardTitle.vue"
import Button from "@/components/ui/button/Button.vue"
import Input from "@/components/ui/input/Input.vue"
import Label from "@/components/ui/label/Label.vue"
import Checkbox from "@/components/ui/checkbox/Checkbox.vue"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { WARNING_LEVEL } from "@/constants/enum"

const route = useRoute()
const { t } = useI18n()
const classId = ref(Number(route.params.id))

const classInfo = ref<ClassInfo | null>(null)
const students = ref<ClassStudent[]>([])
const scoreStats = ref<ClassScoreStats | null>(null)
const allWarnings = ref<StudentWarnings[]>([])

const studentQuery = ref({ name: "", studentNo: "", hasWarning: false })
const studentsLoading = ref(false)
const statsLoading = ref(false)
const warningsLoading = ref(false)

const chartRef = ref<HTMLElement | null>(null)
let chartInstance: echarts.ECharts | null = null

const warningSummary = computed(() => {
  const counts: Record<string, number> = { [WARNING_LEVEL.NORMAL]: 0, [WARNING_LEVEL.FOCUS]: 0, [WARNING_LEVEL.SEVERE]: 0 }
  for (const w of allWarnings.value) {
    if (w.maxLevel && counts[w.maxLevel] !== undefined) {
      counts[w.maxLevel]++
    }
  }
  return Object.entries(counts).map(([level, count]) => ({ level, count }))
})

const warningStudents = computed(() => allWarnings.value.filter(w => w.maxLevel != null))

function getWarningLevelLabel(level: string) {
  if (level === WARNING_LEVEL.SEVERE) return t('warning.severe')
  if (level === WARNING_LEVEL.FOCUS) return t('warning.focus')
  return t('warning.normal')
}

async function loadClassInfo() {
  try {
    classInfo.value = await fetchClassInfo(classId.value)
  } catch (e) {
    console.error("Failed to load class info", e)
  }
}

async function loadStudents() {
  studentsLoading.value = true
  try {
    const params: Record<string, any> = {}
    if (studentQuery.value.name) params.name = studentQuery.value.name
    if (studentQuery.value.studentNo) params.studentNo = studentQuery.value.studentNo
    if (studentQuery.value.hasWarning) params.hasWarning = true
    students.value = await fetchClassStudents(classId.value, params)
  } catch (e) {
    console.error("Failed to load students", e)
  } finally {
    studentsLoading.value = false
  }
}

async function loadScoreStats() {
  statsLoading.value = true
  try {
    scoreStats.value = await fetchClassScoreStats(classId.value)
    await nextTick()
    renderChart()
  } catch (e) {
    console.error("Failed to load score stats", e)
  } finally {
    statsLoading.value = false
  }
}

async function loadWarnings() {
  warningsLoading.value = true
  try {
    allWarnings.value = await fetchClassWarnings(classId.value)
  } catch (e) {
    console.error("Failed to load warnings", e)
  } finally {
    warningsLoading.value = false
  }
}

function renderChart() {
  if (!chartRef.value || !scoreStats.value) return

  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }

  const { labels, counts } = scoreStats.value.distribution

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

function getWarningLevelClass(level: string) {
  if (level === WARNING_LEVEL.SEVERE) return "bg-[#fef2f2] text-[#dc2626]"
  if (level === WARNING_LEVEL.FOCUS) return "bg-[#fff7ed] text-[#c2410c]"
  return "bg-[#fefce8] text-[#a16207]"
}

function getWarningLevelCardClass(level: string) {
  if (level === WARNING_LEVEL.SEVERE) return "border-[#fca5a5] bg-[#fef2f2]"
  if (level === WARNING_LEVEL.FOCUS) return "border-[#fdba74] bg-[#fff7ed]"
  return "border-[#fde68a] bg-[#fefce8]"
}

function getWarningLevelTextClass(level: string) {
  if (level === WARNING_LEVEL.SEVERE) return "text-[#dc2626]"
  if (level === WARNING_LEVEL.FOCUS) return "text-[#c2410c]"
  return "text-[#a16207]"
}

function handleTabChange(value: string | number) {
  if (value === "students") loadStudents()
  if (value === "scores") loadScoreStats()
  if (value === "warnings") loadWarnings()
}

const handleResize = () => {
  chartInstance?.resize()
}

onMounted(async () => {
  await Promise.all([loadClassInfo(), loadStudents()])

  window.addEventListener("resize", handleResize)
})

onUnmounted(() => {
  window.removeEventListener("resize", handleResize)
})

watch(() => route.params.id, (newId) => {
  if (newId) {
    classId.value = Number(newId)
    loadClassInfo()
    loadStudents()
  }
})
</script>

<template>
  <div class="space-y-6">
    <Card v-if="classInfo">
      <CardContent class="py-4">
        <div class="flex flex-wrap items-center gap-6">
          <div class="flex items-center gap-2">
            <Users class="size-5 text-[#155e75]" />
            <h1 class="text-lg font-semibold text-[#0f172a]">{{ classInfo.className }}</h1>
          </div>
          <div class="flex items-center gap-4 text-sm text-[#475569]">
            <span class="rounded-full bg-[#f1f5f9] px-2.5 py-0.5">{{ classInfo.major }}</span>
            <span class="rounded-full bg-[#f1f5f9] px-2.5 py-0.5">
              {{ classInfo.grade === 1 ? t('class.grade1') : classInfo.grade === 2 ? t('class.grade2') : t('class.grade3') }}
            </span>
            <span>{{ t('class.headTeacher') }}: {{ classInfo.headTeacherName }}</span>
            <span>{{ t('class.studentCount') }}: <strong class="text-[#0f172a]">{{ classInfo.studentCount }}</strong></span>
          </div>
        </div>
      </CardContent>
    </Card>

    <Tabs default-value="students" @update:model-value="handleTabChange">
      <TabsList class="bg-[#f1f5f9]">
        <TabsTrigger value="students" class="data-[state=active]:bg-white">
          <Users class="mr-1.5 size-4" />
          {{ t('class.studentList') }}
        </TabsTrigger>
        <TabsTrigger value="scores" class="data-[state=active]:bg-white">
          <BarChart3 class="mr-1.5 size-4" />
          {{ t('class.scoreAnalysis') }}
        </TabsTrigger>
        <TabsTrigger value="warnings" class="data-[state=active]:bg-white">
          <AlertTriangle class="mr-1.5 size-4" />
          {{ t('class.warningWorkbench') }}
        </TabsTrigger>
      </TabsList>

      <TabsContent value="students">
        <Card>
          <CardContent class="pt-6">
            <div class="mb-4 flex flex-wrap items-end gap-3">
              <div class="flex flex-col gap-1.5">
                <Label class="text-xs">{{ t('student.name') }}</Label>
                <Input v-model="studentQuery.name" :placeholder="t('student.inputName')" class="w-40" />
              </div>
              <div class="flex flex-col gap-1.5">
                <Label class="text-xs">{{ t('student.studentId') }}</Label>
                <Input v-model="studentQuery.studentNo" :placeholder="t('student.inputStudentNo')" class="w-40" />
              </div>
              <div class="flex items-center gap-2 pb-0.5">
                <Checkbox id="hasWarning" v-model:checked="studentQuery.hasWarning" />
                <Label for="hasWarning" class="text-xs cursor-pointer">{{ t('class.noStudentWarning') }}</Label>
              </div>
              <Button @click="loadStudents" size="sm" class="bg-[#155e75] hover:bg-[#0e4a5e]">
                <Search class="mr-1 size-4" />
                {{ t('common.query') }}
              </Button>
            </div>

            <div v-if="studentsLoading" class="flex items-center justify-center py-12">
              <div class="size-8 animate-spin rounded-full border-4 border-[#e2e8f0] border-t-[#155e75]"></div>
            </div>

            <div v-else>
              <div class="overflow-x-auto rounded-lg border border-[#e2e8f0]">
                <table class="w-full">
                  <thead>
                    <tr class="border-b border-[#e2e8f0] bg-[#f8fafc]">
                      <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b]">{{ t('student.studentId') }}</th>
                      <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b]">{{ t('student.name') }}</th>
                      <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b]">{{ t('student.gender') }}</th>
                      <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b]">{{ t('warning.level') }}</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr
                      v-for="s in students"
                      :key="s.id"
                      class="border-b border-[#f1f5f9] last:border-0 hover:bg-[#f8fafc]"
                    >
                      <td class="px-4 py-3 text-sm text-[#475569]">{{ s.studentNo }}</td>
                      <td class="px-4 py-3 text-sm font-medium text-[#0f172a]">{{ s.name }}</td>
                      <td class="px-4 py-3 text-sm text-[#475569]">{{ s.gender === 1 ? t('student.male') : s.gender === 2 ? t('student.female') : t('student.unknown') }}</td>
                      <td class="px-4 py-3">
                        <span
                          v-if="s.warningLevel"
                          :class="['rounded-full px-2.5 py-0.5 text-xs font-medium', getWarningLevelClass(s.warningLevel)]"
                        >
                          {{ getWarningLevelLabel(s.warningLevel) }}
                        </span>
                        <span v-else class="text-sm text-[#94a3b8]">{{ t('common.none') }}</span>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <div v-if="students.length === 0" class="py-12 text-center text-sm text-[#94a3b8]">
                {{ t('student.noStudentData') }}
              </div>
            </div>
          </CardContent>
        </Card>
      </TabsContent>

      <TabsContent value="scores">
        <div v-if="statsLoading" class="flex items-center justify-center py-16">
          <div class="size-8 animate-spin rounded-full border-4 border-[#e2e8f0] border-t-[#155e75]"></div>
        </div>
        <div v-else class="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle class="text-sm">{{ t('class.scoreDistribution') }}</CardTitle>
            </CardHeader>
            <CardContent>
              <div ref="chartRef" class="h-72 w-full"></div>
              <div v-if="!scoreStats" class="py-8 text-center text-sm text-[#94a3b8]">{{ t('common.noData') }}</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle class="text-sm">{{ t('class.courseRanking') }}</CardTitle>
            </CardHeader>
            <CardContent>
              <div class="overflow-x-auto rounded-lg border border-[#e2e8f0]">
                <table class="w-full">
                  <thead>
                    <tr class="border-b border-[#e2e8f0] bg-[#f8fafc]">
                      <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b]">{{ t('score.course') }}</th>
                      <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b]">{{ t('score.average') }}</th>
                      <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b]">{{ t('score.passRate') }}</th>
                      <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b]">{{ t('score.failCount') }}</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr
                      v-for="(r, i) in scoreStats?.courseRankings ?? []"
                      :key="i"
                      class="border-b border-[#f1f5f9] last:border-0 hover:bg-[#f8fafc]"
                    >
                      <td class="px-4 py-3 text-sm font-medium text-[#0f172a]">{{ r.courseName }}</td>
                      <td class="px-4 py-3 text-sm text-[#475569]">{{ r.averageScore }}</td>
                      <td class="px-4 py-3 text-sm text-[#475569]">{{ r.passRate }}%</td>
                      <td class="px-4 py-3">
                        <span
                          v-if="r.failCount > 0"
                          class="text-sm font-medium text-[#dc2626]"
                        >
                          {{ r.failCount }}
                        </span>
                        <span v-else class="text-sm text-[#94a3b8]">0</span>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <div v-if="!scoreStats?.courseRankings?.length" class="py-8 text-center text-sm text-[#94a3b8]">
                {{ t('common.noData') }}
              </div>
            </CardContent>
          </Card>
        </div>
      </TabsContent>

      <TabsContent value="warnings">
        <div v-if="warningsLoading" class="flex items-center justify-center py-16">
          <div class="size-8 animate-spin rounded-full border-4 border-[#e2e8f0] border-t-[#155e75]"></div>
        </div>
        <div v-else class="space-y-6">
          <div class="grid gap-4 grid-cols-1 sm:grid-cols-3">
            <Card
              v-for="s in warningSummary"
              :key="s.level"
              :class="['border', getWarningLevelCardClass(s.level)]"
            >
              <CardContent class="py-4 text-center">
                <p class="text-sm text-[#64748b]">{{ getWarningLevelLabel(s.level) }}</p>
                <p :class="['mt-1 text-3xl font-bold', getWarningLevelTextClass(s.level)]">
                  {{ s.count }}
                  <span class="text-base font-normal">{{ t('unit.person') }}</span>
                </p>
              </CardContent>
            </Card>
          </div>

          <div v-if="warningStudents.length === 0" class="py-12 text-center text-sm text-[#94a3b8]">
            {{ t('class.noStudentWarning') }}
          </div>

          <Card v-for="ws in warningStudents" :key="ws.studentId">
            <CardContent class="py-4">
              <div class="mb-3 flex flex-wrap items-center justify-between gap-2">
                <div class="flex items-center gap-3">
                  <h3 class="text-sm font-semibold text-[#0f172a]">{{ ws.studentName }}</h3>
                  <span class="text-xs text-[#64748b]">{{ ws.studentNo }}</span>
                  <span class="text-xs text-[#64748b]">{{ ws.className }}</span>
                </div>
                <span
                  :class="['rounded-full px-2.5 py-0.5 text-xs font-medium', getWarningLevelClass(ws.maxLevel ?? '')]"
                >
                  {{ getWarningLevelLabel(ws.maxLevel ?? '') }}
                </span>
              </div>
              <div class="space-y-1.5">
                <div
                  v-for="(w, i) in ws.warnings"
                  :key="i"
                  class="flex items-start gap-2 rounded-md border border-[#f1f5f9] bg-[#f8fafc] px-3 py-2"
                >
                  <AlertTriangle class="mt-0.5 size-3.5 shrink-0 text-[#b45309]" />
                  <div class="text-xs text-[#475569]">
                    <span class="font-medium text-[#0f172a]">{{ w.courseName }}</span>
                    <span class="mx-1">·</span>
                    <span :class="getWarningLevelClass(w.level)" class="rounded px-1.5 py-0.5 text-xs">{{ getWarningLevelLabel(w.level) }}</span>
                    <span class="mx-1">·</span>
                    <span>{{ w.reason }}</span>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </TabsContent>
    </Tabs>
  </div>
</template>
