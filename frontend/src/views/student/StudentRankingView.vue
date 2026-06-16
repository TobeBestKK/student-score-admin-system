<script setup lang="ts">
import { ref, onMounted, computed } from "vue"
import { useI18n } from "vue-i18n"
import { Trophy, Medal, Award, Users, BookOpen, ChevronRight, X } from "@lucide/vue"
import {
  fetchGradeTotalRanking,
  fetchSemesterOptions,
  fetchStudentStats,
  fetchStudentScores,
  type GradeTotalRank,
  type SemesterOption,
  type StudentStats,
  type StudentCourseScore,
} from "../../api/dashboard"

const { t } = useI18n()

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

const loading = ref(false)
const gradeRanking = ref<GradeTotalRank[]>([])
const semesterOptions = ref<SemesterOption[]>([])
const selectedSemester = ref("")
const selectedExamType = ref("")

const studentStats = ref<StudentStats | null>(null)
const studentCourseScores = ref<StudentCourseScore[]>([])

const showGradeRankingModal = ref(false)
const showStudentDetailModal = ref(false)
const selectedStudentId = ref<number>(0)
const selectedStudentName = ref("")
const selectedStudentClass = ref("")
const selectedStudentCourseScores = ref<StudentCourseScore[]>([])

const examTypes = computed(() => [
  { value: "期中", label: t('score.midterm') },
  { value: "期末", label: t('score.final') },
])

async function loadSemesterOptions() {
  try {
    semesterOptions.value = await fetchSemesterOptions()
    if (semesterOptions.value.length > 0) {
      selectedSemester.value = semesterOptions.value[0].label
    }
    selectedExamType.value = "期末"
  } catch (e) {
    console.error("Failed to load semester options", e)
  }
}

async function loadStudentStats() {
  if (!userInfo.value?.id) return
  loading.value = true
  try {
    const yearPart = selectedSemester.value.split(" ")[0]?.replace("学年", "") || undefined
    const semesterPart = selectedSemester.value.split(" ")[1] || undefined
    const [stats, scores] = await Promise.all([
      fetchStudentStats({
        studentId: userInfo.value.id,
        academicYear: yearPart,
        semester: semesterPart,
        examType: selectedExamType.value || undefined,
      }),
      fetchStudentScores({
        studentId: userInfo.value.id,
        academicYear: yearPart,
        semester: semesterPart,
        examType: selectedExamType.value || undefined,
      }),
    ])
    studentStats.value = stats
    studentCourseScores.value = scores
  } catch (e) {
    console.error("Failed to load student stats", e)
  } finally {
    loading.value = false
  }
}

async function loadGradeRanking() {
  loading.value = true
  try {
    const yearPart = selectedSemester.value.split(" ")[0]?.replace("学年", "") || undefined
    const semesterPart = selectedSemester.value.split(" ")[1] || undefined
    gradeRanking.value = await fetchGradeTotalRanking({
      academicYear: yearPart,
      semester: semesterPart,
      examType: selectedExamType.value || undefined,
    })
  } catch (e) {
    console.error("Failed to load grade ranking", e)
  } finally {
    loading.value = false
  }
}

function getRankIcon(rank: number) {
  if (rank === 1) return { icon: Trophy, color: "text-[#f59e0b]" }
  if (rank === 2) return { icon: Medal, color: "text-[#94a3b8]" }
  if (rank === 3) return { icon: Award, color: "text-[#cd7f32]" }
  return { icon: Users, color: "text-[#64748b]" }
}

function getRankBg(rank: number) {
  if (rank === 1) return "bg-[#fef3c7] border-[#fde68a]"
  if (rank === 2) return "bg-[#f1f5f9] border-[#e2e8f0]"
  if (rank === 3) return "bg-[#fef2f2] border-[#fecaca]"
  return "bg-white border-[#e2e8f0]"
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

async function openGradeRankingModal() {
  await loadGradeRanking()
  showGradeRankingModal.value = true
}

async function closeGradeRankingModal() {
  showGradeRankingModal.value = false
}

async function openStudentDetailModal(studentId: number, name: string, className: string) {
  selectedStudentId.value = studentId
  selectedStudentName.value = name
  selectedStudentClass.value = className
  
  try {
    const yearPart = selectedSemester.value.split(" ")[0]?.replace("学年", "") || undefined
    const semesterPart = selectedSemester.value.split(" ")[1] || undefined
    selectedStudentCourseScores.value = await fetchStudentScores({
      studentId: studentId,
      academicYear: yearPart,
      semester: semesterPart,
      examType: selectedExamType.value || undefined,
    })
  } catch (e) {
    console.error("Failed to load student course scores", e)
    selectedStudentCourseScores.value = []
  }
  
  showStudentDetailModal.value = true
}

function closeStudentDetailModal() {
  showStudentDetailModal.value = false
}

function handleQuery() {
  loadStudentStats()
  loadGradeRanking()
}

onMounted(async () => {
  await loadSemesterOptions()
  await loadStudentStats()
  await loadGradeRanking()
})
</script>

<template>
  <div class="space-y-6">
    <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
      <div>
        <h2 class="text-lg font-semibold text-[#0f172a] dark:text-white">{{ t('ranking.title') }}</h2>
        <p class="text-sm text-[#64748b] dark:text-gray-400">{{ t('ranking.viewDesc') }}</p>
      </div>
      <div class="flex flex-wrap gap-2">
        <select v-model="selectedSemester" class="rounded-lg border border-[#e2e8f0] px-3 py-2 text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-gray-300">
          <option value="">{{ t('student.allSemesters') }}</option>
          <option v-for="opt in semesterOptions" :key="opt.label" :value="opt.label">
            {{ opt.label }}
          </option>
        </select>
        <select v-model="selectedExamType" class="rounded-lg border border-[#e2e8f0] px-3 py-2 text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-gray-300">
          <option v-for="type in examTypes" :key="type.value" :value="type.value">
            {{ type.label }}
          </option>
        </select>
        <button
          class="rounded-lg bg-[#155e75] px-4 py-2 text-sm font-medium text-white hover:bg-[#0e7490] dark:bg-teal-700 dark:hover:bg-teal-600"
          @click="handleQuery"
        >
          {{ t('common.query') }}
        </button>
      </div>
    </div>

    <div v-if="loading" class="py-12 text-center text-sm text-[#64748b] dark:text-gray-400">
      {{ t('common.loading') }}
    </div>

    <template v-else>
      <div class="grid gap-6 lg:grid-cols-3">
        <div class="lg:col-span-1">
          <div class="rounded-xl border border-[#e2e8f0] bg-white p-6 dark:border-gray-700 dark:bg-gray-800">
            <h3 class="mb-4 flex items-center gap-2 text-base font-semibold text-[#0f172a] dark:text-white">
              <Trophy class="size-5 text-[#f59e0b]" />
              {{ t('ranking.myRank') }}
            </h3>
            
            <div class="space-y-4">
              <div class="rounded-lg bg-gradient-to-br from-[#fef3c7] to-[#fde68a] p-4">
                <div class="flex items-center justify-between">
                  <div>
                    <p class="text-sm text-[#92400e]">{{ t('ranking.gradeRanking') }}</p>
                    <p class="mt-1 text-3xl font-bold text-[#b45309]">
                      {{ studentStats?.gradeRank ?? '-' }}
                      <span class="text-sm font-normal text-[#92400e]">/{{ studentStats?.gradeTotal ?? '-' }}</span>
                    </p>
                  </div>
                  <div class="grid size-12 place-items-center rounded-full bg-white">
                    <Trophy class="size-6 text-[#f59e0b]" />
                  </div>
                </div>
                <p class="mt-2 text-sm text-[#92400e]">
                  {{ t('dashboard.totalScore') }}: {{ studentStats?.totalScore ?? 0 }}
                </p>
              </div>

              <div class="rounded-lg bg-gradient-to-br from-[#e0f2fe] to-[#bae6fd] p-4">
                <div class="flex items-center justify-between">
                  <div>
                    <p class="text-sm text-[#0c4a6e]">{{ t('ranking.classRanking') }}</p>
                    <p class="mt-1 text-3xl font-bold text-[#155e75]">
                      {{ studentStats?.classRank ?? '-' }}
                      <span class="text-sm font-normal text-[#0c4a6e]">/{{ studentStats?.classTotal ?? '-' }}</span>
                    </p>
                  </div>
                  <div class="grid size-12 place-items-center rounded-full bg-white">
                    <Users class="size-6 text-[#0ea5e9]" />
                  </div>
                </div>
                <p class="mt-2 text-sm text-[#0c4a6e]">
                  {{ t('score.average') }}: {{ studentStats?.averageScore ?? 0 }}
                </p>
              </div>
            </div>
          </div>
        </div>

        <div class="lg:col-span-2">
          <div class="rounded-xl border border-[#e2e8f0] bg-white p-6 dark:border-gray-700 dark:bg-gray-800">
            <h3 class="mb-4 flex items-center gap-2 text-base font-semibold text-[#0f172a] dark:text-white">
              <BookOpen class="size-5 text-[#155e75]" />
              {{ t('ranking.gradeRanking') }}
            </h3>

            <div v-if="gradeRanking.length > 0" class="space-y-3">
              <div
                v-for="(student, idx) in gradeRanking.slice(0, 5)"
                :key="student.studentId"
                :class="['cursor-pointer overflow-hidden rounded-lg border', getRankBg(idx + 1), 'dark:border-gray-700 dark:bg-gray-800 hover:shadow-md transition-shadow']"
                @click="openStudentDetailModal(student.studentId, student.name, student.className)"
              >
                <div class="flex items-center justify-between px-4 py-3">
                  <div class="flex items-center gap-3">
                    <div class="flex size-8 items-center justify-center">
                      <component :is="getRankIcon(idx + 1).icon" :class="['size-5', getRankIcon(idx + 1).color]" />
                    </div>
                    <div class="flex size-8 items-center justify-center rounded-full bg-gray-100 dark:bg-gray-700">
                      <span class="text-sm font-bold text-[#64748b] dark:text-gray-300">{{ idx + 1 }}</span>
                    </div>
                    <div>
                      <p class="text-sm font-semibold text-[#0f172a] dark:text-white">{{ student.name }}</p>
                      <p class="text-xs text-[#64748b] dark:text-gray-400">{{ student.className }}</p>
                    </div>
                  </div>
                  <div class="flex items-center gap-4">
                    <div class="text-right">
                      <p class="text-lg font-bold text-[#0f172a] dark:text-white">{{ student.totalScore }}</p>
                      <p class="text-xs text-[#64748b] dark:text-gray-400">{{ t('ranking.average') }} {{ student.averageScore }}</p>
                    </div>
                    <ChevronRight class="size-4 text-[#94a3b8]" />
                  </div>
                </div>
                <div v-if="student.courses.length > 0" class="border-t border-inherit px-4 py-2">
                  <div class="flex flex-wrap gap-2">
                    <span
                      v-for="course in student.courses"
                      :key="course.courseName"
                      class="rounded-full bg-[#f1f5f9] px-2.5 py-0.5 text-xs text-[#475569] dark:bg-gray-700 dark:text-gray-300"
                    >
                      {{ course.courseName }}: {{ course.scoreValue }}
                    </span>
                  </div>
                </div>
              </div>

              <button
                @click="openGradeRankingModal"
                class="mt-4 w-full rounded-lg border border-[#e2e8f0] py-3 text-sm text-[#64748b] hover:bg-[#f8fafc] dark:border-gray-700 dark:text-gray-400 dark:hover:bg-gray-700 transition-colors"
              >
                {{ t('common.viewMore') }}
              </button>
            </div>

            <div v-else class="py-12 text-center">
              <BookOpen class="mx-auto size-10 text-[#cbd5e1] dark:text-gray-600" />
              <p class="mt-2 text-sm text-[#64748b] dark:text-gray-400">{{ t('ranking.noRankingData') }}</p>
            </div>
          </div>
        </div>
      </div>

      <div class="rounded-xl border border-[#e2e8f0] bg-white p-6 dark:border-gray-700 dark:bg-gray-800">
        <h3 class="mb-4 flex items-center gap-2 text-base font-semibold text-[#0f172a] dark:text-white">
          <BookOpen class="size-5 text-[#155e75]" />
          {{ t('ranking.courseRanking') }}
        </h3>

        <div v-if="studentCourseScores.length > 0" class="space-y-3">
          <div
            v-for="course in studentCourseScores"
            :key="course.id"
            class="flex items-center justify-between rounded-lg border border-[#f1f5f9] p-4"
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
                <p class="text-xs text-[#64748b]">{{ course.examType }} · {{ course.credit }} {{ t('unit.credit') }}</p>
              </div>
            </div>
            <div class="flex items-center gap-6">
              <div class="text-right">
                <p class="text-xs text-[#64748b]">{{ t('ranking.classRanking') }}</p>
                <p class="text-sm font-medium text-[#155e75]">
                  {{ course.classRank }}<span class="text-xs text-[#94a3b8]">/{{ course.classTotal }}</span>
                </p>
              </div>
              <div class="text-right">
                <p class="text-xs text-[#64748b]">{{ t('ranking.gradeRanking') }}</p>
                <p class="text-sm font-medium text-[#b45309]">
                  {{ course.gradeRank }}<span class="text-xs text-[#94a3b8]">/{{ course.gradeTotal }}</span>
                </p>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="py-8 text-center text-sm text-[#94a3b8]">
          {{ t('overview.noScoreData') }}
        </div>
      </div>
    </template>
  </div>

  <Teleport to="body">
    <div
      v-if="showGradeRankingModal"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
      @click.self="closeGradeRankingModal"
    >
      <div class="w-full max-w-3xl rounded-xl border border-[#e2e8f0] bg-white shadow-2xl dark:border-gray-700 dark:bg-gray-800">
        <div class="flex items-center justify-between border-b border-[#e2e8f0] p-4 dark:border-gray-700">
          <h3 class="text-lg font-semibold text-[#0f172a] dark:text-white">{{ t('ranking.gradeRanking') }}</h3>
          <button
            @click="closeGradeRankingModal"
            class="rounded-full p-1 hover:bg-[#f1f5f9] dark:hover:bg-gray-700 transition-colors"
          >
            <X class="size-5 text-[#64748b] dark:text-gray-400" />
          </button>
        </div>

        <div class="max-h-[60vh] overflow-y-auto p-4">
          <div v-if="gradeRanking.length === 0" class="py-12 text-center">
            <Trophy class="mx-auto size-10 text-[#cbd5e1] dark:text-gray-600" />
            <p class="mt-2 text-sm text-[#64748b] dark:text-gray-400">{{ t('ranking.noRankingData') }}</p>
          </div>

          <div v-else class="space-y-3">
            <div
              v-for="(student, idx) in gradeRanking"
              :key="student.studentId"
              :class="['cursor-pointer overflow-hidden rounded-lg border', getRankBg(idx + 1), 'dark:border-gray-700 dark:bg-gray-800 hover:shadow-md transition-shadow']"
              @click="openStudentDetailModal(student.studentId, student.name, student.className)"
            >
              <div class="flex items-center justify-between px-4 py-3">
                <div class="flex items-center gap-3">
                  <div class="flex size-8 items-center justify-center">
                    <component :is="getRankIcon(idx + 1).icon" :class="['size-5', getRankIcon(idx + 1).color]" />
                  </div>
                  <div class="flex size-8 items-center justify-center rounded-full bg-gray-100 dark:bg-gray-700">
                    <span class="text-sm font-bold text-[#64748b] dark:text-gray-300">{{ idx + 1 }}</span>
                  </div>
                  <div>
                    <p class="text-sm font-semibold text-[#0f172a] dark:text-white">{{ student.name }}</p>
                    <p class="text-xs text-[#64748b] dark:text-gray-400">{{ student.className }}</p>
                  </div>
                </div>
                <div class="flex items-center gap-4">
                  <div class="text-right">
                    <p class="text-lg font-bold text-[#0f172a] dark:text-white">{{ student.totalScore }}</p>
                    <p class="text-xs text-[#64748b] dark:text-gray-400">{{ t('ranking.average') }} {{ student.averageScore }}</p>
                  </div>
                  <ChevronRight class="size-4 text-[#94a3b8]" />
                </div>
              </div>
              <div v-if="student.courses.length > 0" class="border-t border-inherit px-4 py-2">
                <div class="flex flex-wrap gap-2">
                  <span
                    v-for="course in student.courses"
                    :key="course.courseName"
                    class="rounded-full bg-[#f1f5f9] px-2.5 py-0.5 text-xs text-[#475569] dark:bg-gray-700 dark:text-gray-300"
                  >
                    {{ course.courseName }}: {{ course.scoreValue }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div
      v-if="showStudentDetailModal"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
      @click.self="closeStudentDetailModal"
    >
      <div class="w-full max-w-xl rounded-xl border border-[#e2e8f0] bg-white shadow-2xl dark:border-gray-700 dark:bg-gray-800">
        <div class="flex items-center justify-between border-b border-[#e2e8f0] p-4 dark:border-gray-700">
          <div>
            <h3 class="text-lg font-semibold text-[#0f172a] dark:text-white">{{ selectedStudentName }}</h3>
            <p class="text-sm text-[#64748b] dark:text-gray-400">{{ selectedStudentClass }}</p>
          </div>
          <button
            @click="closeStudentDetailModal"
            class="rounded-full p-1 hover:bg-[#f1f5f9] dark:hover:bg-gray-700 transition-colors"
          >
            <X class="size-5 text-[#64748b] dark:text-gray-400" />
          </button>
        </div>

        <div class="max-h-[60vh] overflow-y-auto p-4">
          <div v-if="selectedStudentCourseScores.length === 0" class="py-12 text-center">
            <BookOpen class="mx-auto size-10 text-[#cbd5e1] dark:text-gray-600" />
            <p class="mt-2 text-sm text-[#64748b] dark:text-gray-400">{{ t('overview.noScoreData') }}</p>
          </div>

          <div v-else class="space-y-3">
            <div
              v-for="course in selectedStudentCourseScores"
              :key="course.id"
              class="flex items-center justify-between rounded-lg border border-[#f1f5f9] p-4"
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
                  <p class="text-xs text-[#64748b]">{{ course.examType }} · {{ course.credit }} {{ t('unit.credit') }}</p>
                </div>
              </div>
              <div class="flex items-center gap-6">
                <div class="text-right">
                  <p class="text-xs text-[#64748b]">{{ t('ranking.classRanking') }}</p>
                  <p class="text-sm font-medium text-[#155e75]">
                    {{ course.classRank }}<span class="text-xs text-[#94a3b8]">/{{ course.classTotal }}</span>
                  </p>
                </div>
                <div class="text-right">
                  <p class="text-xs text-[#64748b]">{{ t('ranking.gradeRanking') }}</p>
                  <p class="text-sm font-medium text-[#b45309]">
                    {{ course.gradeRank }}<span class="text-xs text-[#94a3b8]">/{{ course.gradeTotal }}</span>
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>