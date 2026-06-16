<script setup lang="ts">
import { ref, computed, onMounted } from "vue"
import { useI18n } from "vue-i18n"
import { AlertTriangle, Filter, Users } from "@lucide/vue"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { fetchWarnings, type StudentWarnings } from "../../api/warning"
import { WARNING_LEVEL, WARNING_TYPE } from "@/constants/enum"

const { t } = useI18n()

const warnings = ref<StudentWarnings[]>([])
const loading = ref(false)

const selectedClass = ref("")
const selectedLevel = ref("")
const selectedType = ref("")

const classOptions = computed(() => [
  { value: "", label: t('warning.allClasses') },
  { value: "1", label: t('warning.class1') },
  { value: "2", label: t('warning.class2') },
  { value: "3", label: t('warning.class3') },
])

const levelOptions = computed(() => [
  { value: "", label: t('warning.allLevels') },
  { value: WARNING_LEVEL.NORMAL, label: t('warning.normal') },
  { value: WARNING_LEVEL.FOCUS, label: t('warning.focus') },
  { value: WARNING_LEVEL.SEVERE, label: t('warning.severe') },
])

const typeOptions = computed(() => [
  { value: "", label: t('warning.allTypes') },
  { value: WARNING_TYPE.LOW_SCORE, label: t('warning.lowScore') },
  { value: WARNING_TYPE.LOW_AVERAGE, label: t('warning.lowAverage') },
  { value: WARNING_TYPE.LOW_RANKING, label: t('warning.lowRanking') },
  { value: WARNING_TYPE.FAILED_COURSES, label: t('warning.failedCourses') },
])

const normalCount = computed(() => 
  warnings.value.filter((w) => w.maxLevel === WARNING_LEVEL.NORMAL).length
)

const moderateCount = computed(() => 
  warnings.value.filter((w) => w.maxLevel === WARNING_LEVEL.FOCUS).length
)

const severeCount = computed(() => 
  warnings.value.filter((w) => w.maxLevel === WARNING_LEVEL.SEVERE).length
)

function getLevelColor(level: string) {
  if (level === WARNING_LEVEL.SEVERE) return "bg-[#dc2626] text-white"
  if (level === WARNING_LEVEL.FOCUS) return "bg-[#ea580c] text-white"
  return "bg-[#16a34a] text-white"
}

async function loadWarnings() {
  loading.value = true
  try {
    const params: { classId?: number; level?: string; type?: string } = {}
    if (selectedClass.value) params.classId = Number(selectedClass.value)
    if (selectedLevel.value) params.level = selectedLevel.value
    if (selectedType.value) params.type = selectedType.value
    warnings.value = await fetchWarnings(params)
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
    <div class="rounded-lg border border-[#e2e8f0] bg-white p-4 dark:border-gray-700 dark:bg-gray-800">
      <div class="mb-4 flex items-center gap-2">
        <Filter class="size-4 text-[#64748b] dark:text-gray-400" />
        <h3 class="text-sm font-semibold text-[#0f172a] dark:text-white">{{ t('warning.filterTitle') }}</h3>
      </div>
        <div class="flex flex-wrap items-end gap-4">
        <div class="flex flex-col gap-1.5">
          <label class="text-xs font-medium text-[#64748b] dark:text-gray-400">{{ t('student.class') }}</label>
          <select
            v-model="selectedClass"
            class="h-9 rounded-md border border-[#e2e8f0] bg-white px-3 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75] dark:bg-gray-700 dark:border-gray-600 dark:text-gray-300"
          >
            <option v-for="opt in classOptions" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </option>
          </select>
        </div>
        <div class="flex flex-col gap-1.5">
          <label class="text-xs font-medium text-[#64748b] dark:text-gray-400">{{ t('warning.level') }}</label>
          <select
            v-model="selectedLevel"
            class="h-9 rounded-md border border-[#e2e8f0] bg-white px-3 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75] dark:bg-gray-700 dark:border-gray-600 dark:text-gray-300"
          >
            <option v-for="opt in levelOptions" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </option>
          </select>
        </div>
        <div class="flex flex-col gap-1.5">
          <label class="text-xs font-medium text-[#64748b] dark:text-gray-400">{{ t('warning.type') }}</label>
          <select
            v-model="selectedType"
            class="h-9 rounded-md border border-[#e2e8f0] bg-white px-3 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75] dark:bg-gray-700 dark:border-gray-600 dark:text-gray-300"
          >
            <option v-for="opt in typeOptions" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </option>
          </select>
        </div>
        <div class="flex items-center gap-2">
          <Button size="sm" @click="handleSearch" class="dark:bg-teal-700 dark:hover:bg-teal-600">{{ t('common.query') }}</Button>
          <Button size="sm" variant="outline" @click="handleReset" class="dark:bg-gray-700 dark:text-gray-300 dark:hover:bg-gray-600">{{ t('common.reset') }}</Button>
        </div>
      </div>
    </div>

    <div class="grid gap-4 md:grid-cols-3">
      <Card class="dark:border-gray-700 dark:bg-gray-800">
        <CardHeader class="pb-2">
          <CardTitle class="text-sm font-medium text-[#64748b] dark:text-gray-400">{{ t('warning.normal') }}</CardTitle>
        </CardHeader>
        <CardContent>
          <div class="flex items-center justify-between">
            <p class="text-3xl font-bold text-[#16a34a] dark:text-green-400">{{ normalCount }}</p>
            <div class="grid size-10 place-items-center rounded-md bg-[#f0fdf4] dark:bg-green-900">
              <Users class="size-5 text-[#16a34a] dark:text-green-400" />
            </div>
          </div>
          <p class="mt-1 text-xs text-[#64748b] dark:text-gray-400">{{ t('unit.person') }}</p>
        </CardContent>
      </Card>

      <Card class="dark:border-gray-700 dark:bg-gray-800">
        <CardHeader class="pb-2">
          <CardTitle class="text-sm font-medium text-[#64748b] dark:text-gray-400">{{ t('warning.focus') }}</CardTitle>
        </CardHeader>
        <CardContent>
          <div class="flex items-center justify-between">
            <p class="text-3xl font-bold text-[#ea580c] dark:text-orange-400">{{ moderateCount }}</p>
            <div class="grid size-10 place-items-center rounded-md bg-[#fff7ed] dark:bg-orange-900">
              <AlertTriangle class="size-5 text-[#ea580c] dark:text-orange-400" />
            </div>
          </div>
          <p class="mt-1 text-xs text-[#64748b] dark:text-gray-400">{{ t('unit.person') }}</p>
        </CardContent>
      </Card>

      <Card class="dark:border-gray-700 dark:bg-gray-800">
        <CardHeader class="pb-2">
          <CardTitle class="text-sm font-medium text-[#64748b] dark:text-gray-400">{{ t('warning.severe') }}</CardTitle>
        </CardHeader>
        <CardContent>
          <div class="flex items-center justify-between">
            <p class="text-3xl font-bold text-[#dc2626] dark:text-red-400">{{ severeCount }}</p>
            <div class="grid size-10 place-items-center rounded-md bg-[#fef2f2] dark:bg-red-900">
              <AlertTriangle class="size-5 text-[#dc2626] dark:text-red-400" />
            </div>
          </div>
          <p class="mt-1 text-xs text-[#64748b] dark:text-gray-400">{{ t('unit.person') }}</p>
        </CardContent>
      </Card>
    </div>

    <div class="space-y-4">
      <div v-if="loading" class="py-12 text-center text-sm text-[#64748b] dark:text-gray-400">
        {{ t('common.loading') }}
      </div>
      <div v-else-if="warnings.length === 0" class="py-12 text-center">
        <AlertTriangle class="mx-auto size-10 text-[#cbd5e1] dark:text-gray-600" />
        <p class="mt-2 text-sm text-[#64748b] dark:text-gray-400">{{ t('warning.noWarningData') }}</p>
      </div>
      <div
        v-for="student in warnings"
        :key="student.studentId"
        class="overflow-hidden rounded-lg border border-[#e2e8f0] bg-white dark:border-gray-700 dark:bg-gray-800"
      >
        <div class="flex items-center justify-between border-b border-[#f1f5f9] bg-[#f8fafc] px-4 py-3 dark:border-gray-700 dark:bg-gray-700">
          <div class="flex items-center gap-4">
            <div class="flex items-center gap-2">
              <Users class="size-4 text-[#64748b] dark:text-gray-400" />
              <span class="text-sm font-semibold text-[#0f172a] dark:text-white">{{ student.studentName }}</span>
            </div>
            <span class="text-xs text-[#64748b] dark:text-gray-400">{{ student.studentNo }}</span>
            <span class="rounded-full bg-[#f1f5f9] px-2 py-0.5 text-xs text-[#475569] dark:bg-gray-600 dark:text-gray-300">
              {{ student.className }}
            </span>
          </div>
          <span :class="['rounded-full px-2.5 py-0.5 text-xs font-medium', getLevelColor(student.maxLevel ?? '')]">
            {{ student.maxLevel }}
          </span>
        </div>
        <div class="divide-y divide-[#f1f5f9] dark:divide-gray-700">
          <div
            v-for="(item, idx) in student.warnings"
            :key="idx"
            class="flex items-center justify-between px-4 py-3"
          >
            <div class="flex items-center gap-4">
              <span :class="['rounded px-2 py-0.5 text-xs font-medium', getLevelColor(item.level)]">
                {{ item.type }}
              </span>
              <span v-if="item.courseName" class="text-sm text-[#475569] dark:text-gray-400">{{ item.courseName }}</span>
              <span class="text-xs text-[#64748b] dark:text-gray-400">
                {{ t('warning.currentValue') }}: {{ item.currentValue }} / {{ t('warning.threshold') }}: {{ item.threshold }}
              </span>
            </div>
            <span class="text-xs text-[#94a3b8] dark:text-gray-500">{{ item.reason }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
