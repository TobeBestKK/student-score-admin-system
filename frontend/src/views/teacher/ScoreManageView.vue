<script setup lang="ts">
import { ref, onMounted, watch, computed } from "vue"
import { useI18n } from "vue-i18n"
import { FileText, Search, Plus, Pencil, Trash2, X, BarChart3, CheckCircle, XCircle, Hash } from "@lucide/vue"
import {
  fetchScoreRecords,
  createScoreRecord,
  updateScoreRecord,
  deleteScoreRecord,
  type ScoreRecord,
  type ScoreRecordCreate,
  type ScoreRecordUpdate,
  type ScoreQuery,
} from "@/api/score"
import { fetchCourseOptions, fetchSemesterOptions, type CourseOption, type SemesterOption } from "@/api/dashboard"
import { fetchAllStudents } from "@/api/student"
import Card from "@/components/ui/card/Card.vue"
import CardContent from "@/components/ui/card/CardContent.vue"
import CardHeader from "@/components/ui/card/CardHeader.vue"
import CardTitle from "@/components/ui/card/CardTitle.vue"
import Button from "@/components/ui/button/Button.vue"
import Input from "@/components/ui/input/Input.vue"
import Label from "@/components/ui/label/Label.vue"
import { EXAM_TYPE, GRADE_LEVEL, REMARK } from "@/constants/enum"

const { t } = useI18n()

interface StudentOption {
  id: number
  name: string
  studentNo: string
}

const loading = ref(false)
const scoreList = ref<ScoreRecord[]>([])
const totalElements = ref(0)
const totalPages = ref(0)

const semesterOptions = ref<SemesterOption[]>([])
const courseOptions = ref<CourseOption[]>([])
const studentOptions = ref<StudentOption[]>([])

const query = ref<ScoreQuery>({
  academicYear: undefined,
  semester: undefined,
  courseId: undefined,
  examType: undefined,
  keyword: undefined,
  page: 0,
  size: 10,
})

const selectedSemester = ref("")
const stats = ref({ totalRecords: 0, averageScore: 0, passRate: 0, failCount: 0 })

const examTypeOptions = computed(() => [
  { value: "", label: t('student.allExamTypes') },
  { value: EXAM_TYPE.MIDTERM, label: t('score.midterm') },
  { value: EXAM_TYPE.FINAL, label: t('score.final') },
])

const showModal = ref(false)
const modalMode = ref<"create" | "edit">("create")
const currentRecord = ref<ScoreRecord | null>(null)
const formLoading = ref(false)

const remarkOptions = computed(() => [
  { value: REMARK.EXCELLENT, label: t('score.remarkExcellent') },
  { value: REMARK.GOOD, label: t('score.remarkGood') },
  { value: REMARK.MEDIUM, label: t('score.remarkMedium') },
  { value: REMARK.POOR, label: t('score.remarkPoor') },
])

const form = ref<ScoreRecordCreate>({
  studentId: 0,
  courseId: 0,
  scoreValue: 0,
  examType: EXAM_TYPE.FINAL,
  remark: "",
})

const showDeleteModal = ref(false)
const deleteTarget = ref<ScoreRecord | null>(null)

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

async function loadCourses() {
  try {
    const semesterParams = getSemesterParams()
    courseOptions.value = await fetchCourseOptions(semesterParams)
  } catch (e) {
    console.error("Failed to load courses", e)
  }
}

async function loadStudents() {
  try {
    const res = await fetchAllStudents()
    studentOptions.value = res.map((s: any) => ({ id: s.id, name: s.name, studentNo: s.studentNo }))
  } catch (e) {
    console.error("Failed to load students", e)
  }
}

async function loadScores() {
  loading.value = true
  try {
    const semesterParams = getSemesterParams()
    const params: ScoreQuery = {
      ...semesterParams,
      ...query.value,
    }
    const res = await fetchScoreRecords(params)
    scoreList.value = res.content
    totalElements.value = res.totalElements
    totalPages.value = res.totalPages

    // 计算统计数据
    stats.value.totalRecords = res.totalElements
    if (res.content.length > 0) {
      const scores = res.content.map(s => s.scoreValue)
      const avg = scores.reduce((a, b) => a + b, 0) / scores.length
      stats.value.averageScore = Math.round(avg * 10) / 10
      const passCount = scores.filter(s => s >= 60).length
      stats.value.passRate = Math.round((passCount / scores.length) * 100 * 10) / 10
      stats.value.failCount = scores.length - passCount
    } else {
      stats.value.averageScore = 0
      stats.value.passRate = 0
      stats.value.failCount = 0
    }
  } catch (e) {
    console.error("Failed to load scores", e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.value.page = 0
  loadScores()
}

function handlePageChange(page: number) {
  query.value.page = page
  loadScores()
}

async function openCreateModal() {
  modalMode.value = "create"
  currentRecord.value = null
  if (studentOptions.value.length === 0) await loadStudents()
  if (courseOptions.value.length === 0) await loadCourses()
  form.value = {
    studentId: studentOptions.value[0]?.id || 0,
    courseId: courseOptions.value[0]?.id || 0,
    scoreValue: 0,
    examType: EXAM_TYPE.FINAL,
    remark: "",
  }
  showModal.value = true
}

function openEditModal(record: ScoreRecord) {
  modalMode.value = "edit"
  currentRecord.value = record
  form.value = {
    studentId: record.studentId,
    courseId: record.courseId,
    scoreValue: record.scoreValue,
    examType: record.examType,
    remark: record.remark || "",
  }
  showModal.value = true
}

function closeModal() {
  showModal.value = false
  currentRecord.value = null
}

async function handleSubmit() {
  if (!form.value.studentId || form.value.studentId <= 0) {
    alert(t('score.selectStudent'))
    return
  }
  if (!form.value.courseId || form.value.courseId <= 0) {
    alert(t('score.selectCourse'))
    return
  }
  const score = Number(form.value.scoreValue)
  if (form.value.scoreValue === null || form.value.scoreValue === undefined || isNaN(score) || score < 0 || score > 100) {
    alert(t('score.scoreRequired'))
    return
  }
  if (!form.value.examType) {
    alert(t('score.selectExamType'))
    return
  }

  formLoading.value = true
  try {
    const submitData = {
      studentId: form.value.studentId,
      courseId: form.value.courseId,
      scoreValue: score,
      examType: form.value.examType,
      remark: form.value.remark || undefined,
    }
    if (modalMode.value === "create") {
      await createScoreRecord(submitData)
    } else if (currentRecord.value) {
      const updateData: ScoreRecordUpdate = {
        scoreValue: score,
        examType: form.value.examType,
        remark: form.value.remark || undefined,
      }
      await updateScoreRecord(currentRecord.value.id, updateData)
    }
    closeModal()
    loadScores()
  } catch (e: any) {
    console.error("Failed to save score", e)
    const msg = e?.response?.data?.message || e?.message || t('message.operationFailed')
    alert(t('message.saveFailed') + msg)
  } finally {
    formLoading.value = false
  }
}

function confirmDelete(record: ScoreRecord) {
  deleteTarget.value = record
  showDeleteModal.value = true
}

function closeDeleteModal() {
  showDeleteModal.value = false
  deleteTarget.value = null
}

async function handleDelete() {
  if (!deleteTarget.value) return
  try {
    await deleteScoreRecord(deleteTarget.value.id)
    closeDeleteModal()
    loadScores()
  } catch (e: any) {
    console.error("Failed to delete score", e)
    const msg = e?.response?.data?.message || e?.message || t('message.deleteFailed')
    alert(t('message.deleteFailed') + '：' + msg)
  }
}

function getGradeColor(grade: string) {
  if (grade === GRADE_LEVEL.EXCELLENT) return "bg-[#dcfce7] text-[#15803d]"
  if (grade === GRADE_LEVEL.GOOD) return "bg-[#e0f2fe] text-[#155e75]"
  if (grade === GRADE_LEVEL.MEDIUM) return "bg-[#fef3c7] text-[#b45309]"
  if (grade === GRADE_LEVEL.PASS) return "bg-[#fef2f2] text-[#dc2626]"
  return "bg-[#f1f5f9] text-[#64748b]"
}

function getRemarkColor(remark: string | null) {
  if (remark === REMARK.EXCELLENT) return "bg-[#dcfce7] text-[#15803d]"
  if (remark === REMARK.GOOD) return "bg-[#e0f2fe] text-[#155e75]"
  if (remark === REMARK.MEDIUM) return "bg-[#fef3c7] text-[#b45309]"
  if (remark === REMARK.POOR) return "bg-[#fef2f2] text-[#dc2626]"
  return "bg-[#f1f5f9] text-[#64748b]"
}

function formatDate(dateStr: string) {
  if (!dateStr) return ""
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`
}

watch(selectedSemester, () => {
  query.value.page = 0
  loadCourses()
  loadScores()
})

onMounted(async () => {
  await Promise.all([loadSemesters(), loadCourses(), loadStudents()])
  await loadScores()
})
</script>

<template>
  <div class="space-y-6">
    <Card class="dark:border-gray-700 dark:bg-gray-800">
      <CardHeader>
        <CardTitle class="flex items-center gap-2 text-base dark:text-white">
          <FileText class="size-5 text-[#155e75] dark:text-teal-400" />
          {{ t('score.management') }}
        </CardTitle>
      </CardHeader>
      <CardContent>
        <div class="mb-6 flex flex-wrap items-end gap-4">
          <div class="flex flex-col gap-1.5">
            <Label class="text-xs dark:text-gray-400">{{ t('score.semester') }}</Label>
            <select
              v-model="selectedSemester"
              class="h-9 rounded-md border border-[#e2e8f0] bg-white px-3 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75] dark:bg-gray-700 dark:border-gray-600 dark:text-gray-300"
            >
              <option value="">{{ t('student.allSemesters') }}</option>
              <option v-for="opt in semesterOptions" :key="opt.academicYear + '-' + opt.semester" :value="opt.academicYear + '-' + opt.semester">
                {{ opt.label }}
              </option>
            </select>
          </div>

          <div class="flex flex-col gap-1.5">
            <Label class="text-xs dark:text-gray-400">{{ t('score.course') }}</Label>
            <select
              v-model="query.courseId"
              class="h-9 rounded-md border border-[#e2e8f0] bg-white px-3 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75] dark:bg-gray-700 dark:border-gray-600 dark:text-gray-300"
            >
              <option :value="undefined">{{ t('student.allCourses') }}</option>
              <option v-for="c in courseOptions" :key="c.id" :value="c.id">
                {{ c.courseName }}
              </option>
            </select>
          </div>

          <div class="flex flex-col gap-1.5">
            <Label class="text-xs dark:text-gray-400">{{ t('score.examType') }}</Label>
            <select
              v-model="query.examType"
              class="h-9 rounded-md border border-[#e2e8f0] bg-white px-3 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75] dark:bg-gray-700 dark:border-gray-600 dark:text-gray-300"
            >
              <option v-for="opt in examTypeOptions" :key="opt.value" :value="opt.value">
                {{ opt.label }}
              </option>
            </select>
          </div>

          <div class="flex flex-col gap-1.5">
            <Label class="text-xs dark:text-gray-400">{{ t('student.nameAndId') }}</Label>
            <Input v-model="query.keyword" :placeholder="t('student.searchPlaceholder')" class="w-48 dark:bg-gray-700 dark:border-gray-600 dark:text-gray-300" />
          </div>

          <Button @click="handleSearch" class="bg-[#155e75] hover:bg-[#0e4a5e] dark:bg-teal-700 dark:hover:bg-teal-600">
            <Search class="mr-1.5 size-4" />
            {{ t('common.query') }}
          </Button>

          <Button @click="openCreateModal" class="bg-[#0f766e] hover:bg-[#0d5d57] dark:bg-teal-800 dark:hover:bg-teal-700">
            <Plus class="mr-1.5 size-4" />
            {{ t('score.enterScore') }}
          </Button>
        </div>

        <div class="mb-6 grid gap-4 grid-cols-2 lg:grid-cols-4">
          <div class="rounded-lg border border-[#e2e8f0] bg-white p-4 dark:border-gray-700 dark:bg-gray-800">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-[#64748b] dark:text-gray-400">{{ t('score.totalRecords') }}</p>
                <p class="mt-1 text-2xl font-semibold text-[#0f172a] dark:text-white">{{ stats.totalRecords }}</p>
              </div>
              <div class="grid size-10 place-items-center rounded-md bg-[#ccfbf1] dark:bg-teal-900">
                <Hash class="size-5 text-[#0f766e] dark:text-teal-400" />
              </div>
            </div>
          </div>

          <div class="rounded-lg border border-[#e2e8f0] bg-white p-4 dark:border-gray-700 dark:bg-gray-800">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-[#64748b] dark:text-gray-400">{{ t('score.average') }}</p>
                <p class="mt-1 text-2xl font-semibold text-[#0f172a] dark:text-white">{{ stats.averageScore }}</p>
              </div>
              <div class="grid size-10 place-items-center rounded-md bg-[#e0f2fe] dark:bg-cyan-900">
                <BarChart3 class="size-5 text-[#155e75] dark:text-cyan-400" />
              </div>
            </div>
          </div>

          <div class="rounded-lg border border-[#e2e8f0] bg-white p-4 dark:border-gray-700 dark:bg-gray-800">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-[#64748b] dark:text-gray-400">{{ t('score.passRate') }}</p>
                <p class="mt-1 text-2xl font-semibold text-[#15803d] dark:text-green-400">{{ stats.passRate }}%</p>
              </div>
              <div class="grid size-10 place-items-center rounded-md bg-[#f0fdf4] dark:bg-green-900">
                <CheckCircle class="size-5 text-[#15803d] dark:text-green-400" />
              </div>
            </div>
          </div>

          <div class="rounded-lg border border-[#e2e8f0] bg-white p-4 dark:border-gray-700 dark:bg-gray-800">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-[#64748b] dark:text-gray-400">{{ t('score.failCount') }}</p>
                <p class="mt-1 text-2xl font-semibold text-[#dc2626] dark:text-red-400">{{ stats.failCount }}</p>
              </div>
              <div class="grid size-10 place-items-center rounded-md bg-[#fef2f2] dark:bg-red-900">
                <XCircle class="size-5 text-[#dc2626] dark:text-red-400" />
              </div>
            </div>
          </div>
        </div>

        <div v-if="loading" class="flex items-center justify-center py-16">
          <div class="size-8 animate-spin rounded-full border-4 border-[#e2e8f0] border-t-[#155e75] dark:border-gray-600 dark:border-t-teal-500"></div>
        </div>

        <div v-else>
          <div class="overflow-x-auto rounded-lg border border-[#e2e8f0] dark:border-gray-700">
            <table class="w-full">
              <thead>
                <tr class="border-b border-[#e2e8f0] bg-[#f8fafc] dark:border-gray-700 dark:bg-gray-700">
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b] dark:text-gray-300">{{ t('student.name') }}</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b] dark:text-gray-300">{{ t('student.studentId') }}</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b] dark:text-gray-300">{{ t('student.class') }}</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b] dark:text-gray-300">{{ t('score.course') }}</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b] dark:text-gray-300">{{ t('score.score') }}</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b] dark:text-gray-300">{{ t('score.level') }}</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b] dark:text-gray-300">{{ t('score.examType') }}</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b] dark:text-gray-300">{{ t('score.comment') }}</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b] dark:text-gray-300">{{ t('score.recordTime') }}</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b] dark:text-gray-300">{{ t('common.operation') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="record in scoreList"
                  :key="record.id"
                  class="border-b border-[#f1f5f9] transition-colors hover:bg-[#f8fafc] dark:border-gray-700 dark:hover:bg-gray-700"
                >
                  <td class="px-4 py-3 text-sm font-medium text-[#0f172a] dark:text-white">{{ record.studentName }}</td>
                  <td class="px-4 py-3 text-sm text-[#475569] dark:text-gray-400">{{ record.studentNo }}</td>
                  <td class="px-4 py-3 text-sm text-[#475569] dark:text-gray-400">{{ record.className }}</td>
                  <td class="px-4 py-3 text-sm text-[#475569] dark:text-gray-400">{{ record.courseName }}</td>
                  <td class="px-4 py-3">
                    <span
                      :class="[
                        'inline-flex items-center justify-center rounded-md px-2.5 py-1 text-sm font-semibold',
                        getGradeColor(record.grade),
                      ]"
                    >
                      {{ record.scoreValue }}
                    </span>
                  </td>
                  <td class="px-4 py-3">
                    <span
                      :class="[
                        'rounded-full px-2.5 py-0.5 text-xs font-medium',
                        getGradeColor(record.grade),
                      ]"
                    >
                      {{ record.grade }}
                    </span>
                  </td>
                  <td class="px-4 py-3 text-sm text-[#475569] dark:text-gray-400">{{ record.examType }}</td>
                  <td class="px-4 py-3">
                    <span
                      v-if="record.remark"
                      :class="[
                        'rounded-full px-2.5 py-0.5 text-xs font-medium',
                        getRemarkColor(record.remark),
                      ]"
                    >
                      {{ record.remark }}
                    </span>
                    <span v-else class="text-xs text-[#94a3b8] dark:text-gray-500">—</span>
                  </td>
                  <td class="px-4 py-3 text-sm text-[#64748b] dark:text-gray-400">{{ formatDate(record.createTime) }}</td>
                  <td class="px-4 py-3">
                    <div class="flex items-center gap-2">
                      <Button
                        @click="openEditModal(record)"
                        class="h-7 bg-[#f1f5f9] px-2 text-xs text-[#475569] hover:bg-[#e2e8f0] dark:bg-gray-700 dark:text-gray-300 dark:hover:bg-gray-600"
                      >
                        <Pencil class="mr-1 size-3" />
                        {{ t('common.edit') }}
                      </Button>
                      <Button
                        @click="confirmDelete(record)"
                        class="h-7 bg-[#fef2f2] px-2 text-xs text-[#dc2626] hover:bg-[#fee2e2] dark:bg-red-900 dark:text-red-400 dark:hover:bg-red-800"
                      >
                        <Trash2 class="mr-1 size-3" />
                        {{ t('common.delete') }}
                      </Button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div v-if="scoreList.length === 0" class="flex flex-col items-center justify-center py-16 text-[#94a3b8] dark:text-gray-500">
            <FileText class="mb-2 size-10" />
            <p class="text-sm">{{ t('common.noData') }}</p>
          </div>

          <div v-if="totalPages > 1" class="mt-4 flex items-center justify-center gap-2">
            <Button
              v-for="p in totalPages"
              :key="p"
              @click="handlePageChange(p - 1)"
              :class="[
                'h-8 px-3 text-sm',
                query.page === p - 1
                  ? 'bg-[#155e75] text-white hover:bg-[#0e4a5e]'
                  : 'bg-[#f1f5f9] text-[#475569] hover:bg-[#e2e8f0]',
              ]"
            >
              {{ p }}
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>

    <!-- 新增/编辑弹窗 -->
    <div
      v-if="showModal"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/50"
      @click.self="closeModal"
    >
      <div class="w-full max-w-md rounded-lg bg-white p-6 shadow-[0_12px_36px_rgba(15,23,42,0.10)]">
        <div class="mb-4 flex items-center justify-between">
          <h3 class="text-base font-semibold text-[#0f172a]">
            {{ modalMode === "create" ? t('score.enterScore') : t('score.editScore') }}
          </h3>
          <Button @click="closeModal" class="h-7 w-7 bg-transparent p-0 text-[#64748b] hover:bg-[#f1f5f9]">
            <X class="size-4" />
          </Button>
        </div>

        <form @submit.prevent="handleSubmit" class="space-y-4">
          <div class="flex flex-col gap-1.5">
            <Label class="text-xs">{{ t('student.name') }} *</Label>
            <select
              v-model="form.studentId"
              class="h-9 rounded-md border border-[#e2e8f0] bg-white px-3 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75]"
              :disabled="modalMode === 'edit'"
              required
            >
              <option v-for="s in studentOptions" :key="s.id" :value="s.id">
                {{ s.name }} ({{ s.studentNo }})
              </option>
            </select>
          </div>

          <div class="flex flex-col gap-1.5">
            <Label class="text-xs">{{ t('score.course') }} *</Label>
            <select
              v-model="form.courseId"
              class="h-9 rounded-md border border-[#e2e8f0] bg-white px-3 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75]"
              :disabled="modalMode === 'edit'"
              required
            >
              <option v-for="c in courseOptions" :key="c.id" :value="c.id">
                {{ c.courseName }}
              </option>
            </select>
          </div>

          <div class="flex flex-col gap-1.5">
            <Label class="text-xs">{{ t('score.score') }} * (0-100)</Label>
            <Input
              v-model="form.scoreValue"
              type="number"
              min="0"
              max="100"
              step="0.01"
              :placeholder="t('common.pleaseInput') + t('score.score')"
              required
            />
          </div>

          <div class="flex flex-col gap-1.5">
            <Label class="text-xs">{{ t('score.examType') }} *</Label>
            <select
              v-model="form.examType"
              class="h-9 rounded-md border border-[#e2e8f0] bg-white px-3 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75]"
              required
            >
              <option :value="EXAM_TYPE.MIDTERM">{{ t('score.midterm') }}</option>
              <option :value="EXAM_TYPE.FINAL">{{ t('score.final') }}</option>
            </select>
          </div>

          <div class="flex flex-col gap-1.5">
            <Label class="text-xs">{{ t('score.comment') }}</Label>
            <div class="flex gap-2">
              <button
                v-for="opt in remarkOptions"
                :key="opt.value"
                type="button"
                @click="form.remark = form.remark === opt.value ? '' : opt.value"
                :class="[
                  'rounded-md border px-3 py-1.5 text-sm font-medium transition-colors',
                  form.remark === opt.value
                    ? 'border-[#155e75] bg-[#ccfbf1] text-[#0f766e]'
                    : 'border-[#e2e8f0] bg-white text-[#475569] hover:border-[#cbd5e1]',
                ]"
              >
                {{ opt.label }}
              </button>
            </div>
            <Input v-model="form.remark" :placeholder="t('score.inputComment')" class="mt-1" />
          </div>

          <div class="flex justify-end gap-2 pt-4">
            <Button @click="closeModal" type="button" class="bg-[#f1f5f9] text-[#475569] hover:bg-[#e2e8f0]">
              {{ t('common.cancel') }}
            </Button>
            <Button type="submit" class="bg-[#155e75] hover:bg-[#0e4a5e]" :disabled="formLoading">
              {{ formLoading ? t('score.entering') : (modalMode === "create" ? t('common.enter') : t('common.save')) }}
            </Button>
          </div>
        </form>
      </div>
    </div>

    <!-- 删除确认弹窗 -->
    <div
      v-if="showDeleteModal"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/50"
      @click.self="closeDeleteModal"
    >
      <div class="w-full max-w-sm rounded-lg bg-white p-6 shadow-[0_12px_36px_rgba(15,23,42,0.10)]">
        <h3 class="text-base font-semibold text-[#0f172a]">{{ t('score.confirmDelete') }}</h3>
        <p class="mt-2 text-sm text-[#475569]">
          {{ t('message.deleteConfirm') }}
        </p>
        <div class="mt-6 flex justify-end gap-2">
          <Button @click="closeDeleteModal" class="bg-[#f1f5f9] text-[#475569] hover:bg-[#e2e8f0]">
            {{ t('common.cancel') }}
          </Button>
          <Button @click="handleDelete" class="bg-[#dc2626] hover:bg-[#b91c1c]">
            {{ t('common.delete') }}
          </Button>
        </div>
      </div>
    </div>
  </div>
</template>
