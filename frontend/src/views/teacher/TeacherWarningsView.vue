<script setup lang="ts">
import { ref, computed, onMounted, watch } from "vue"
import { useI18n } from "vue-i18n"
import {
  AlertTriangle,
  Filter,
  Users,
  BookOpen,
  TrendingDown,
  Award,
  Clock,
} from "@lucide/vue"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { fetchWarnings, type StudentWarnings, type WarningItem } from "../../api/warning"
import { WARNING_LEVEL, WARNING_TYPE } from "@/constants/enum"

const { t } = useI18n()

const warnings = ref<StudentWarnings[]>([])
const loading = ref(false)

const selectedClass = ref("")
const selectedLevel = ref("")
const selectedType = ref("")
const selectedStudentId = ref<number | null>(null)

// 左侧学生分页
const studentPage = ref(1)
const studentPageSize = 9
const studentTotalPages = computed(() => Math.ceil(warnings.value.length / studentPageSize))
const pagedStudents = computed(() => {
  const start = (studentPage.value - 1) * studentPageSize
  return warnings.value.slice(start, start + studentPageSize)
})

// 右侧预警分页
const warningPage = ref(1)
const warningPageSize = 5
const warningTotalPages = computed(() => {
  if (!selectedStudent.value) return 0
  return Math.ceil(selectedStudent.value.warnings.length / warningPageSize)
})
const pagedWarnings = computed(() => {
  if (!selectedStudent.value) return []
  const start = (warningPage.value - 1) * warningPageSize
  return selectedStudent.value.warnings.slice(start, start + warningPageSize)
})

const selectedStudent = computed(() =>
  warnings.value.find((w) => w.studentId === selectedStudentId.value) ?? null
)

function selectStudent(id: number) {
  selectedStudentId.value = id
  warningPage.value = 1
}

const classOptions = computed(() => [
  { value: "", label: t("warning.allClasses") },
  { value: "1", label: t("warning.class1") },
  { value: "2", label: t("warning.class2") },
  { value: "3", label: t("warning.class3") },
])

const levelOptions = computed(() => [
  { value: "", label: t("warning.allLevels") },
  { value: WARNING_LEVEL.NORMAL, label: t("warning.normal") },
  { value: WARNING_LEVEL.FOCUS, label: t("warning.focus") },
  { value: WARNING_LEVEL.SEVERE, label: t("warning.severe") },
])

const typeOptions = computed(() => [
  { value: "", label: t("warning.allTypes") },
  { value: WARNING_TYPE.LOW_SCORE, label: t("warning.lowScore") },
  { value: WARNING_TYPE.LOW_AVERAGE, label: t("warning.lowAverage") },
  { value: WARNING_TYPE.LOW_RANKING, label: t("warning.lowRanking") },
  { value: WARNING_TYPE.FAILED_COURSES, label: t("warning.failedCourses") },
])

const normalCount = computed(
  () => warnings.value.filter((w) => w.maxLevel === WARNING_LEVEL.NORMAL).length
)

const moderateCount = computed(
  () => warnings.value.filter((w) => w.maxLevel === WARNING_LEVEL.FOCUS).length
)

const severeCount = computed(
  () => warnings.value.filter((w) => w.maxLevel === WARNING_LEVEL.SEVERE).length
)

function getLevelColor(level: string) {
  if (level === WARNING_LEVEL.SEVERE) return "bg-[#dc2626] text-white"
  if (level === WARNING_LEVEL.FOCUS) return "bg-[#ea580c] text-white"
  return "bg-[#16a34a] text-white"
}

function getLevelBg(level: string) {
  if (level === WARNING_LEVEL.SEVERE) return "bg-[#fef2f2] border-[#fecaca]"
  if (level === WARNING_LEVEL.FOCUS) return "bg-[#fff7ed] border-[#fed7aa]"
  return "bg-[#f0fdf4] border-[#bbf7d0]"
}

function getTypeIcon(type: string) {
  if (type === "单科低分") return BookOpen
  if (type === "学期平均分偏低") return TrendingDown
  if (type === "总分排名靠后") return Award
  if (type === "累计不及格") return Clock
  return AlertTriangle
}

async function loadWarnings() {
  loading.value = true
  try {
    const params: { classId?: number; level?: string; type?: string } = {}
    if (selectedClass.value) params.classId = Number(selectedClass.value)
    if (selectedLevel.value) params.level = selectedLevel.value
    if (selectedType.value) params.type = selectedType.value
    warnings.value = await fetchWarnings(params)
    studentPage.value = 1
    warningPage.value = 1
    if (warnings.value.length > 0) {
      selectedStudentId.value = warnings.value[0].studentId
    } else {
      selectedStudentId.value = null
    }
  } catch (e) {
    console.error("Failed to load warnings", e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  loadWarnings()
}

function handleReset() {
  selectedClass.value = ""
  selectedLevel.value = ""
  selectedType.value = ""
  loadWarnings()
}

onMounted(() => {
  loadWarnings()
})
</script>

<template>
  <div class="space-y-6">
    <!-- 筛选条件 -->
    <div
      class="rounded-lg border border-[#e2e8f0] bg-white p-4 dark:border-gray-700 dark:bg-gray-800"
    >
      <div class="mb-4 flex items-center gap-2">
        <Filter class="size-4 text-[#64748b] dark:text-gray-400" />
        <h3 class="text-sm font-semibold text-[#0f172a] dark:text-white">
          {{ t("warning.filterTitle") }}
        </h3>
      </div>
      <div class="flex flex-wrap items-end gap-4">
        <div class="flex flex-col gap-1.5">
          <label class="text-xs font-medium text-[#64748b] dark:text-gray-400">{{
            t("student.class")
          }}</label>
          <select
            v-model="selectedClass"
            class="h-9 rounded-md border border-[#e2e8f0] bg-white px-3 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75] dark:border-gray-600 dark:bg-gray-700 dark:text-gray-300"
          >
            <option v-for="opt in classOptions" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </option>
          </select>
        </div>
        <div class="flex flex-col gap-1.5">
          <label class="text-xs font-medium text-[#64748b] dark:text-gray-400">{{
            t("warning.level")
          }}</label>
          <select
            v-model="selectedLevel"
            class="h-9 rounded-md border border-[#e2e8f0] bg-white px-3 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75] dark:border-gray-600 dark:bg-gray-700 dark:text-gray-300"
          >
            <option v-for="opt in levelOptions" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </option>
          </select>
        </div>
        <div class="flex flex-col gap-1.5">
          <label class="text-xs font-medium text-[#64748b] dark:text-gray-400">{{
            t("warning.type")
          }}</label>
          <select
            v-model="selectedType"
            class="h-9 rounded-md border border-[#e2e8f0] bg-white px-3 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75] dark:border-gray-600 dark:bg-gray-700 dark:text-gray-300"
          >
            <option v-for="opt in typeOptions" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </option>
          </select>
        </div>
        <div class="flex items-center gap-2">
          <Button
            size="sm"
            @click="handleSearch"
            class="dark:bg-teal-700 dark:hover:bg-teal-600"
            >{{ t("common.query") }}</Button
          >
          <Button
            size="sm"
            variant="outline"
            @click="handleReset"
            class="dark:bg-gray-700 dark:text-gray-300 dark:hover:bg-gray-600"
            >{{ t("common.reset") }}</Button
          >
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="grid gap-4 md:grid-cols-3">
      <Card class="dark:border-gray-700 dark:bg-gray-800">
        <CardHeader class="pb-2">
          <CardTitle class="text-sm font-medium text-[#64748b] dark:text-gray-400">{{
            t("warning.normal")
          }}</CardTitle>
        </CardHeader>
        <CardContent>
          <div class="flex items-center justify-between">
            <div class="flex items-baseline gap-1">
              <p class="text-3xl font-bold text-[#16a34a] dark:text-green-400">
                {{ normalCount }}
              </p>
              <span class="text-sm text-[#64748b] dark:text-gray-400">{{
                t("unit.person")
              }}</span>
            </div>
            <div
              class="grid size-10 place-items-center rounded-md bg-[#f0fdf4] dark:bg-green-900"
            >
              <Users class="size-5 text-[#16a34a] dark:text-green-400" />
            </div>
          </div>
        </CardContent>
      </Card>

      <Card class="dark:border-gray-700 dark:bg-gray-800">
        <CardHeader class="pb-2">
          <CardTitle class="text-sm font-medium text-[#64748b] dark:text-gray-400">{{
            t("warning.focus")
          }}</CardTitle>
        </CardHeader>
        <CardContent>
          <div class="flex items-center justify-between">
            <div class="flex items-baseline gap-1">
              <p class="text-3xl font-bold text-[#ea580c] dark:text-orange-400">
                {{ moderateCount }}
              </p>
              <span class="text-sm text-[#64748b] dark:text-gray-400">{{
                t("unit.person")
              }}</span>
            </div>
            <div
              class="grid size-10 place-items-center rounded-md bg-[#fff7ed] dark:bg-orange-900"
            >
              <AlertTriangle class="size-5 text-[#ea580c] dark:text-orange-400" />
            </div>
          </div>
        </CardContent>
      </Card>

      <Card class="dark:border-gray-700 dark:bg-gray-800">
        <CardHeader class="pb-2">
          <CardTitle class="text-sm font-medium text-[#64748b] dark:text-gray-400">{{
            t("warning.severe")
          }}</CardTitle>
        </CardHeader>
        <CardContent>
          <div class="flex items-center justify-between">
            <div class="flex items-baseline gap-1">
              <p class="text-3xl font-bold text-[#dc2626] dark:text-red-400">
                {{ severeCount }}
              </p>
              <span class="text-sm text-[#64748b] dark:text-gray-400">{{
                t("unit.person")
              }}</span>
            </div>
            <div
              class="grid size-10 place-items-center rounded-md bg-[#fef2f2] dark:bg-red-900"
            >
              <AlertTriangle class="size-5 text-[#dc2626] dark:text-red-400" />
            </div>
          </div>
        </CardContent>
      </Card>
    </div>

    <!-- 加载/空状态 -->
    <div
      v-if="loading"
      class="py-12 text-center text-sm text-[#64748b] dark:text-gray-400"
    >
      {{ t("common.loading") }}
    </div>
    <div v-else-if="warnings.length === 0" class="py-12 text-center">
      <AlertTriangle class="mx-auto size-10 text-[#cbd5e1] dark:text-gray-600" />
      <p class="mt-2 text-sm text-[#64748b] dark:text-gray-400">
        {{ t("warning.noWarningData") }}
      </p>
    </div>

    <!-- 侧边详情面板 -->
    <div v-else class="flex gap-4" style="min-height: 420px">
      <!-- 左侧：学生列表 -->
      <div
        class="w-72 shrink-0 flex flex-col rounded-lg border border-[#e2e8f0] bg-white dark:border-gray-700 dark:bg-gray-800"
      >
        <div class="flex-1 space-y-2 overflow-y-auto p-3">
          <div
            v-for="student in pagedStudents"
            :key="student.studentId"
            :class="[
              'cursor-pointer rounded-lg border p-3 transition-colors',
              selectedStudentId === student.studentId
                ? 'border-[#155e75] bg-[#f0fdfa] dark:border-teal-500 dark:bg-teal-900/30'
                : 'border-transparent hover:bg-[#f8fafc] dark:hover:bg-gray-700',
            ]"
            @click="selectStudent(student.studentId)"
          >
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <Users class="size-4 text-[#64748b] dark:text-gray-400" />
                <span class="text-sm font-medium text-[#0f172a] dark:text-white">{{
                  student.studentName
                }}</span>
              </div>
              <span
                :class="[
                  'rounded-full px-2 py-0.5 text-xs font-medium',
                  getLevelColor(student.maxLevel ?? ''),
                ]"
              >
                {{ student.maxLevel }}
              </span>
            </div>
            <div class="mt-1 flex items-center gap-2 text-xs text-[#64748b] dark:text-gray-400">
              <span>{{ student.studentNo }}</span>
              <span>·</span>
              <span>{{ student.className }}</span>
              <span>·</span>
              <span>{{ student.warnings.length }}{{ t("unit.course") }}</span>
            </div>
          </div>
        </div>
        <!-- 左侧分页控件 -->
        <div
          v-if="studentTotalPages > 1"
          class="flex items-center justify-between border-t border-[#f1f5f9] px-3 py-2 dark:border-gray-700"
        >
          <button
            :disabled="studentPage <= 1"
            :class="[
              'rounded-md px-2.5 py-1 text-xs font-medium transition-colors',
              studentPage <= 1
                ? 'cursor-not-allowed bg-[#f1f5f9] text-[#cbd5e1] dark:bg-gray-700 dark:text-gray-600'
                : 'bg-[#f1f5f9] text-[#475569] hover:bg-[#e2e8f0] dark:bg-gray-700 dark:text-gray-300 dark:hover:bg-gray-600',
            ]"
            @click="studentPage--"
          >
            ‹ {{ t("common.prevPage") }}
          </button>
          <span class="text-xs text-[#64748b] dark:text-gray-400">
            {{ studentPage }} / {{ studentTotalPages }}
          </span>
          <button
            :disabled="studentPage >= studentTotalPages"
            :class="[
              'rounded-md px-2.5 py-1 text-xs font-medium transition-colors',
              studentPage >= studentTotalPages
                ? 'cursor-not-allowed bg-[#f1f5f9] text-[#cbd5e1] dark:bg-gray-700 dark:text-gray-600'
                : 'bg-[#f1f5f9] text-[#475569] hover:bg-[#e2e8f0] dark:bg-gray-700 dark:text-gray-300 dark:hover:bg-gray-600',
            ]"
            @click="studentPage++"
          >
            {{ t("common.nextPage") }} ›
          </button>
        </div>
      </div>

      <!-- 右侧：预警详情 -->
      <div
        class="flex-1 rounded-lg border border-[#e2e8f0] bg-white p-5 dark:border-gray-700 dark:bg-gray-800"
      >
        <div
          v-if="!selectedStudent"
          class="flex h-full items-center justify-center"
        >
          <p class="text-sm text-[#94a3b8] dark:text-gray-500">
            ← 请从左侧选择学生查看预警详情
          </p>
        </div>
        <div v-else>
          <!-- 学生信息头部 -->
          <div
            class="mb-5 flex items-center gap-4 border-b border-[#f1f5f9] pb-4 dark:border-gray-700"
          >
            <div class="flex items-center gap-2">
              <Users class="size-5 text-[#64748b] dark:text-gray-400" />
              <span class="text-base font-semibold text-[#0f172a] dark:text-white">{{
                selectedStudent.studentName
              }}</span>
            </div>
            <span class="text-sm text-[#64748b] dark:text-gray-400">{{
              selectedStudent.studentNo
            }}</span>
            <span
              class="rounded-full bg-[#f1f5f9] px-2 py-0.5 text-xs text-[#475569] dark:bg-gray-600 dark:text-gray-300"
            >
              {{ selectedStudent.className }}
            </span>
            <span
              :class="[
                'rounded-full px-2.5 py-0.5 text-xs font-medium',
                getLevelColor(selectedStudent.maxLevel ?? ''),
              ]"
            >
              {{ selectedStudent.maxLevel }}
            </span>
          </div>

          <!-- 预警项列表 -->
          <div class="space-y-3">
            <div
              v-for="(item, idx) in pagedWarnings"
              :key="idx"
              :class="['rounded-lg border p-4', getLevelBg(item.level)]"
            >
              <div class="mb-2 flex items-center justify-between">
                <div class="flex items-center gap-2">
                  <component
                    :is="getTypeIcon(item.type)"
                    class="size-4 text-[#64748b] dark:text-gray-400"
                  />
                  <span class="text-sm font-semibold text-[#0f172a] dark:text-white">{{
                    item.type
                  }}</span>
                  <span
                    v-if="item.courseName"
                    class="text-xs text-[#64748b] dark:text-gray-400"
                  >
                    · {{ item.courseName }}
                  </span>
                </div>
                <span
                  :class="[
                    'rounded-full px-2 py-0.5 text-xs font-medium',
                    getLevelColor(item.level),
                  ]"
                >
                  {{ item.level }}
                </span>
              </div>
              <div class="mb-2 flex items-center gap-4">
                <div>
                  <span class="text-xs text-[#64748b] dark:text-gray-400">{{
                    t("warning.currentValue")
                  }}：</span>
                  <span class="text-sm font-semibold text-[#0f172a] dark:text-white">{{
                    item.currentValue
                  }}</span>
                </div>
                <div>
                  <span class="text-xs text-[#64748b] dark:text-gray-400">{{
                    t("warning.threshold")
                  }}：</span>
                  <span class="text-sm font-semibold text-[#0f172a] dark:text-white">{{
                    item.threshold
                  }}</span>
                </div>
              </div>
              <p class="text-sm text-[#475569] dark:text-gray-300">
                {{ item.reason }}
              </p>
            </div>
          </div>
          <!-- 右侧分页控件 -->
          <div
            v-if="warningTotalPages > 1"
            class="mt-4 flex items-center justify-between border-t border-[#f1f5f9] pt-3 dark:border-gray-700"
          >
            <button
              :disabled="warningPage <= 1"
              :class="[
                'rounded-md px-2.5 py-1 text-xs font-medium transition-colors',
                warningPage <= 1
                  ? 'cursor-not-allowed bg-[#f1f5f9] text-[#cbd5e1] dark:bg-gray-700 dark:text-gray-600'
                  : 'bg-[#f1f5f9] text-[#475569] hover:bg-[#e2e8f0] dark:bg-gray-700 dark:text-gray-300 dark:hover:bg-gray-600',
              ]"
              @click="warningPage--"
            >
              ‹ {{ t("common.prevPage") }}
            </button>
            <span class="text-xs text-[#64748b] dark:text-gray-400">
              {{ warningPage }} / {{ warningTotalPages }}
            </span>
            <button
              :disabled="warningPage >= warningTotalPages"
              :class="[
                'rounded-md px-2.5 py-1 text-xs font-medium transition-colors',
                warningPage >= warningTotalPages
                  ? 'cursor-not-allowed bg-[#f1f5f9] text-[#cbd5e1] dark:bg-gray-700 dark:text-gray-600'
                  : 'bg-[#f1f5f9] text-[#475569] hover:bg-[#e2e8f0] dark:bg-gray-700 dark:text-gray-300 dark:hover:bg-gray-600',
              ]"
              @click="warningPage++"
            >
              {{ t("common.nextPage") }} ›
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
