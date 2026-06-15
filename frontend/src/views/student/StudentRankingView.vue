<script setup lang="ts">
import { ref, onMounted, computed } from "vue"
import { useI18n } from "vue-i18n"
import { Trophy, Medal, Award, Users, BookOpen } from "@lucide/vue"
import {
  fetchGradeTotalRanking,
  fetchCourseRanking,
  fetchSemesterOptions,
  type GradeTotalRank,
  type CourseRank,
  type SemesterOption,
} from "../../api/dashboard"

const { t } = useI18n()

const loading = ref(false)
const gradeRanking = ref<GradeTotalRank[]>([])
const courseRanking = ref<CourseRank[]>([])
const semesterOptions = ref<SemesterOption[]>([])
const selectedSemester = ref("")
const selectedExamType = ref("")
const selectedCourse = ref("")
const rankingType = ref<"grade" | "course">("grade")

const examTypes = computed(() => [
  { value: "期中", label: t('score.midterm') },
  { value: "期末", label: t('score.final') },
])

async function loadSemesterOptions() {
  try {
    semesterOptions.value = await fetchSemesterOptions()
  } catch (e) {
    console.error("Failed to load semester options", e)
  }
}

async function loadGradeRanking() {
  loading.value = true
  try {
    gradeRanking.value = await fetchGradeTotalRanking({
      academicYear: selectedSemester.value.split(" ")[0] || undefined,
      semester: selectedSemester.value.split(" ")[1] || undefined,
      examType: selectedExamType.value || undefined,
    })
  } catch (e) {
    console.error("Failed to load grade ranking", e)
  } finally {
    loading.value = false
  }
}

async function loadCourseRanking() {
  if (!selectedCourse.value) return
  loading.value = true
  try {
    courseRanking.value = await fetchCourseRanking({
      courseName: selectedCourse.value,
      academicYear: selectedSemester.value.split(" ")[0] || undefined,
      semester: selectedSemester.value.split(" ")[1] || undefined,
      examType: selectedExamType.value || undefined,
    })
  } catch (e) {
    console.error("Failed to load course ranking", e)
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

onMounted(() => {
  loadSemesterOptions()
  loadGradeRanking()
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
        <select
          v-model="rankingType"
          class="rounded-lg border border-[#e2e8f0] px-3 py-2 text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-gray-300"
          @change="rankingType === 'grade' ? loadGradeRanking() : loadCourseRanking()"
        >
          <option value="grade">{{ t('ranking.gradeRanking') }}</option>
          <option value="course">{{ t('ranking.singleCourse') }}</option>
        </select>
        <select v-model="selectedSemester" class="rounded-lg border border-[#e2e8f0] px-3 py-2 text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-gray-300">
          <option value="">{{ t('student.allSemesters') }}</option>
          <option v-for="opt in semesterOptions" :key="opt.label" :value="opt.label">
            {{ opt.label }}
          </option>
        </select>
        <select v-model="selectedExamType" class="rounded-lg border border-[#e2e8f0] px-3 py-2 text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-gray-300">
          <option value="">{{ t('student.allExamTypes') }}</option>
          <option v-for="type in examTypes" :key="type.value" :value="type.value">
            {{ type.label }}
          </option>
        </select>
        <input
          v-if="rankingType === 'course'"
          v-model="selectedCourse"
          :placeholder="t('class.inputClassName')"
          class="rounded-lg border border-[#e2e8f0] px-3 py-2 text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-gray-300"
        />
        <button
          class="rounded-lg bg-[#155e75] px-4 py-2 text-sm font-medium text-white hover:bg-[#0e7490] dark:bg-teal-700 dark:hover:bg-teal-600"
          @click="rankingType === 'grade' ? loadGradeRanking() : loadCourseRanking()"
        >
          {{ t('common.query') }}
        </button>
      </div>
    </div>

    <div v-if="loading" class="py-12 text-center text-sm text-[#64748b] dark:text-gray-400">
      {{ t('common.loading') }}
    </div>

    <template v-else>
      <!-- 年级排名 -->
      <div v-if="rankingType === 'grade'" class="space-y-4">
        <div v-if="gradeRanking.length === 0" class="py-12 text-center">
          <Trophy class="mx-auto size-10 text-[#cbd5e1] dark:text-gray-600" />
           <p class="mt-2 text-sm text-[#64748b] dark:text-gray-400">{{ t('ranking.noRankingData') }}</p>
        </div>
        <div v-else class="space-y-3">
          <div
            v-for="(student, idx) in gradeRanking"
            :key="student.studentId"
            :class="['overflow-hidden rounded-lg border', getRankBg(idx + 1), 'dark:border-gray-700 dark:bg-gray-800']"
          >
            <div class="flex items-center justify-between px-4 py-3">
              <div class="flex items-center gap-3">
                <div class="flex size-8 items-center justify-center">
                  <component :is="getRankIcon(idx + 1).icon" :class="['size-5', getRankIcon(idx + 1).color]" />
                </div>
                <div>
                  <p class="text-sm font-semibold text-[#0f172a] dark:text-white">{{ student.name }}</p>
                  <p class="text-xs text-[#64748b] dark:text-gray-400">{{ student.className }}</p>
                </div>
              </div>
              <div class="text-right">
                <p class="text-lg font-bold text-[#0f172a] dark:text-white">{{ student.totalScore }}</p>
                <p class="text-xs text-[#64748b] dark:text-gray-400">{{ t('ranking.average') }} {{ student.averageScore }}</p>
              </div>
            </div>
            <div v-if="student.courses.length > 0" class="border-t border-inherit px-4 py-2">
              <div class="flex flex-wrap gap-2">
                <span
                  v-for="course in student.courses"
                  :key="course.name"
                  class="rounded-full bg-[#f1f5f9] px-2.5 py-0.5 text-xs text-[#475569] dark:bg-gray-700 dark:text-gray-300"
                >
                  {{ course.name }}: {{ course.score }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 单科排名 -->
      <div v-else class="space-y-4">
        <div v-if="courseRanking.length === 0" class="py-12 text-center">
          <BookOpen class="mx-auto size-10 text-[#cbd5e1] dark:text-gray-600" />
           <p class="mt-2 text-sm text-[#64748b] dark:text-gray-400">{{ t('ranking.noRankingData') }}</p>
        </div>
        <div v-else class="space-y-3">
          <div
            v-for="(student, idx) in courseRanking"
            :key="student.studentId"
            :class="['overflow-hidden rounded-lg border', getRankBg(idx + 1), 'dark:border-gray-700 dark:bg-gray-800']"
          >
            <div class="flex items-center justify-between px-4 py-3">
              <div class="flex items-center gap-3">
                <div class="flex size-8 items-center justify-center">
                  <component :is="getRankIcon(idx + 1).icon" :class="['size-5', getRankIcon(idx + 1).color]" />
                </div>
                <div>
                  <p class="text-sm font-semibold text-[#0f172a] dark:text-white">{{ student.name }}</p>
                  <p class="text-xs text-[#64748b] dark:text-gray-400">{{ student.className }}</p>
                </div>
              </div>
              <div class="text-right">
                <p class="text-lg font-bold text-[#0f172a] dark:text-white">{{ student.score }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>