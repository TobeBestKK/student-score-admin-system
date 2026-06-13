<script setup lang="ts">
import { ref, computed, onMounted } from "vue"
import { AlertTriangle, Filter, Users } from "@lucide/vue"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { fetchWarnings, type StudentWarnings } from "../../api/warning"

const warnings = ref<StudentWarnings[]>([])
const loading = ref(false)

const selectedClass = ref("")
const selectedLevel = ref("")
const selectedType = ref("")

const classOptions = [
  { value: "", label: "全部班级" },
  { value: "1", label: "1班" },
  { value: "2", label: "2班" },
  { value: "3", label: "3班" },
]

const levelOptions = [
  { value: "", label: "全部级别" },
  { value: "普通提醒", label: "普通提醒" },
  { value: "重点关注", label: "重点关注" },
  { value: "严重预警", label: "严重预警" },
]

const typeOptions = [
  { value: "", label: "全部类型" },
  { value: "单科低分", label: "单科低分" },
  { value: "学期平均分偏低", label: "学期平均分偏低" },
  { value: "总分排名靠后", label: "总分排名靠后" },
  { value: "累计不及格", label: "累计不及格" },
]

const normalCount = computed(() => 
  warnings.value.filter((w) => w.maxLevel === "普通提醒").length
)

const moderateCount = computed(() => 
  warnings.value.filter((w) => w.maxLevel === "重点关注").length
)

const severeCount = computed(() => 
  warnings.value.filter((w) => w.maxLevel === "严重预警").length
)

function getLevelColor(level: string) {
  if (level === "严重预警") return "bg-[#dc2626] text-white"
  if (level === "重点关注") return "bg-[#ea580c] text-white"
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
    <div class="rounded-lg border border-[#e2e8f0] bg-white p-4">
      <div class="mb-4 flex items-center gap-2">
        <Filter class="size-4 text-[#64748b]" />
        <h3 class="text-sm font-semibold text-[#0f172a]">筛选条件</h3>
      </div>
      <div class="flex flex-wrap items-end gap-4">
        <div class="flex flex-col gap-1.5">
          <label class="text-xs font-medium text-[#64748b]">班级</label>
          <select
            v-model="selectedClass"
            class="h-9 rounded-md border border-[#e2e8f0] bg-white px-3 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75]"
          >
            <option v-for="opt in classOptions" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </option>
          </select>
        </div>
        <div class="flex flex-col gap-1.5">
          <label class="text-xs font-medium text-[#64748b]">预警级别</label>
          <select
            v-model="selectedLevel"
            class="h-9 rounded-md border border-[#e2e8f0] bg-white px-3 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75]"
          >
            <option v-for="opt in levelOptions" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </option>
          </select>
        </div>
        <div class="flex flex-col gap-1.5">
          <label class="text-xs font-medium text-[#64748b]">预警类型</label>
          <select
            v-model="selectedType"
            class="h-9 rounded-md border border-[#e2e8f0] bg-white px-3 text-sm text-[#475569] focus:outline-none focus:ring-2 focus:ring-[#155e75]"
          >
            <option v-for="opt in typeOptions" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </option>
          </select>
        </div>
        <div class="flex items-center gap-2">
          <Button size="sm" @click="handleSearch">查询</Button>
          <Button size="sm" variant="outline" @click="handleReset">重置</Button>
        </div>
      </div>
    </div>

    <div class="grid gap-4 md:grid-cols-3">
      <Card>
        <CardHeader class="pb-2">
          <CardTitle class="text-sm font-medium text-[#64748b]">普通提醒</CardTitle>
        </CardHeader>
        <CardContent>
          <div class="flex items-center justify-between">
            <p class="text-3xl font-bold text-[#16a34a]">{{ normalCount }}</p>
            <div class="grid size-10 place-items-center rounded-md bg-[#f0fdf4]">
              <Users class="size-5 text-[#16a34a]" />
            </div>
          </div>
          <p class="mt-1 text-xs text-[#64748b]">人</p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader class="pb-2">
          <CardTitle class="text-sm font-medium text-[#64748b]">重点关注</CardTitle>
        </CardHeader>
        <CardContent>
          <div class="flex items-center justify-between">
            <p class="text-3xl font-bold text-[#ea580c]">{{ moderateCount }}</p>
            <div class="grid size-10 place-items-center rounded-md bg-[#fff7ed]">
              <AlertTriangle class="size-5 text-[#ea580c]" />
            </div>
          </div>
          <p class="mt-1 text-xs text-[#64748b]">人</p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader class="pb-2">
          <CardTitle class="text-sm font-medium text-[#64748b]">严重预警</CardTitle>
        </CardHeader>
        <CardContent>
          <div class="flex items-center justify-between">
            <p class="text-3xl font-bold text-[#dc2626]">{{ severeCount }}</p>
            <div class="grid size-10 place-items-center rounded-md bg-[#fef2f2]">
              <AlertTriangle class="size-5 text-[#dc2626]" />
            </div>
          </div>
          <p class="mt-1 text-xs text-[#64748b]">人</p>
        </CardContent>
      </Card>
    </div>

    <div class="space-y-4">
      <div v-if="loading" class="py-12 text-center text-sm text-[#64748b]">
        加载中...
      </div>
      <div v-else-if="warnings.length === 0" class="py-12 text-center">
        <AlertTriangle class="mx-auto size-10 text-[#cbd5e1]" />
        <p class="mt-2 text-sm text-[#64748b]">暂无预警数据</p>
      </div>
      <div
        v-for="student in warnings"
        :key="student.studentId"
        class="overflow-hidden rounded-lg border border-[#e2e8f0] bg-white"
      >
        <div class="flex items-center justify-between border-b border-[#f1f5f9] bg-[#f8fafc] px-4 py-3">
          <div class="flex items-center gap-4">
            <div class="flex items-center gap-2">
              <Users class="size-4 text-[#64748b]" />
              <span class="text-sm font-semibold text-[#0f172a]">{{ student.studentName }}</span>
            </div>
            <span class="text-xs text-[#64748b]">{{ student.studentNo }}</span>
            <span class="rounded-full bg-[#f1f5f9] px-2 py-0.5 text-xs text-[#475569]">
              {{ student.className }}
            </span>
          </div>
          <span :class="['rounded-full px-2.5 py-0.5 text-xs font-medium', getLevelColor(student.maxLevel ?? '')]">
            {{ student.maxLevel }}
          </span>
        </div>
        <div class="divide-y divide-[#f1f5f9]">
          <div
            v-for="(item, idx) in student.warnings"
            :key="idx"
            class="flex items-center justify-between px-4 py-3"
          >
            <div class="flex items-center gap-4">
              <span :class="['rounded px-2 py-0.5 text-xs font-medium', getLevelColor(item.level)]">
                {{ item.type }}
              </span>
              <span v-if="item.courseName" class="text-sm text-[#475569]">{{ item.courseName }}</span>
              <span class="text-xs text-[#64748b]">
                当前值: {{ item.currentValue }} / 阈值: {{ item.threshold }}
              </span>
            </div>
            <span class="text-xs text-[#94a3b8]">{{ item.reason }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
