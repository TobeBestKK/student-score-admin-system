<script setup lang="ts">
import { ref, onMounted, computed } from "vue"
import { useI18n } from "vue-i18n"
import { useRouter } from "vue-router"
import { Search, Users, AlertTriangle } from "@lucide/vue"
import { fetchClasses, type ClassItem, type ClassQuery } from "@/api/class"
import Card from "@/components/ui/card/Card.vue"
import CardContent from "@/components/ui/card/CardContent.vue"
import CardHeader from "@/components/ui/card/CardHeader.vue"
import CardTitle from "@/components/ui/card/CardTitle.vue"
import Button from "@/components/ui/button/Button.vue"
import Input from "@/components/ui/input/Input.vue"
import Label from "@/components/ui/label/Label.vue"

const { t } = useI18n()

const router = useRouter()
const loading = ref(false)
const classList = ref<ClassItem[]>([])

const gradeOptions = computed(() => [
  { label: t('class.grade1'), value: 1 },
  { label: t('class.grade2'), value: 2 },
  { label: t('class.grade3'), value: 3 },
])
const majorOptions = computed(() => [t('class.science'), t('class.arts')])

const query = ref<ClassQuery>({
  grade: undefined,
  major: undefined,
  keyword: undefined,
})

async function loadClasses() {
  loading.value = true
  try {
    classList.value = await fetchClasses(query.value)
  } catch (e) {
    console.error("Failed to load classes", e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  loadClasses()
}

function goToDetail(id: number) {
  router.push(`/teacher/classes/${id}`)
}

function getGradeName(grade: number) {
  const map: Record<number, string> = { 1: t('class.grade1'), 2: t('class.grade2'), 3: t('class.grade3') }
  return map[grade] ?? String(grade)
}

onMounted(() => {
  loadClasses()
})
</script>

<template>
  <div class="space-y-6">
    <Card>
      <CardHeader>
        <CardTitle class="flex items-center gap-2 text-base">
          <Users class="size-5 text-[#155e75]" />
          {{ t('class.management') }}
        </CardTitle>
      </CardHeader>
      <CardContent>
        <div class="mb-6 flex flex-wrap items-end gap-4">
          <div class="flex flex-col gap-1.5">
            <Label class="text-xs">{{ t('class.grade') }}</Label>
            <select
              v-model="query.grade"
              class="h-9 rounded-md border border-[#e2e8f0] bg-white px-3 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75]"
            >
              <option :value="undefined">{{ t('student.allGrades') }}</option>
              <option v-for="g in gradeOptions" :key="g.value" :value="g.value">
                {{ g.label }}
              </option>
            </select>
          </div>

          <div class="flex flex-col gap-1.5">
            <Label class="text-xs">{{ t('student.subjectType') }}</Label>
            <select
              v-model="query.major"
              class="h-9 rounded-md border border-[#e2e8f0] bg-white px-3 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75]"
            >
              <option :value="undefined">{{ t('student.allSubjects') }}</option>
              <option v-for="m in majorOptions" :key="m" :value="m">
                {{ m }}
              </option>
            </select>
          </div>

          <div class="flex flex-col gap-1.5">
            <Label class="text-xs">{{ t('class.name') }}</Label>
            <Input v-model="query.keyword" :placeholder="t('class.inputClassName')" class="w-48" />
          </div>

          <Button @click="handleSearch" class="bg-[#155e75] hover:bg-[#0e4a5e]">
            <Search class="mr-1.5 size-4" />
            {{ t('common.search') }}
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
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b]">{{ t('class.code') }}</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b]">{{ t('class.name') }}</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b]">{{ t('student.subjectType') }}</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b]">{{ t('class.grade') }}</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b]">{{ t('class.headTeacher') }}</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b]">{{ t('class.studentCount') }}</th>
                  <th class="px-4 py-3 text-left text-xs font-medium text-[#64748b]">{{ t('class.warningCount') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="cls in classList"
                  :key="cls.id"
                  class="cursor-pointer border-b border-[#f1f5f9] transition-colors hover:bg-[#f8fafc]"
                  @click="goToDetail(cls.id)"
                >
                  <td class="px-4 py-3 text-sm text-[#475569]">{{ cls.classCode }}</td>
                  <td class="px-4 py-3 text-sm font-medium text-[#0f172a]">{{ cls.className }}</td>
                  <td class="px-4 py-3 text-sm text-[#475569]">{{ cls.major }}</td>
                  <td class="px-4 py-3 text-sm text-[#475569]">{{ getGradeName(cls.grade) }}</td>
                  <td class="px-4 py-3 text-sm text-[#475569]">{{ cls.headTeacherName }}</td>
                  <td class="px-4 py-3 text-sm text-[#475569]">{{ cls.studentCount }}</td>
                  <td class="px-4 py-3">
                    <span
                      v-if="cls.warningCount > 0"
                      class="inline-flex items-center gap-1 rounded-full bg-[#fef2f2] px-2 py-0.5 text-xs font-medium text-[#dc2626]"
                    >
                      <AlertTriangle class="size-3" />
                      {{ cls.warningCount }}
                    </span>
                    <span v-else class="text-sm text-[#94a3b8]">0</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div v-if="classList.length === 0" class="flex flex-col items-center justify-center py-16 text-[#94a3b8]">
            <Users class="mb-2 size-10" />
            <p class="text-sm">{{ t('class.noClassData') }}</p>
          </div>
        </div>
      </CardContent>
    </Card>
  </div>
</template>
