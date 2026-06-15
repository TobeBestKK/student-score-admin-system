<script setup lang="ts">
import { ref, onMounted, watch, computed } from "vue"
import { useI18n } from "vue-i18n"
import { BookOpen, TrendingUp, Award, Search } from "@lucide/vue"
import {
  fetchSemesterOptions,
  fetchStudentScores,
  fetchStudentStats,
  fetchScoreTrend,
  type SemesterOption,
  type StudentCourseScore,
  type StudentStats,
  type ScoreTrend,
} from "@/api/dashboard"
import Card from "@/components/ui/card/Card.vue"
import CardContent from "@/components/ui/card/CardContent.vue"
import CardHeader from "@/components/ui/card/CardHeader.vue"
import CardTitle from "@/components/ui/card/CardTitle.vue"
import Input from "@/components/ui/input/Input.vue"
import Label from "@/components/ui/label/Label.vue"

const { t } = useI18n()

const userInfo = ref<{ id: number; name: string; role: string } | null>(null)

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
const keyword = ref("")
const scores = ref<StudentCourseScore[]>([])
const stats = ref<StudentStats | null>(null)
const trend = ref<ScoreTrend | null>(null)
const loading = ref(false)

const examTypeOptions = [
  { value: "", label: "全部类型" },
  { value: "期中", label: "期中" },
  { value: "期末", label: "期末" },
]

const filteredScores = computed(() => {
  if (!keyword.value) return scores.value
  return scores.value.filter((s) =>
    s.courseName.toLowerCase().includes(keyword.value.toLowerCase())
  )
})

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

function getSemesterParams() {
  if (!selectedSemester.value) return {}
  const parts = selectedSemester.value.split("-")
  if (parts.length >= 3) {
    return { academicYear: parts[0] + "-" + parts[1], semester: parts[2] }
  }
  return {}
}

async function loadData() {
  if (!userInfo.value?.id) return
  loading.value = true
  const semesterParams = getSemesterParams()
  const params = {
    studentId: userInfo.value.id,
    ...semesterParams,
    examType: selectedExamType.value || undefined,
  }

  try {
    const [scoresData, statsData, trendData] = await Promise.all([
      fetchStudentScores(params),
      fetchStudentStats(params),
      fetchScoreTrend(userInfo.value.id),
    ])
    scores.value = scoresData
    stats.value = statsData
    trend.value = trendData
  } catch (e) {
    console.error("Failed to load data", e)
  } finally {
    loading.value = false
  }
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

function getScoreLabel(score: number) {
  if (score >= 90) return "优秀"
  if (score >= 80) return "良好"
  if (score >= 70) return "中等"
  if (score >= 60) return "及格"
  return "不及格"
}

onMounted(async () => {
  await loadSemesters()
  await loadData()
})

watch([selectedSemester, selectedExamType], () => {
  loadData()
})
</script>

<template>
  <div class="space-y-6">
    <Card class="dark:border-gray-700 dark:bg-gray-800">
      <CardHeader>
        <CardTitle class="flex items-center gap-2 text-base dark:text-white">
          <BookOpen class="size-5 text-[#155e75] dark:text-teal-400" />
          {{ t('score.course') }}
        </CardTitle>
      </CardHeader>
      <CardContent>
        <div class="mb-6 flex flex-wrap items-end gap-4">
          <div class="flex flex-col gap-1.5">
            <Label class="text-xs dark:text-gray-300">{{ t('score.semester') }}</Label>
            <select
              v-model="selectedSemester"
              class="h-9 rounded-md border border-[#e2e8f0] bg-white px-3 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75] dark:bg-gray-700 dark:border-gray-600 dark:text-gray-300"
            >
              <option value="">{{ t('common.all') }}</option>
              <option
                v-for="opt in semesterOptions"
                :key="opt.academicYear + '-' + opt.semester"
                :value="opt.academicYear + '-' + opt.semester"
              >
                {{ opt.label }}
              </option>
            </select>
          </div>

          <div class="flex flex-col gap-1.5">
            <Label class="text-xs dark:text-gray-300">{{ t('score.examType') }}</Label>
            <select
              v-model="selectedExamType"
              class="h-9 rounded-md border border-[#e2e8f0] bg-white px-3 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75] dark:bg-gray-700 dark:border-gray-600 dark:text-gray-300"
            >
              <option v-for="opt in examTypeOptions" :key="opt.value" :value="opt.value">
                {{ opt.label }}
              </option>
            </select>
          </div>

          <div class="flex flex-col gap-1.5">
            <Label class="text-xs dark:text-gray-300">{{ t('score.course') }}</Label>
            <Input v-model="keyword" :placeholder="t('common.pleaseInput')" class="w-48 dark:bg-gray-700 dark:border-gray-600" />
          </div>
        </div>

        <div v-if="loading" class="flex items-center justify-center py-16">
          <div class="size-8 animate-spin rounded-full border-4 border-[#e2e8f0] border-t-[#155e75] dark:border-gray-600 dark:border-t-teal-500"></div>
        </div>

        <div v-else>
          <div class="overflow-x-auto rounded-lg border border-[#e2e8f0] dark:border-gray-700">
            <table class="w-full">
              <thead>
                <tr class="border-b border-[#e2e8f0] bg-[#f8fafc] dark:border-gray-700 dark:bg-gray-900">
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b] dark:text-gray-400">{{ t('score.course') }}</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b] dark:text-gray-400">{{ t('score.score') }}</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b] dark:text-gray-400">{{ t('score.level') }}</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b] dark:text-gray-400">{{ t('score.examType') }}</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b] dark:text-gray-400">{{ t('score.credit') }}</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b] dark:text-gray-400">{{ t('ranking.classRanking') }}</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b] dark:text-gray-400">{{ t('ranking.gradeRanking') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="course in filteredScores"
                  :key="course.id"
                  class="border-b border-[#f1f5f9] transition-colors hover:bg-[#f8fafc] dark:border-gray-700 dark:hover:bg-gray-700"
                >
                  <td class="px-4 py-3 text-sm font-medium text-[#0f172a] dark:text-white">{{ course.courseName }}</td>
                  <td class="px-4 py-3">
                    <span
                      :class="[
                        'inline-flex items-center justify-center rounded-md px-2.5 py-1 text-sm font-semibold',
                        getScoreBg(course.scoreValue),
                        getScoreColor(course.scoreValue),
                      ]"
                    >
                      {{ course.scoreValue }}
                    </span>
                  </td>
                  <td class="px-4 py-3">
                    <span
                      :class="[
                        'rounded-full px-2 py-0.5 text-xs font-medium',
                        course.scoreValue >= 90 ? 'bg-[#dcfce7] text-[#15803d] dark:bg-green-900/30 dark:text-green-400' : '',
                        course.scoreValue >= 80 && course.scoreValue < 90 ? 'bg-[#e0f2fe] text-[#155e75] dark:bg-blue-900/30 dark:text-blue-400' : '',
                        course.scoreValue >= 70 && course.scoreValue < 80 ? 'bg-[#fef3c7] text-[#b45309] dark:bg-orange-900/30 dark:text-orange-400' : '',
                        course.scoreValue >= 60 && course.scoreValue < 70 ? 'bg-[#fef2f2] text-[#dc2626] dark:bg-red-900/30 dark:text-red-400' : '',
                        course.scoreValue < 60 ? 'bg-[#f1f5f9] text-[#64748b] dark:bg-gray-700 dark:text-gray-400' : '',
                      ]"
                    >
                      {{ getScoreLabel(course.scoreValue) }}
                    </span>
                  </td>
                  <td class="px-4 py-3 text-sm text-[#475569] dark:text-gray-300">{{ course.examType }}</td>
                  <td class="px-4 py-3 text-sm text-[#475569] dark:text-gray-300">{{ course.credit }}</td>
                  <td class="px-4 py-3">
                    <span class="text-sm font-medium text-[#155e75] dark:text-blue-400">
                      {{ course.classRank }}
                      <span class="text-[#94a3b8] dark:text-gray-500">/{{ course.classTotal }}</span>
                    </span>
                  </td>
                  <td class="px-4 py-3">
                    <span class="text-sm font-medium text-[#b45309] dark:text-orange-400">
                      {{ course.gradeRank }}
                      <span class="text-[#94a3b8] dark:text-gray-500">/{{ course.gradeTotal }}</span>
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div v-if="filteredScores.length === 0" class="flex flex-col items-center justify-center py-16 text-[#94a3b8] dark:text-gray-500">
            <BookOpen class="mb-2 size-10" />
            <p class="text-sm">{{ t('common.noData') }}</p>
          </div>
        </div>
      </CardContent>
    </Card>

    <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
      <Card class="dark:border-gray-700 dark:bg-gray-800">
        <CardContent class="p-4">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-[#64748b] dark:text-gray-400">{{ t('score.score') }}</p>
              <p class="mt-1 text-2xl font-semibold text-[#0f172a] dark:text-white">{{ stats?.totalScore ?? 0 }}</p>
            </div>
            <div class="grid size-10 place-items-center rounded-md bg-[#ccfbf1] dark:bg-teal-900/30">
              <Award class="size-5 text-[#0f766e] dark:text-teal-400" />
            </div>
          </div>
        </CardContent>
      </Card>

      <Card class="dark:border-gray-700 dark:bg-gray-800">
        <CardContent class="p-4">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-[#64748b] dark:text-gray-400">{{ t('score.average') }}</p>
              <p class="mt-1 text-2xl font-semibold text-[#0f172a] dark:text-white">{{ stats?.averageScore ?? 0 }}</p>
            </div>
            <div class="grid size-10 place-items-center rounded-md bg-[#e0f2fe] dark:bg-blue-900/30">
              <TrendingUp class="size-5 text-[#155e75] dark:text-blue-400" />
            </div>
          </div>
        </CardContent>
      </Card>

      <Card class="dark:border-gray-700 dark:bg-gray-800">
        <CardContent class="p-4">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-[#64748b] dark:text-gray-400">{{ t('overview.completedCredits') }}</p>
              <p class="mt-1 text-2xl font-semibold text-[#15803d] dark:text-green-400">{{ stats?.earnedCredit ?? 0 }}</p>
            </div>
            <div class="grid size-10 place-items-center rounded-md bg-[#f0fdf4] dark:bg-green-900/30">
              <BookOpen class="size-5 text-[#15803d] dark:text-green-400" />
            </div>
          </div>
        </CardContent>
      </Card>

      <Card class="dark:border-gray-700 dark:bg-gray-800">
        <CardContent class="p-4">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-[#64748b] dark:text-gray-400">{{ t('warning.pending') }}</p>
              <p class="mt-1 text-2xl font-semibold text-[#dc2626] dark:text-red-400">{{ stats?.failCount ?? 0 }}</p>
            </div>
            <div class="grid size-10 place-items-center rounded-md bg-[#fef2f2] dark:bg-red-900/30">
              <Award class="size-5 text-[#dc2626] dark:text-red-400" />
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  </div>
</template>