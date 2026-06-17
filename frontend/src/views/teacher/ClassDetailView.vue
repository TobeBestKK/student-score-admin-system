<script setup lang="ts">
import { ref, onMounted, watch } from "vue"
import { useRoute } from "vue-router"
import { useI18n } from "vue-i18n"
import { Users, Search } from "@lucide/vue"
import { fetchClassInfo, fetchClassStudents, type ClassInfo, type ClassStudent } from "@/api/class"
import Card from "@/components/ui/card/Card.vue"
import CardContent from "@/components/ui/card/CardContent.vue"
import Button from "@/components/ui/button/Button.vue"
import Input from "@/components/ui/input/Input.vue"
import Label from "@/components/ui/label/Label.vue"
import { WARNING_LEVEL } from "@/constants/enum"

const route = useRoute()
const { t } = useI18n()
const classId = ref(Number(route.params.id))

const classInfo = ref<ClassInfo | null>(null)
const students = ref<ClassStudent[]>([])

const studentQuery = ref({ name: "", studentNo: "" })
const studentsLoading = ref(false)

async function loadClassInfo() {
  try {
    classInfo.value = await fetchClassInfo(classId.value)
  } catch (e) {
    console.error("Failed to load class info", e)
  }
}

async function loadStudents() {
  studentsLoading.value = true
  try {
    const params: Record<string, any> = {}
    if (studentQuery.value.name) params.name = studentQuery.value.name
    if (studentQuery.value.studentNo) params.studentNo = studentQuery.value.studentNo
    students.value = await fetchClassStudents(classId.value, params)
  } catch (e) {
    console.error("Failed to load students", e)
  } finally {
    studentsLoading.value = false
  }
}

function getWarningLevelLabel(level: string) {
  if (level === WARNING_LEVEL.SEVERE) return t('warning.severe')
  if (level === WARNING_LEVEL.FOCUS) return t('warning.focus')
  return t('warning.normal')
}

function getWarningLevelClass(level: string) {
  if (level === WARNING_LEVEL.SEVERE) return "bg-[#fef2f2] text-[#dc2626]"
  if (level === WARNING_LEVEL.FOCUS) return "bg-[#fff7ed] text-[#c2410c]"
  return "bg-[#fefce8] text-[#a16207]"
}

onMounted(async () => {
  await Promise.all([loadClassInfo(), loadStudents()])
})

watch(() => route.params.id, (newId) => {
  if (newId) {
    classId.value = Number(newId)
    loadClassInfo()
    loadStudents()
  }
})
</script>

<template>
  <div class="space-y-6">
    <Card v-if="classInfo">
      <CardContent class="py-4">
        <div class="flex flex-wrap items-center gap-6">
          <div class="flex items-center gap-2">
            <Users class="size-5 text-[#155e75]" />
            <h1 class="text-lg font-semibold text-[#0f172a]">{{ classInfo.className }}</h1>
          </div>
          <div class="flex items-center gap-4 text-sm text-[#475569]">
            <span class="rounded-full bg-[#f1f5f9] px-2.5 py-0.5">{{ classInfo.major }}</span>
            <span class="rounded-full bg-[#f1f5f9] px-2.5 py-0.5">
              {{ classInfo.grade === 1 ? t('class.grade1') : classInfo.grade === 2 ? t('class.grade2') : t('class.grade3') }}
            </span>
            <span>{{ t('class.headTeacher') }}: {{ classInfo.headTeacherName }}</span>
            <span>{{ t('class.studentCount') }}: <strong class="text-[#0f172a]">{{ classInfo.studentCount }}</strong></span>
          </div>
        </div>
      </CardContent>
    </Card>

    <Card>
      <CardContent class="pt-6">
        <div class="mb-4 flex flex-wrap items-end gap-3">
          <div class="flex flex-col gap-1.5">
            <Label class="text-xs">{{ t('student.name') }}</Label>
            <Input v-model="studentQuery.name" :placeholder="t('student.inputName')" class="w-40" />
          </div>
          <div class="flex flex-col gap-1.5">
            <Label class="text-xs">{{ t('student.studentId') }}</Label>
            <Input v-model="studentQuery.studentNo" :placeholder="t('student.inputStudentNo')" class="w-40" />
          </div>
          <Button @click="loadStudents" size="sm" class="bg-[#155e75] hover:bg-[#0e4a5e]">
            <Search class="mr-1 size-4" />
            {{ t('common.query') }}
          </Button>
        </div>

        <div v-if="studentsLoading" class="flex items-center justify-center py-12">
          <div class="size-8 animate-spin rounded-full border-4 border-[#e2e8f0] border-t-[#155e75]"></div>
        </div>

        <div v-else>
          <div class="overflow-x-auto rounded-lg border border-[#e2e8f0]">
            <table class="w-full">
              <thead>
                <tr class="border-b border-[#e2e8f0] bg-[#f8fafc]">
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b]">{{ t('student.studentId') }}</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b]">{{ t('student.name') }}</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b]">{{ t('student.gender') }}</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b]">{{ t('warning.level') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="s in students"
                  :key="s.id"
                  class="border-b border-[#f1f5f9] last:border-0 hover:bg-[#f8fafc]"
                >
                  <td class="px-4 py-3 text-sm text-[#475569]">{{ s.studentNo }}</td>
                  <td class="px-4 py-3 text-sm font-medium text-[#0f172a]">{{ s.name }}</td>
                  <td class="px-4 py-3 text-sm text-[#475569]">{{ s.gender === 1 ? t('student.male') : s.gender === 2 ? t('student.female') : t('student.unknown') }}</td>
                  <td class="px-4 py-3">
                    <span
                      v-if="s.warningLevel"
                      :class="['rounded-full px-2.5 py-0.5 text-xs font-medium', getWarningLevelClass(s.warningLevel)]"
                    >
                      {{ getWarningLevelLabel(s.warningLevel) }}
                    </span>
                    <span v-else class="text-sm text-[#94a3b8]">{{ t('common.none') }}</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div v-if="students.length === 0" class="py-12 text-center text-sm text-[#94a3b8]">
            {{ t('student.noStudentData') }}
          </div>
        </div>
      </CardContent>
    </Card>
  </div>
</template>
