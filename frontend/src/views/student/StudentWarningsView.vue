<script setup lang="ts">
import { ref, computed, onMounted } from "vue"
import { useI18n } from "vue-i18n"
import { AlertTriangle, BookOpen, TrendingDown, Award, Clock } from "@lucide/vue"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { fetchStudentWarnings, type StudentWarnings } from "../../api/warning"

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
  if (type === "单科低分") return t('warning.suggestLowScore')
  if (type === "学期平均分偏低") return t('warning.suggestLowAverage')
  if (type === "总分排名靠后") return t('warning.suggestLowRanking')
  if (type === "累计不及格") return t('warning.suggestFailed')
  return ""
}

const uniqueWarningTypes = computed(() => {
  const types = new Set(warnings.value.map((w) => w.type))
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

onMounted(() => {
  loadWarnings()
})
</script>

<template>
  <div class="space-y-6">
    <div class="grid gap-4 md:grid-cols-2">
      <Card class="dark:border-gray-700 dark:bg-gray-800">
        <CardHeader class="pb-2">
          <CardTitle class="text-sm font-medium text-[#64748b] dark:text-gray-400">{{ t('warning.failCourseCount') }}</CardTitle>
        </CardHeader>
        <CardContent>
          <div class="flex items-center justify-between">
            <p class="text-3xl font-bold text-[#dc2626] dark:text-red-400">{{ failCount }}</p>
            <div class="grid size-10 place-items-center rounded-md bg-[#fef2f2] dark:bg-red-900">
              <AlertTriangle class="size-5 text-[#dc2626] dark:text-red-400" />
            </div>
          </div>
          <p class="mt-1 text-xs text-[#64748b] dark:text-gray-400">{{ t('unit.course') }}</p>
        </CardContent>
      </Card>

      <Card class="dark:border-gray-700 dark:bg-gray-800">
        <CardHeader class="pb-2">
          <CardTitle class="text-sm font-medium text-[#64748b] dark:text-gray-400">{{ t('warning.maxLevel') }}</CardTitle>
        </CardHeader>
        <CardContent>
          <div class="flex items-center justify-between">
            <span :class="['rounded-full px-3 py-1 text-sm font-semibold', getLevelColor(maxLevel)]">
              {{ maxLevel }}
            </span>
            <div class="grid size-10 place-items-center rounded-md bg-[#fff7ed] dark:bg-orange-900">
              <Award class="size-5 text-[#ea580c] dark:text-orange-400" />
            </div>
          </div>
        </CardContent>
      </Card>
    </div>

    <div class="space-y-4">
      <h3 class="text-sm font-semibold text-[#0f172a] dark:text-white">{{ t('warning.warningDetails') }}</h3>
      <div v-if="loading" class="py-12 text-center text-sm text-[#64748b] dark:text-gray-400">
        {{ t('common.loading') }}
      </div>
      <div v-else-if="warnings.length === 0" class="py-12 text-center">
        <AlertTriangle class="mx-auto size-10 text-[#cbd5e1] dark:text-gray-600" />
        <p class="mt-2 text-sm text-[#64748b] dark:text-gray-400">{{ t('warning.noWarningInfo') }}</p>
      </div>
      <div
        v-for="(item, idx) in warnings"
        :key="idx"
        :class="['overflow-hidden rounded-lg border', getLevelBg(item.level)]"
      >
        <div class="flex items-center justify-between border-b border-inherit px-4 py-3">
          <div class="flex items-center gap-3">
            <component :is="getTypeIcon(item.type)" class="size-4 text-[#64748b] dark:text-gray-400" />
            <span class="text-sm font-semibold text-[#0f172a] dark:text-white">{{ item.type }}</span>
          </div>
          <span :class="['rounded-full px-2.5 py-0.5 text-xs font-medium', getLevelColor(item.level)]">
            {{ item.level }}
          </span>
        </div>
        <div class="px-4 py-3">
          <div v-if="item.courseName" class="mb-2">
            <span class="text-xs text-[#64748b] dark:text-gray-400">{{ t('warning.courseLabel') }}：</span>
            <span class="text-sm font-medium text-[#475569] dark:text-gray-300">{{ item.courseName }}</span>
          </div>
          <div class="mb-2 flex items-center gap-4">
            <div>
              <span class="text-xs text-[#64748b] dark:text-gray-400">{{ t('warning.currentValue') }}：</span>
              <span class="text-sm font-semibold text-[#0f172a] dark:text-white">{{ item.currentValue }}</span>
            </div>
            <div>
              <span class="text-xs text-[#64748b] dark:text-gray-400">{{ t('warning.threshold') }}：</span>
              <span class="text-sm font-semibold text-[#0f172a] dark:text-white">{{ item.threshold }}</span>
            </div>
          </div>
          <p class="text-sm text-[#475569] dark:text-gray-300">{{ item.reason }}</p>
        </div>
      </div>
    </div>

    <div v-if="suggestions.length > 0" class="space-y-4">
      <h3 class="text-sm font-semibold text-[#0f172a] dark:text-white">{{ t('warning.suggestions') }}</h3>
      <div class="grid gap-4 md:grid-cols-2">
        <div
          v-for="item in suggestions"
          :key="item.type"
          class="rounded-lg border border-[#e2e8f0] bg-white p-4 dark:border-gray-700 dark:bg-gray-800"
        >
          <div class="mb-2 flex items-center gap-2">
            <component :is="getTypeIcon(item.type)" class="size-4 text-[#155e75] dark:text-teal-400" />
            <span class="text-sm font-semibold text-[#0f172a] dark:text-white">{{ item.type }}</span>
          </div>
          <p class="text-sm text-[#475569] dark:text-gray-300">{{ item.suggestion }}</p>
        </div>
      </div>
    </div>
  </div>
</template>
