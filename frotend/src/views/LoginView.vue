<script setup lang="ts">
import {
  ArrowRight,
  Building2,
  CircleCheckBig,
  Eye,
  EyeOff,
  GraduationCap,
  KeyRound,
  Landmark,
  UserRound,
} from "@lucide/vue"
import { computed, reactive, ref, watch } from "vue"
import { RouterLink, useRouter } from "vue-router"

import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { Button } from "@/components/ui/button"
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"

import { login as loginApi } from "@/api/auth"

type Role = "student" | "teacher"

type StudentLoginForm = {
  studentId: string
  password: string
}

type TeacherLoginForm = {
  teacherId: string
  password: string
}

const router = useRouter()
const activeRole = ref<Role>("student")
const loginSuccess = ref(false)
const loginError = ref("")
const isLoading = ref(false)
const showStudentPassword = ref(false)
const showTeacherPassword = ref(false)

const poeticSentences = [
  "书山有路勤为径，学海无涯苦作舟。",
  "博学之，审问之，慎思之，明辨之，笃行之。",
  "业精于勤，荒于嬉；行成于思，毁于随。",
  "纸上得来终觉浅，绝知此事要躬行。",
  "黑发不知勤学早，白首方悔读书迟。",
  "问渠那得清如许？为有源头活水来。",
  "路漫漫其修远兮，吾将上下而求索。",
  "学而不思则罔，思而不学则殆。",
  "千里之行，始于足下。",
  "不积跬步，无以至千里；不积小流，无以成江海。",
]

const currentSentence = ref(poeticSentences[Math.floor(Math.random() * poeticSentences.length)])

const studentForm = reactive<StudentLoginForm>({
  studentId: "",
  password: "",
})

const teacherForm = reactive<TeacherLoginForm>({
  teacherId: "",
  password: "",
})

const studentErrors = reactive({
  studentId: "",
  password: "",
})

const teacherErrors = reactive({
  teacherId: "",
  password: "",
})

const roleConfig = computed(() =>
  activeRole.value === "student"
    ? {
        title: "学生登录",
        accountLabel: "学号",
        accountPlaceholder: "请输入学号",
        passwordHint: "使用教务系统或统一身份认证密码",
        buttonText: "登录学生端",
      }
    : {
        title: "教师登录",
        accountLabel: "工号",
        accountPlaceholder: "请输入工号",
        passwordHint: "使用教师工号绑定的教务账号密码",
        buttonText: "登录教师端",
      },
)

watch(activeRole, () => {
  loginSuccess.value = false
  loginError.value = ""
  clearErrors()
})

function clearErrors() {
  studentErrors.studentId = ""
  studentErrors.password = ""
  teacherErrors.teacherId = ""
  teacherErrors.password = ""
}

async function handleSubmit() {
  loginSuccess.value = false
  loginError.value = ""
  clearErrors()

  let username = ""
  let password = ""

  if (activeRole.value === "student") {
    if (!studentForm.studentId.trim()) {
      studentErrors.studentId = "请输入学号"
    }

    if (!studentForm.password.trim()) {
      studentErrors.password = "请输入密码"
    }

    if (studentErrors.studentId || studentErrors.password) {
      return
    }

    username = studentForm.studentId
    password = studentForm.password
  } else {
    if (!teacherForm.teacherId.trim()) {
      teacherErrors.teacherId = "请输入工号"
    }

    if (!teacherForm.password.trim()) {
      teacherErrors.password = "请输入密码"
    }

    if (teacherErrors.teacherId || teacherErrors.password) {
      return
    }

    username = teacherForm.teacherId
    password = teacherForm.password
  }

  isLoading.value = true

  try {
    const response = await loginApi({
      username,
      password,
      role: activeRole.value,
    })

    if (response.success) {
      localStorage.setItem("token", response.token)
      localStorage.setItem("userInfo", JSON.stringify(response.userInfo))
      loginSuccess.value = true

      setTimeout(() => {
        if (activeRole.value === "student") {
          router.push("/student-dashboard")
        } else {
          router.push("/dashboard")
        }
      }, 1500)
    } else {
      loginError.value = response.message
    }
  } catch (error) {
    loginError.value = "登录失败，请稍后重试"
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <main class="portal-login-page relative min-h-screen overflow-hidden bg-[#f5f8fb] text-slate-900">
    <div class="pointer-events-none absolute inset-0">
      <div class="absolute inset-0 bg-[linear-gradient(rgba(20,83,111,0.055)_1px,transparent_1px),linear-gradient(90deg,rgba(20,83,111,0.055)_1px,transparent_1px)] bg-[size:44px_44px]" />
      <div class="absolute inset-x-0 bottom-0 h-64 bg-[linear-gradient(180deg,rgba(245,248,251,0),rgba(219,234,240,0.76))]" />
      <svg class="absolute left-1/2 top-24 h-56 w-[760px] -translate-x-1/2 text-teal-800/10" viewBox="0 0 760 220" fill="none" aria-hidden="true">
        <path d="M28 162C96 110 146 126 206 86C268 45 314 104 380 72C451 38 496 84 548 62C612 35 668 56 732 30" stroke="currentColor" stroke-width="4" stroke-linecap="round" />
        <path d="M44 184C132 142 196 168 266 124C333 82 380 132 450 98C520 66 574 112 704 76" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-dasharray="8 14" />
      </svg>
      <div class="campus-silhouette absolute inset-x-0 bottom-0 h-32 opacity-60" />
    </div>

    <header class="relative z-10 mx-auto flex w-full max-w-6xl items-center justify-between px-5 py-6 sm:px-8">
      <div class="flex items-center gap-3">
        <div class="grid size-10 place-items-center rounded-lg bg-[#155e75] text-white shadow-sm">
          <Landmark class="size-5" />
        </div>
        <div>
          <p class="text-base font-semibold text-slate-950">学生成绩分析教务系统</p>
          <p class="text-xs text-slate-500">Academic Affairs Portal</p>
        </div>
      </div>
      <div class="hidden items-center gap-2 text-sm text-slate-500 sm:flex">
        <Building2 class="size-4 text-[#0f766e]" />
        教务统一入口
      </div>
    </header>

    <section class="relative z-10 mx-auto flex min-h-[calc(100vh-144px)] w-full max-w-6xl items-center justify-center px-5 pb-16 pt-6 sm:px-8">
      <Card class="w-full max-w-[430px] rounded-lg border-slate-200 bg-white shadow-[0_18px_55px_rgba(15,23,42,0.12)]">
        <CardHeader class="place-items-center space-y-3 border-b border-slate-100 px-7 pb-5 pt-7 text-center">
          <div class="grid size-11 place-items-center rounded-lg bg-teal-50 text-[#0f766e]">
            <GraduationCap class="size-5" />
          </div>
          <div>
            <CardTitle class="font-display text-2xl font-semibold text-slate-950">
              教务统一登录
            </CardTitle>
            <CardDescription class="mt-2 text-sm leading-6 text-slate-500">
              请选择身份后登录系统
            </CardDescription>
          </div>
        </CardHeader>

        <CardContent class="space-y-5 px-7 pb-7 pt-6">
          <Alert
            v-if="loginSuccess"
            class="rounded-lg border-emerald-200 bg-emerald-50 text-emerald-900"
          >
            <CircleCheckBig class="mt-0.5 size-4" />
            <AlertTitle>登录成功</AlertTitle>
            <AlertDescription>
              正在跳转至系统首页...
            </AlertDescription>
          </Alert>

          <Alert
            v-if="loginError"
            class="rounded-lg border-red-200 bg-red-50 text-red-900"
          >
            <AlertTitle>登录失败</AlertTitle>
            <AlertDescription>
              {{ loginError }}
            </AlertDescription>
          </Alert>

          <Tabs v-model="activeRole" class="gap-5">
            <TabsList class="grid h-10 w-full grid-cols-2 rounded-lg bg-slate-100 p-1">
              <TabsTrigger value="student" class="rounded-md">
                <GraduationCap class="size-4" />
                学生登录
              </TabsTrigger>
              <TabsTrigger value="teacher" class="rounded-md">
                <UserRound class="size-4" />
                教师登录
              </TabsTrigger>
            </TabsList>

            <TabsContent value="student" class="mt-0">
              <form class="space-y-4" @submit.prevent="handleSubmit">
                <div class="space-y-2">
                  <Label for="student-id">学号</Label>
                  <Input
                    id="student-id"
                    v-model="studentForm.studentId"
                    :aria-invalid="studentErrors.studentId ? 'true' : 'false'"
                    class="h-10 rounded-lg bg-white"
                    placeholder="请输入学号"
                  />
                  <p v-if="studentErrors.studentId" class="text-sm text-red-600">
                    {{ studentErrors.studentId }}
                  </p>
                </div>

                <div class="space-y-2">
                  <div class="flex items-center justify-between gap-3">
                    <Label for="student-password">密码</Label>
                    <span class="truncate text-xs text-slate-500">
                      {{ roleConfig.passwordHint }}
                    </span>
                  </div>
                  <div class="relative">
                    <KeyRound class="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-slate-400" />
                    <Input
                      id="student-password"
                      v-model="studentForm.password"
                      :aria-invalid="studentErrors.password ? 'true' : 'false'"
                      class="h-10 rounded-lg bg-white pl-9 pr-10"
                      placeholder="请输入密码"
                      :type="showStudentPassword ? 'text' : 'password'"
                    />
                    <button
                      type="button"
                      class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 focus:outline-none"
                      aria-label="切换密码可见性"
                      @click="showStudentPassword = !showStudentPassword"
                    >
                      <EyeOff v-if="showStudentPassword" class="size-4" />
                      <Eye v-else class="size-4" />
                    </button>
                  </div>
                  <p v-if="studentErrors.password" class="text-sm text-red-600">
                    {{ studentErrors.password }}
                  </p>
                </div>

                <div class="flex items-center justify-end gap-4 text-sm">
                  <RouterLink class="shrink-0 font-medium text-[#0f766e] hover:text-[#155e75]" to="/forgot-password">
                    忘记密码
                  </RouterLink>
                </div>

                <Button :disabled="isLoading" class="h-10 w-full rounded-lg bg-[#155e75] text-white hover:bg-[#164e63]">
                  {{ roleConfig.buttonText }}
                  <ArrowRight class="size-4" />
                </Button>
              </form>
            </TabsContent>

            <TabsContent value="teacher" class="mt-0">
              <form class="space-y-4" @submit.prevent="handleSubmit">
                <div class="space-y-2">
                  <Label for="teacher-id">工号</Label>
                  <Input
                    id="teacher-id"
                    v-model="teacherForm.teacherId"
                    :aria-invalid="teacherErrors.teacherId ? 'true' : 'false'"
                    class="h-10 rounded-lg bg-white"
                    placeholder="请输入工号"
                  />
                  <p v-if="teacherErrors.teacherId" class="text-sm text-red-600">
                    {{ teacherErrors.teacherId }}
                  </p>
                </div>

                <div class="space-y-2">
                  <div class="flex items-center justify-between gap-3">
                    <Label for="teacher-password">密码</Label>
                    <span class="truncate text-xs text-slate-500">
                      {{ roleConfig.passwordHint }}
                    </span>
                  </div>
                  <div class="relative">
                    <KeyRound class="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-slate-400" />
                    <Input
                      id="teacher-password"
                      v-model="teacherForm.password"
                      :aria-invalid="teacherErrors.password ? 'true' : 'false'"
                      class="h-10 rounded-lg bg-white pl-9 pr-10"
                      placeholder="请输入密码"
                      :type="showTeacherPassword ? 'text' : 'password'"
                    />
                    <button
                      type="button"
                      class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 focus:outline-none"
                      aria-label="切换密码可见性"
                      @click="showTeacherPassword = !showTeacherPassword"
                    >
                      <EyeOff v-if="showTeacherPassword" class="size-4" />
                      <Eye v-else class="size-4" />
                    </button>
                  </div>
                  <p v-if="teacherErrors.password" class="text-sm text-red-600">
                    {{ teacherErrors.password }}
                  </p>
                </div>

                <div class="flex items-center justify-end gap-4 text-sm">
                  <RouterLink class="shrink-0 font-medium text-[#0f766e] hover:text-[#155e75]" to="/forgot-password">
                    忘记密码
                  </RouterLink>
                </div>

                <Button :disabled="isLoading" class="h-10 w-full rounded-lg bg-[#155e75] text-white hover:bg-[#164e63]">
                  {{ roleConfig.buttonText }}
                  <ArrowRight class="size-4" />
                </Button>
              </form>
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>
    </section>

    <p class="relative z-10 pb-4 text-center text-sm text-slate-400 italic">
      {{ currentSentence }}
    </p>

    <footer class="relative z-10 pb-6 text-center text-xs text-slate-500">
      教务支持中心 · 数据仅用于教学管理与学业分析
    </footer>
  </main>
</template>