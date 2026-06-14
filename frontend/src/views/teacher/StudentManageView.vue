<script setup lang="ts">
import { ref, onMounted, computed } from "vue"
import { Search, Users, Plus, Pencil, Trash2, X } from "@lucide/vue"
import {
  fetchStudents,
  fetchAllStudents,
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

const loading = ref(false)
const studentList = ref<Student[]>([])
const classList = ref<ClassItem[]>([])
const totalElements = ref(0)
const totalPages = ref(0)

const gradeOptions = [
  { label: "高三", value: 3 },
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
    alert("操作失败，请检查输入信息")
  }
}

async function handleDelete(student: Student) {
  if (!confirm(`确定要删除学生 "${student.name}" 吗？`)) return
  try {
    await deleteStudent(student.id)
    loadStudents()
  } catch (e) {
    console.error("Failed to delete student", e)
    alert("删除失败")
  }
}

function getGradeName(grade: number) {
  const map: Record<number, string> = { 1: "高一", 2: "高二", 3: "高三" }
  return map[grade] ?? String(grade)
}

function getGenderName(gender: number) {
  return gender === 1 ? "男" : gender === 2 ? "女" : "未知"
}

onMounted(() => {
  loadClasses()
  loadStudents()
})
</script>

<template>
  <div class="space-y-6">
    <Card>
      <CardHeader>
        <CardTitle class="flex items-center gap-2 text-base">
          <Users class="size-5 text-[#155e75]" />
          学生管理
        </CardTitle>
      </CardHeader>
      <CardContent>
        <div class="mb-6 flex flex-wrap items-end gap-4">
          <div class="flex flex-col gap-1.5">
            <Label class="text-xs">年级</Label>
            <select
              v-model="query.grade"
              class="h-9 rounded-md border border-[#e2e8f0] bg-white px-3 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75]"
            >
              <option :value="undefined">全部年级</option>
              <option v-for="g in gradeOptions" :key="g.value" :value="g.value">
                {{ g.label }}
              </option>
            </select>
          </div>

          <div class="flex flex-col gap-1.5">
            <Label class="text-xs">班级</Label>
            <select
              v-model="query.classId"
              class="h-9 rounded-md border border-[#e2e8f0] bg-white px-3 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75]"
            >
              <option :value="undefined">全部班级</option>
              <option v-for="c in filteredClassList" :key="c.id" :value="c.id">
                {{ c.className }}
              </option>
            </select>
          </div>

          <div class="flex flex-col gap-1.5">
            <Label class="text-xs">姓名/学号</Label>
            <Input v-model="query.keyword" placeholder="输入姓名或学号" class="w-48" />
          </div>

          <Button @click="handleSearch" class="bg-[#155e75] hover:bg-[#0e4a5e]">
            <Search class="mr-1.5 size-4" />
            搜索
          </Button>

          <Button @click="openCreateModal" class="bg-[#0f766e] hover:bg-[#0d5d57]">
            <Plus class="mr-1.5 size-4" />
            添加学生
          </Button>
        </div>

        <div v-if="loading" class="flex items-center justify-center py-16">
          <div class="size-8 animate-spin rounded-full border-4 border-[#e2e8f0] border-t-[#155e75]"></div>
        </div>

        <div v-else>
          <div class="overflow-x-auto rounded-lg border border-[#e2e8f0]">
            <table class="w-full">
              <thead>
                <tr class="border-b border-[#e2e8f0] bg-[#f8fafc]">
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b]">学号</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b]">姓名</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b]">性别</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b]">班级</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b]">电话</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b]">邮箱</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b]">入学年份</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b]">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="student in studentList"
                  :key="student.id"
                  class="border-b border-[#f1f5f9] transition-colors hover:bg-[#f8fafc]"
                >
                  <td class="px-4 py-3 text-sm text-[#475569]">{{ student.studentNo }}</td>
                  <td class="px-4 py-3 text-sm font-medium text-[#0f172a]">{{ student.name }}</td>
                  <td class="px-4 py-3 text-sm text-[#475569]">{{ getGenderName(student.gender) }}</td>
                  <td class="px-4 py-3 text-sm text-[#475569]">{{ student.className }}</td>
                  <td class="px-4 py-3 text-sm text-[#475569]">{{ student.phone || "-" }}</td>
                  <td class="px-4 py-3 text-sm text-[#475569]">{{ student.email || "-" }}</td>
                  <td class="px-4 py-3 text-sm text-[#475569]">{{ student.enrollmentYear || "-" }}</td>
                  <td class="px-4 py-3">
                    <div class="flex items-center gap-2">
                      <Button
                        @click="openEditModal(student)"
                        class="h-7 bg-[#f1f5f9] px-2 text-xs text-[#475569] hover:bg-[#e2e8f0]"
                      >
                        <Pencil class="mr-1 size-3" />
                        编辑
                      </Button>
                      <Button
                        @click="handleDelete(student)"
                        class="h-7 bg-[#fef2f2] px-2 text-xs text-[#dc2626] hover:bg-[#fee2e2]"
                      >
                        <Trash2 class="mr-1 size-3" />
                        删除
                      </Button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div v-if="studentList.length === 0" class="flex flex-col items-center justify-center py-16 text-[#94a3b8]">
            <Users class="mb-2 size-10" />
            <p class="text-sm">暂无学生数据</p>
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

    <!-- Modal -->
    <div
      v-if="showModal"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/50"
      @click.self="closeModal"
    >
      <div class="w-full max-w-md rounded-lg bg-white p-6 shadow-lg">
        <div class="mb-4 flex items-center justify-between">
          <h3 class="text-base font-semibold text-[#0f172a]">
            {{ modalMode === "create" ? "添加学生" : "编辑学生" }}
          </h3>
          <Button @click="closeModal" class="h-7 w-7 bg-transparent p-0 text-[#64748b] hover:bg-[#f1f5f9]">
            <X class="size-4" />
          </Button>
        </div>

        <form @submit.prevent="handleSubmit" class="space-y-4">
          <div class="flex flex-col gap-1.5">
            <Label class="text-xs">学号 *</Label>
            <Input v-model="form.studentNo" placeholder="请输入学号" required />
          </div>

          <div class="flex flex-col gap-1.5">
            <Label class="text-xs">姓名 *</Label>
            <Input v-model="form.name" placeholder="请输入姓名" required />
          </div>

          <div class="flex flex-col gap-1.5">
            <Label class="text-xs">性别</Label>
            <select
              v-model="form.gender"
              class="h-9 rounded-md border border-[#e2e8f0] bg-white px-3 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75]"
            >
              <option :value="1">男</option>
              <option :value="2">女</option>
            </select>
          </div>

          <div class="flex flex-col gap-1.5">
            <Label class="text-xs">班级 *</Label>
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
            <Label class="text-xs">电话</Label>
            <Input v-model="form.phone" placeholder="请输入电话" />
          </div>

          <div class="flex flex-col gap-1.5">
            <Label class="text-xs">邮箱</Label>
            <Input v-model="form.email" placeholder="请输入邮箱" />
          </div>

          <div class="flex flex-col gap-1.5">
            <Label class="text-xs">入学年份</Label>
            <Input v-model="form.enrollmentYear" type="number" placeholder="请输入入学年份" />
          </div>

          <div class="flex justify-end gap-2 pt-4">
            <Button @click="closeModal" class="bg-[#f1f5f9] text-[#475569] hover:bg-[#e2e8f0]">
              取消
            </Button>
            <Button type="submit" class="bg-[#155e75] hover:bg-[#0e4a5e]">
              {{ modalMode === "create" ? "添加" : "保存" }}
            </Button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>