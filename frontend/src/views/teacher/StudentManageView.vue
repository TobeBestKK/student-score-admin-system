<script setup lang="ts">
import { ref, onMounted, computed } from "vue"
import { useI18n } from "vue-i18n"
import { Search, Users, Plus, Pencil, Trash2, X } from "@lucide/vue"
import {
  fetchStudents,
  createStudent,
  updateStudent,
  deleteStudent,
  type Student,
  type StudentQuery,
  type StudentCreate,
  type StudentUpdate,
  type PageResponse,
} from "@/api/student"
import { fetchClasses, type ClassItem } from "@/api/class"
import Card from "@/components/ui/card/Card.vue"
import CardContent from "@/components/ui/card/CardContent.vue"
import CardHeader from "@/components/ui/card/CardHeader.vue"
import CardTitle from "@/components/ui/card/CardTitle.vue"
import Button from "@/components/ui/button/Button.vue"
import Input from "@/components/ui/input/Input.vue"
import Label from "@/components/ui/label/Label.vue"

const { t } = useI18n()

const loading = ref(false)
const studentList = ref<Student[]>([])
const classList = ref<ClassItem[]>([])
const totalElements = ref(0)
const totalPages = ref(0)

const gradeOptions = [
  { label: t('class.grade3'), value: 3 },
]

const query = ref<StudentQuery>({
  grade: undefined,
  classId: undefined,
  keyword: undefined,
  page: 0,
  size: 10,
})

const showModal = ref(false)
const modalMode = ref<"create" | "edit">("create")
const currentStudent = ref<Student | null>(null)

const form = ref<StudentCreate>({
  studentNo: "",
  name: "",
  gender: 1,
  classId: 0,
  phone: "",
  email: "",
  enrollmentYear: new Date().getFullYear(),
})

const filteredClassList = computed(() => {
  if (!query.value.grade) return classList.value
  return classList.value
    .filter((c) => c.grade === query.value.grade)
    .sort((a, b) => a.className.localeCompare(b.className))
})

async function loadClasses() {
  try {
    const classes = await fetchClasses()
    classList.value = classes.sort((a, b) => a.className.localeCompare(b.className))
  } catch (e) {
    console.error("Failed to load classes", e)
  }
}

async function loadStudents() {
  loading.value = true
  try {
    const res: PageResponse<Student> = await fetchStudents(query.value)
    studentList.value = res.content
    totalElements.value = res.totalElements
    totalPages.value = res.totalPages
  } catch (e) {
    console.error("Failed to load students", e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.value.page = 0
  loadStudents()
}

function handlePageChange(page: number) {
  query.value.page = page
  loadStudents()
}

function openCreateModal() {
  modalMode.value = "create"
  currentStudent.value = null
  form.value = {
    studentNo: "",
    name: "",
    gender: 1,
    classId: filteredClassList.value[0]?.id || 0,
    phone: "",
    email: "",
    enrollmentYear: new Date().getFullYear(),
  }
  showModal.value = true
}

function openEditModal(student: Student) {
  modalMode.value = "edit"
  currentStudent.value = student
  form.value = {
    studentNo: student.studentNo,
    name: student.name,
    gender: student.gender,
    classId: student.classId,
    phone: student.phone || "",
    email: student.email || "",
    enrollmentYear: student.enrollmentYear || new Date().getFullYear(),
  }
  showModal.value = true
}

function closeModal() {
  showModal.value = false
  currentStudent.value = null
}

async function handleSubmit() {
  try {
    if (modalMode.value === "create") {
      await createStudent(form.value)
    } else if (currentStudent.value) {
      const updateData: StudentUpdate = {
        studentNo: form.value.studentNo,
        name: form.value.name,
        gender: form.value.gender,
        classId: form.value.classId,
        phone: form.value.phone,
        email: form.value.email,
        enrollmentYear: form.value.enrollmentYear,
      }
      await updateStudent(currentStudent.value.id, updateData)
    }
    closeModal()
    loadStudents()
  } catch (e) {
    console.error("Failed to save student", e)
    alert(t('message.operationFailedCheck'))
  }
}

async function handleDelete(student: Student) {
  if (!confirm(t('student.deleteConfirm').replace('{name}', student.name))) return
  try {
    await deleteStudent(student.id)
    loadStudents()
  } catch (e) {
    console.error("Failed to delete student", e)
    alert(t('message.deleteFailed'))
  }
}

function getGenderName(gender: number) {
  return gender === 1 ? t('student.male') : gender === 2 ? t('student.female') : t('student.unknown')
}

onMounted(() => {
  loadClasses()
  loadStudents()
})
</script>

<template>
  <div class="space-y-6">
    <Card class="dark:border-gray-700 dark:bg-gray-800">
      <CardHeader>
        <CardTitle class="flex items-center gap-2 text-base dark:text-white">
          <Users class="size-5 text-[#155e75] dark:text-teal-400" />
          {{ t('student.management') }}
        </CardTitle>
      </CardHeader>
      <CardContent>
        <div class="mb-6 flex flex-wrap items-end gap-4">
          <div class="flex flex-col gap-1.5">
            <Label class="text-xs dark:text-gray-400">{{ t('student.grade') }}</Label>
            <select
              v-model="query.grade"
              class="h-9 rounded-md border border-[#e2e8f0] bg-white px-3 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75] dark:bg-gray-700 dark:border-gray-600 dark:text-gray-300"
            >
              <option :value="undefined">{{ t('student.allGrades') }}</option>
              <option v-for="g in gradeOptions" :key="g.value" :value="g.value">
                {{ g.label }}
              </option>
            </select>
          </div>

          <div class="flex flex-col gap-1.5">
            <Label class="text-xs dark:text-gray-400">{{ t('student.class') }}</Label>
            <select
              v-model="query.classId"
              class="h-9 rounded-md border border-[#e2e8f0] bg-white px-3 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75] dark:bg-gray-700 dark:border-gray-600 dark:text-gray-300"
            >
              <option :value="undefined">{{ t('student.allClasses') }}</option>
              <option v-for="c in filteredClassList" :key="c.id" :value="c.id">
                {{ c.className }}
              </option>
            </select>
          </div>

          <div class="flex flex-col gap-1.5">
            <Label class="text-xs dark:text-gray-400">{{ t('student.studentId') }}</Label>
            <Input v-model="query.keyword" :placeholder="t('student.searchPlaceholder')" class="w-48 dark:bg-gray-700 dark:border-gray-600 dark:text-gray-300" />
          </div>

          <Button @click="handleSearch" class="bg-[#155e75] hover:bg-[#0e4a5e] dark:bg-teal-700 dark:hover:bg-teal-600">
            <Search class="mr-1.5 size-4" />
            {{ t('common.search') }}
          </Button>

          <Button @click="openCreateModal" class="bg-[#0f766e] hover:bg-[#0d5d57] dark:bg-teal-800 dark:hover:bg-teal-700">
            <Plus class="mr-1.5 size-4" />
            {{ t('student.addStudent') }}
          </Button>
        </div>

        <div v-if="loading" class="flex items-center justify-center py-16">
          <div class="size-8 animate-spin rounded-full border-4 border-[#e2e8f0] border-t-[#155e75] dark:border-gray-600 dark:border-t-teal-500"></div>
        </div>

        <div v-else>
          <div class="overflow-x-auto rounded-lg border border-[#e2e8f0] dark:border-gray-700">
            <table class="w-full">
              <thead>
                <tr class="border-b border-[#e2e8f0] bg-[#f8fafc] dark:border-gray-700 dark:bg-gray-700">
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b] dark:text-gray-300">{{ t('student.studentId') }}</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b] dark:text-gray-300">{{ t('student.name') }}</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b] dark:text-gray-300">{{ t('student.gender') }}</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b] dark:text-gray-300">{{ t('student.class') }}</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b] dark:text-gray-300">{{ t('common.phone') }}</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b] dark:text-gray-300">{{ t('common.email') }}</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b] dark:text-gray-300">{{ t('student.enrollmentYear') }}</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b] dark:text-gray-300">{{ t('common.operation') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="student in studentList"
                  :key="student.id"
                  class="border-b border-[#f1f5f9] transition-colors hover:bg-[#f8fafc] dark:border-gray-700 dark:hover:bg-gray-700"
                >
                  <td class="px-4 py-3 text-sm text-[#475569] dark:text-gray-400">{{ student.studentNo }}</td>
                  <td class="px-4 py-3 text-sm font-medium text-[#0f172a] dark:text-white">{{ student.name }}</td>
                  <td class="px-4 py-3 text-sm text-[#475569] dark:text-gray-400">{{ getGenderName(student.gender) }}</td>
                  <td class="px-4 py-3 text-sm text-[#475569] dark:text-gray-400">{{ student.className }}</td>
                  <td class="px-4 py-3 text-sm text-[#475569] dark:text-gray-400">{{ student.phone || "-" }}</td>
                  <td class="px-4 py-3 text-sm text-[#475569] dark:text-gray-400">{{ student.email || "-" }}</td>
                  <td class="px-4 py-3 text-sm text-[#475569] dark:text-gray-400">{{ student.enrollmentYear || "-" }}</td>
                  <td class="px-4 py-3">
                    <div class="flex items-center gap-2">
                      <Button
                        @click="openEditModal(student)"
                        class="h-7 bg-[#f1f5f9] px-2 text-xs text-[#475569] hover:bg-[#e2e8f0] dark:bg-gray-700 dark:text-gray-300 dark:hover:bg-gray-600"
                      >
                        <Pencil class="mr-1 size-3" />
                        {{ t('common.edit') }}
                      </Button>
                      <Button
                        @click="handleDelete(student)"
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

          <div v-if="studentList.length === 0" class="flex flex-col items-center justify-center py-16 text-[#94a3b8] dark:text-gray-500">
            <Users class="mb-2 size-10" />
            <p class="text-sm">{{ t('student.noStudentData') }}</p>
          </div>

          <div v-if="totalPages > 1" class="mt-4 flex items-center justify-center gap-2">
            <Button
              v-for="p in totalPages"
              :key="p"
              @click="handlePageChange(p - 1)"
              :class="[
                'h-8 px-3 text-sm',
                query.page === p - 1
                  ? 'bg-[#155e75] text-white hover:bg-[#0e4a5e] dark:bg-teal-700 dark:hover:bg-teal-600'
                  : 'bg-[#f1f5f9] text-[#475569] hover:bg-[#e2e8f0] dark:bg-gray-700 dark:text-gray-300 dark:hover:bg-gray-600',
              ]"
            >
              {{ p }}
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>

    <!-- Modal -->
    <div
      v-if="showModal"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/50"
      @click.self="closeModal"
    >
      <div class="w-full max-w-md rounded-lg bg-white p-6 shadow-lg">
        <div class="mb-4 flex items-center justify-between">
          <h3 class="text-base font-semibold text-[#0f172a]">
            {{ modalMode === "create" ? t('student.addStudent') : t('student.editStudent') }}
          </h3>
          <Button @click="closeModal" class="h-7 w-7 bg-transparent p-0 text-[#64748b] hover:bg-[#f1f5f9]">
            <X class="size-4" />
          </Button>
        </div>

        <form @submit.prevent="handleSubmit" class="space-y-4">
          <div class="flex flex-col gap-1.5">
            <Label class="text-xs">{{ t('student.studentId') }} *</Label>
            <Input v-model="form.studentNo" :placeholder="t('student.inputStudentNo')" required />
          </div>

          <div class="flex flex-col gap-1.5">
            <Label class="text-xs">{{ t('student.name') }} *</Label>
            <Input v-model="form.name" :placeholder="t('student.inputName')" required />
          </div>

          <div class="flex flex-col gap-1.5">
            <Label class="text-xs">{{ t('student.gender') }}</Label>
            <select
              v-model="form.gender"
              class="h-9 rounded-md border border-[#e2e8f0] bg-white px-3 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75]"
            >
              <option :value="1">{{ t('student.male') }}</option>
              <option :value="2">{{ t('student.female') }}</option>
            </select>
          </div>

          <div class="flex flex-col gap-1.5">
            <Label class="text-xs">{{ t('student.class') }} *</Label>
            <select
              v-model="form.classId"
              class="h-9 rounded-md border border-[#e2e8f0] bg-white px-3 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75]"
              required
            >
              <option v-for="c in classList" :key="c.id" :value="c.id">
                {{ c.className }}
              </option>
            </select>
          </div>

          <div class="flex flex-col gap-1.5">
            <Label class="text-xs">{{ t('common.phone') }}</Label>
            <Input v-model="form.phone" :placeholder="t('student.inputPhone')" />
          </div>

          <div class="flex flex-col gap-1.5">
            <Label class="text-xs">{{ t('common.email') }}</Label>
            <Input v-model="form.email" :placeholder="t('student.inputEmail')" />
          </div>

          <div class="flex flex-col gap-1.5">
            <Label class="text-xs">{{ t('student.enrollmentYear') }}</Label>
            <Input v-model="form.enrollmentYear" type="number" :placeholder="t('student.inputEnrollmentYear')" />
          </div>

          <div class="flex justify-end gap-2 pt-4">
            <Button @click="closeModal" class="bg-[#f1f5f9] text-[#475569] hover:bg-[#e2e8f0]">
              {{ t('common.cancel') }}
            </Button>
            <Button type="submit" class="bg-[#155e75] hover:bg-[#0e4a5e]">
              {{ modalMode === "create" ? t('common.add') : t('common.save') }}
            </Button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>