<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from "vue"
import { useI18n } from "vue-i18n"
import {
  AlertTriangle,
  BookOpen,
  TrendingDown,
  Award,
  Clock,
  CheckCircle,
  ChevronDown,
  ChevronRight,
} from "@lucide/vue"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import {
  fetchStudentWarnings,
  type StudentWarnings,
  type WarningItem,
} from "../../api/warning"

const { t } = useI18n()

const warningData = ref<StudentWarnings | null>(null)
const loading = ref(false)

const userInfo = ref<{ id: number; name: string } | null>(null)

try {
  const stored = localStorage.getItem("userInfo")
  if (stored) {
    userInfo.value = JSON.parse(stored)
  }
} catch (e) {
  console.error("Failed to get user info", e)
}

const failCount = computed(() => warningData.value?.failCount ?? 0)
const maxLevel = computed(() => warningData.value?.maxLevel ?? "无")
const warnings = computed(() => warningData.value?.warnings ?? [])

// 学期筛选
const selectedAcademicYear = ref("")
const selectedSemester = ref("")

const semesterOptions = computed(() => {
  const semesters = new Set<string>()
  warnings.value.forEach((w) => {
    if (w.history) {
      w.history.forEach((h) => {
        semesters.add(`${h.academicYear} ${h.semester}`)
      })
    }
  })
  return Array.from(semesters).sort().reverse()
})

const filteredWarnings = computed(() => {
  if (!selectedAcademicYear.value && !selectedSemester.value) {
    return warnings.value
  }
  return warnings.value.filter((w) => {
    if (!w.history || w.history.length === 0) return true
    return w.history.some(
      (h) =>
        (!selectedAcademicYear.value || h.academicYear === selectedAcademicYear.value) &&
        (!selectedSemester.value || h.semester === selectedSemester.value)
    )
  })
})

// 折叠状态
const expandedItems = ref<Set<string>>(new Set())

function generateKey(item: WarningItem): string {
  return `${item.type}-${item.courseName || 'null'}-${item.currentValue}`
}

function toggleExpand(key: string) {
  if (expandedItems.value.has(key)) {
    expandedItems.value.delete(key)
  } else {
    expandedItems.value.add(key)
  }
}

function isExpanded(key: string) {
  return expandedItems.value.has(key)
}

function getLevelColor(level: string) {
  if (level === "严重预警") return "bg-[#dc2626] text-white"
  if (level === "重点关注") return "bg-[#ea580c] text-white"
  if (level === "普通提醒") return "bg-[#16a34a] text-white"
  return "bg-[#64748b] text-white"
}

function getLevelBg(level: string) {
  if (level === "严重预警") return "bg-[#fef2f2] border-[#fecaca]"
  if (level === "重点关注") return "bg-[#fff7ed] border-[#fed7aa]"
  if (level === "普通提醒") return "bg-[#f0fdf4] border-[#bbf7d0]"
  return "bg-[#f8fafc] border-[#e2e8f0]"
}

function getTypeIcon(type: string) {
  if (type === "单科低分") return BookOpen
  if (type === "学期平均分偏低") return TrendingDown
  if (type === "总分排名靠后") return Award
  if (type === "累计不及格") return Clock
  return AlertTriangle
}

function getSuggestion(type: string) {
  if (type === "单科低分") return t("warning.suggestLowScore")
  if (type === "学期平均分偏低") return t("warning.suggestLowAverage")
  if (type === "总分排名靠后") return t("warning.suggestLowRanking")
  if (type === "累计不及格") return t("warning.suggestFailed")
  return ""
}

const uniqueWarningTypes = computed(() => {
  const types = new Set(filteredWarnings.value.map((w) => w.type))
  return Array.from(types)
})

const suggestions = computed(() => {
  return uniqueWarningTypes.value.map((type) => ({
    type,
    suggestion: getSuggestion(type),
  }))
})

async function loadWarnings() {
  if (!userInfo.value?.id) return
  loading.value = true
  try {
    warningData.value = await fetchStudentWarnings(userInfo.value.id)
  } catch (e) {
    console.error("Failed to load warnings", e)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadWarnings()
})

onUnmounted(() => {
})
</script>

<template>
  <div class="space-y-6">
    <!-- 统计卡片 -->
    <div class="grid gap-4 md:grid-cols-2">
      <Card class="dark:border-gray-700 dark:bg-gray-800">
        <CardHeader class="pb-2">
          <CardTitle class="text-sm font-medium text-[#64748b] dark:text-gray-400">{{
            t("warning.failCourseCount")
          }}</CardTitle>
        </CardHeader>
        <CardContent>
          <div class="flex items-center justify-between">
            <div class="flex items-baseline gap-1">
              <p class="text-3xl font-bold text-[#dc2626] dark:text-red-400">{{ failCount }}</p>
              <span class="text-sm text-[#64748b] dark:text-gray-400">{{ t("unit.course") }}</span>
            </div>
            <div class="grid size-10 place-items-center rounded-md bg-[#fef2f2] dark:bg-red-900">
              <AlertTriangle class="size-5 text-[#dc2626] dark:text-red-400" />
            </div>
          </div>
        </CardContent>
      </Card>

      <Card class="dark:border-gray-700 dark:bg-gray-800">
        <CardHeader class="pb-2">
          <CardTitle class="text-sm font-medium text-[#64748b] dark:text-gray-400">{{
            t("warning.maxLevel")
          }}</CardTitle>
        </CardHeader>
        <CardContent>
          <div class="flex items-center justify-between">
            <span
              :class="[
                'rounded-full px-3 py-1 text-sm font-semibold',
                getLevelColor(maxLevel),
              ]"
            >
              {{ maxLevel }}
            </span>
            <div class="grid size-10 place-items-center rounded-md bg-[#fff7ed] dark:bg-orange-900">
              <Award class="size-5 text-[#ea580c] dark:text-orange-400" />
            </div>
          </div>
        </CardContent>
      </Card>
    </div>

    <!-- 预警详情 -->
    <div class="space-y-4">
      <div class="flex items-center justify-between">
        <h3 class="text-sm font-semibold text-[#0f172a] dark:text-white">
          {{ t("warning.warningDetails") }}
        </h3>
        <!-- 学期筛选 -->
        <div class="flex items-center gap-2">
          <select
            v-model="selectedSemester"
            class="h-8 rounded-md border border-[#e2e8f0] bg-white px-2 text-xs text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75] dark:border-gray-600 dark:bg-gray-700 dark:text-gray-300"
          >
            <option value="">{{ t("warning.allSemesters") }}</option>
            <option v-for="sem in semesterOptions" :key="sem" :value="sem">
              {{ sem }}
            </option>
          </select>
        </div>
      </div>

      <div
        v-if="loading"
        class="py-12 text-center text-sm text-[#64748b] dark:text-gray-400"
      >
        {{ t("common.loading") }}
      </div>

      <!-- 空状态优化 -->
      <div
        v-else-if="filteredWarnings.length === 0"
        class="py-12 text-center"
      >
        <CheckCircle class="mx-auto size-12 text-[#16a34a] dark:text-green-400" />
        <p class="mt-3 text-base font-medium text-[#0f172a] dark:text-white">
          {{ t("warning.emptyTitle") }}
        </p>
        <p class="mt-1 text-sm text-[#64748b] dark:text-gray-400">
          {{ t("warning.emptySubtitle") }}
        </p>
      </div>

      <!-- 预警卡片列表 - 三列布局 -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div
          v-for="item in filteredWarnings"
          :key="generateKey(item)"
          :class="['flex flex-col overflow-hidden rounded-lg border', getLevelBg(item.level)]"
        >
          <!-- 卡片头部 - 可点击 -->
          <div
            class="flex cursor-pointer items-center justify-between border-b border-inherit px-4 py-3 hover:bg-black/[0.02]"
            @click="toggleExpand(generateKey(item))"
          >
            <div class="flex items-center gap-2 min-w-0">
              <component
                :is="getTypeIcon(item.type)"
                class="size-4 shrink-0 text-[#64748b] dark:text-gray-400"
              />
              <span class="text-sm font-semibold text-[#0f172a] dark:text-white truncate">{{
                item.type
              }}</span>
            </div>
            <div class="flex items-center gap-2 shrink-0">
              <span
                :class="[
                  'rounded-full px-2 py-0.5 text-xs font-medium',
                  getLevelColor(item.level),
                ]"
              >
                {{ item.level }}
              </span>
              <ChevronDown
                v-if="isExpanded(generateKey(item))"
                class="size-4 text-[#64748b] dark:text-gray-400"
              />
              <ChevronRight
                v-else
                class="size-4 text-[#64748b] dark:text-gray-400"
              />
            </div>
          </div>

          <!-- 卡片内容 -->
          <div class="flex-1 px-4 py-3">
            <div v-if="item.courseName" class="mb-2">
              <span class="text-xs text-[#64748b] dark:text-gray-400">{{
                t("warning.courseLabel")
              }}</span>
              <span class="text-sm font-medium text-[#0f172a] dark:text-white">{{
                item.courseName
              }}</span>
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
            <p class="text-sm text-[#475569] dark:text-gray-300 line-clamp-2">{{ item.reason }}</p>

            <!-- 展开后显示成绩历史 -->
            <Transition
              enter-active-class="transition-all duration-300 ease-out"
              enter-from-class="max-h-0 opacity-0"
              enter-to-class="max-h-96 opacity-100"
              leave-active-class="transition-all duration-200 ease-in"
              leave-from-class="max-h-96 opacity-100"
              leave-to-class="max-h-0 opacity-0"
            >
              <div
                v-if="isExpanded(generateKey(item)) && item.history && item.history.length > 0"
                class="mt-3 overflow-hidden border-t border-inherit pt-3"
              >
                <p class="mb-2 text-xs font-medium text-[#64748b] dark:text-gray-400">
                  {{ t("warning.scoreHistory") }}
                </p>
                <div class="space-y-1.5">
                  <div
                    v-for="(history, hIdx) in item.history"
                    :key="hIdx"
                    class="flex items-center justify-between text-xs"
                  >
                    <span class="text-[#475569] dark:text-gray-300">
                      {{ history.academicYear }} {{ history.semester }} · {{ history.examType }}
                    </span>
                    <span
                      :class="[
                        'font-medium',
                        history.score < 60
                          ? 'text-[#dc2626] dark:text-red-400'
                          : 'text-[#0f172a] dark:text-white',
                      ]"
                    >
                      {{ history.score }}
                    </span>
                  </div>
                </div>
              </div>
            </Transition>
          </div>
        </div>
      </div>
    </div>

    <!-- 改进建议 -->
    <div v-if="suggestions.length > 0" class="space-y-4">
      <h3 class="text-sm font-semibold text-[#0f172a] dark:text-white">
        {{ t("warning.suggestions") }}
      </h3>
      <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        <div
          v-for="item in suggestions"
          :key="item.type"
          class="rounded-lg border border-[#e2e8f0] bg-white p-4 dark:border-gray-700 dark:bg-gray-800"
        >
          <div class="mb-2 flex items-center gap-2">
            <component
              :is="getTypeIcon(item.type)"
              class="size-4 text-[#155e75] dark:text-teal-400"
            />
            <span class="text-sm font-semibold text-[#0f172a] dark:text-white">{{
              item.type
            }}</span>
          </div>
          <p class="text-sm text-[#475569] dark:text-gray-300">{{ item.suggestion }}</p>
        </div>
      </div>
    </div>
  </div>
</template>
