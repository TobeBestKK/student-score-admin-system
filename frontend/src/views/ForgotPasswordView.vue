<script setup lang="ts">
import {
  ArrowLeft,
  ArrowRight,
  Building2,
  CheckCircle,
  CircleCheckBig,
  Eye,
  EyeOff,
  KeyRound,
  Landmark,
  Mail,
  ShieldCheck,
  UserRound,
} from "@lucide/vue"
import { computed, reactive, ref } from "vue"
import { RouterLink } from "vue-router"

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

type Role = "student" | "teacher"
type Step = 1 | 2 | 3 | 4

const currentStep = ref<Step>(1)
const activeRole = ref<Role>("student")
const countdown = ref(0)
const showNewPassword = ref(false)
const showConfirmPassword = ref(false)
let countdownTimer: ReturnType<typeof setInterval> | null = null

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

const form = reactive({
  accountId: "",
  contact: "",
  verificationCode: "",
  newPassword: "",
  confirmPassword: "",
})

const errors = reactive({
  accountId: "",
  contact: "",
  verificationCode: "",
  newPassword: "",
  confirmPassword: "",
})

const roleConfig = computed(() =>
  activeRole.value === "student"
    ? {
        accountLabel: "学号",
        accountPlaceholder: "请输入学号",
        contactLabel: "邮箱或手机号",
        contactPlaceholder: "请输入绑定的邮箱或手机号",
      }
    : {
        accountLabel: "工号",
        accountPlaceholder: "请输入工号",
        contactLabel: "邮箱或手机号",
        contactPlaceholder: "请输入绑定的邮箱或手机号",
      },
)

const steps = [
  { number: 1, label: "身份验证" },
  { number: 2, label: "验证码" },
  { number: 3, label: "新密码" },
]

function clearErrors() {
  errors.accountId = ""
  errors.contact = ""
  errors.verificationCode = ""
  errors.newPassword = ""
  errors.confirmPassword = ""
}

function validateStep1(): boolean {
  clearErrors()
  let valid = true

  if (!form.accountId.trim()) {
    errors.accountId = activeRole.value === "student" ? "请输入学号" : "请输入工号"
    valid = false
  }

  if (!form.contact.trim()) {
    errors.contact = "请输入邮箱或手机号"
    valid = false
  }

  return valid
}

function validateStep2(): boolean {
  clearErrors()

  if (!form.verificationCode.trim()) {
    errors.verificationCode = "请输入验证码"
    return false
  }

  if (form.verificationCode.length !== 6) {
    errors.verificationCode = "验证码为6位数字"
    return false
  }

  return true
}

function validateStep3(): boolean {
  clearErrors()
  let valid = true

  if (!form.newPassword.trim()) {
    errors.newPassword = "请输入新密码"
    valid = false
  } else if (form.newPassword.length < 6) {
    errors.newPassword = "密码长度不能少于6位"
    valid = false
  }

  if (!form.confirmPassword.trim()) {
    errors.confirmPassword = "请确认新密码"
    valid = false
  } else if (form.newPassword !== form.confirmPassword) {
    errors.confirmPassword = "两次输入的密码不一致"
    valid = false
  }

  return valid
}

function handleSendCode() {
  if (!validateStep1()) return
  currentStep.value = 2
  startCountdown()
}

function handleVerifyCode() {
  if (!validateStep2()) return
  currentStep.value = 3
}

function handleResetPassword() {
  if (!validateStep3()) return
  currentStep.value = 4
}

function startCountdown() {
  countdown.value = 60
  if (countdownTimer) clearInterval(countdownTimer)
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      if (countdownTimer) clearInterval(countdownTimer)
    }
  }, 1000)
}

function handleResendCode() {
  if (countdown.value > 0) return
  startCountdown()
}

function handleBack() {
  clearErrors()
  if (currentStep.value > 1) {
    currentStep.value--
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
        账号服务
      </div>
    </header>

    <section class="relative z-10 mx-auto flex min-h-[calc(100vh-144px)] w-full max-w-6xl items-center justify-center px-5 pb-16 pt-6 sm:px-8">
      <Card class="w-full max-w-[430px] rounded-lg border-slate-200 bg-white shadow-[0_18px_55px_rgba(15,23,42,0.12)]">
        <CardHeader class="place-items-center space-y-3 border-b border-slate-100 px-7 pb-5 pt-7 text-center">
          <div class="grid size-11 place-items-center rounded-lg bg-teal-50 text-[#0f766e]">
            <ShieldCheck class="size-5" />
          </div>
          <div>
            <CardTitle class="font-display text-2xl font-semibold text-slate-950">
              忘记密码
            </CardTitle>
            <CardDescription class="mt-2 text-sm leading-6 text-slate-500">
              请按步骤重置您的账号密码
            </CardDescription>
          </div>
        </CardHeader>

        <CardContent class="space-y-6 px-7 pb-7 pt-6">
          <!-- 步骤指示器 -->
          <div v-if="currentStep < 4" class="flex items-center justify-center gap-2">
            <template v-for="(step, index) in steps" :key="step.number">
              <div class="flex items-center gap-2">
                <div
                  class="flex size-7 items-center justify-center rounded-full text-xs font-medium transition-colors"
                  :class="[
                    currentStep >= step.number
                      ? 'bg-[#155e75] text-white'
                      : 'bg-slate-100 text-slate-400',
                  ]"
                >
                  {{ step.number }}
                </div>
                <span
                  class="text-xs font-medium"
                  :class="[
                    currentStep >= step.number ? 'text-slate-700' : 'text-slate-400',
                  ]"
                >
                  {{ step.label }}
                </span>
              </div>
              <div
                v-if="index < steps.length - 1"
                class="mx-1 h-px w-6"
                :class="[
                  currentStep > step.number ? 'bg-[#155e75]' : 'bg-slate-200',
                ]"
              />
            </template>
          </div>

          <!-- 步骤1：身份验证 -->
          <div v-if="currentStep === 1" class="space-y-5">
            <Tabs v-model="activeRole" class="gap-5">
              <TabsList class="grid h-10 w-full grid-cols-2 rounded-lg bg-slate-100 p-1">
                <TabsTrigger value="student" class="rounded-md">
                  <UserRound class="size-4" />
                  学生
                </TabsTrigger>
                <TabsTrigger value="teacher" class="rounded-md">
                  <Building2 class="size-4" />
                  教师
                </TabsTrigger>
              </TabsList>

              <TabsContent value="student" class="mt-0">
                <form class="space-y-4" @submit.prevent="handleSendCode">
                  <div class="space-y-2">
                    <Label for="student-id">{{ roleConfig.accountLabel }}</Label>
                    <Input
                      id="student-id"
                      v-model="form.accountId"
                      :aria-invalid="errors.accountId ? 'true' : 'false'"
                      class="h-10 rounded-lg bg-white"
                      :placeholder="roleConfig.accountPlaceholder"
                    />
                    <p v-if="errors.accountId" class="text-sm text-red-600">
                      {{ errors.accountId }}
                    </p>
                  </div>

                  <div class="space-y-2">
                    <Label for="student-contact">{{ roleConfig.contactLabel }}</Label>
                    <div class="relative">
                      <Mail class="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-slate-400" />
                      <Input
                        id="student-contact"
                        v-model="form.contact"
                        :aria-invalid="errors.contact ? 'true' : 'false'"
                        class="h-10 rounded-lg bg-white pl-9"
                        :placeholder="roleConfig.contactPlaceholder"
                      />
                    </div>
                    <p v-if="errors.contact" class="text-sm text-red-600">
                      {{ errors.contact }}
                    </p>
                  </div>

                  <Button type="submit" class="h-10 w-full rounded-lg bg-[#155e75] text-white hover:bg-[#164e63]">
                    发送验证码
                    <ArrowRight class="size-4" />
                  </Button>
                </form>
              </TabsContent>

              <TabsContent value="teacher" class="mt-0">
                <form class="space-y-4" @submit.prevent="handleSendCode">
                  <div class="space-y-2">
                    <Label for="teacher-id">{{ roleConfig.accountLabel }}</Label>
                    <Input
                      id="teacher-id"
                      v-model="form.accountId"
                      :aria-invalid="errors.accountId ? 'true' : 'false'"
                      class="h-10 rounded-lg bg-white"
                      :placeholder="roleConfig.accountPlaceholder"
                    />
                    <p v-if="errors.accountId" class="text-sm text-red-600">
                      {{ errors.accountId }}
                    </p>
                  </div>

                  <div class="space-y-2">
                    <Label for="teacher-contact">{{ roleConfig.contactLabel }}</Label>
                    <div class="relative">
                      <Mail class="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-slate-400" />
                      <Input
                        id="teacher-contact"
                        v-model="form.contact"
                        :aria-invalid="errors.contact ? 'true' : 'false'"
                        class="h-10 rounded-lg bg-white pl-9"
                        :placeholder="roleConfig.contactPlaceholder"
                      />
                    </div>
                    <p v-if="errors.contact" class="text-sm text-red-600">
                      {{ errors.contact }}
                    </p>
                  </div>

                  <Button type="submit" class="h-10 w-full rounded-lg bg-[#155e75] text-white hover:bg-[#164e63]">
                    发送验证码
                    <ArrowRight class="size-4" />
                  </Button>
                </form>
              </TabsContent>
            </Tabs>
          </div>

          <!-- 步骤2：验证码 -->
          <div v-if="currentStep === 2" class="space-y-5">
            <Alert class="rounded-lg border-blue-200 bg-blue-50 text-blue-900">
              <Mail class="mt-0.5 size-4" />
              <AlertDescription>
                验证码已发送至您{{ activeRole === 'student' ? '学号' : '工号' }}绑定的邮箱或手机，请注意查收。
              </AlertDescription>
            </Alert>

            <form class="space-y-4" @submit.prevent="handleVerifyCode">
              <div class="space-y-2">
                <Label for="verification-code">验证码</Label>
                <Input
                  id="verification-code"
                  v-model="form.verificationCode"
                  :aria-invalid="errors.verificationCode ? 'true' : 'false'"
                  class="h-10 rounded-lg bg-white text-center tracking-[0.5em]"
                  placeholder="请输入6位验证码"
                  maxlength="6"
                />
                <p v-if="errors.verificationCode" class="text-sm text-red-600">
                  {{ errors.verificationCode }}
                </p>
              </div>

              <div class="flex items-center justify-between text-sm">
                <span class="text-slate-500">
                  {{ countdown > 0 ? `${countdown}秒后可重新发送` : '未收到验证码？' }}
                </span>
                <button
                  type="button"
                  class="font-medium text-[#0f766e] hover:text-[#155e75] disabled:cursor-not-allowed disabled:text-slate-400"
                  :disabled="countdown > 0"
                  @click="handleResendCode"
                >
                  重新发送
                </button>
              </div>

              <div class="flex gap-3">
                <Button
                  type="button"
                  variant="outline"
                  class="h-10 flex-1 rounded-lg border-slate-200"
                  @click="handleBack"
                >
                  <ArrowLeft class="size-4" />
                  返回
                </Button>
                <Button type="submit" class="h-10 flex-1 rounded-lg bg-[#155e75] text-white hover:bg-[#164e63]">
                  验证
                  <ArrowRight class="size-4" />
                </Button>
              </div>
            </form>
          </div>

          <!-- 步骤3：新密码设置 -->
          <div v-if="currentStep === 3" class="space-y-5">
            <form class="space-y-4" @submit.prevent="handleResetPassword">
              <div class="space-y-2">
                <Label for="new-password">新密码</Label>
                <div class="relative">
                  <KeyRound class="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-slate-400" />
                  <Input
                    id="new-password"
                    v-model="form.newPassword"
                    :aria-invalid="errors.newPassword ? 'true' : 'false'"
                    class="h-10 rounded-lg bg-white pl-9 pr-10"
                    placeholder="请输入新密码（至少6位）"
                    :type="showNewPassword ? 'text' : 'password'"
                  />
                  <button
                    type="button"
                    class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 focus:outline-none"
                    aria-label="切换密码可见性"
                    @click="showNewPassword = !showNewPassword"
                  >
                    <EyeOff v-if="showNewPassword" class="size-4" />
                    <Eye v-else class="size-4" />
                  </button>
                </div>
                <p v-if="errors.newPassword" class="text-sm text-red-600">
                  {{ errors.newPassword }}
                </p>
              </div>

              <div class="space-y-2">
                <Label for="confirm-password">确认密码</Label>
                <div class="relative">
                  <KeyRound class="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-slate-400" />
                  <Input
                    id="confirm-password"
                    v-model="form.confirmPassword"
                    :aria-invalid="errors.confirmPassword ? 'true' : 'false'"
                    class="h-10 rounded-lg bg-white pl-9 pr-10"
                    placeholder="请再次输入新密码"
                    :type="showConfirmPassword ? 'text' : 'password'"
                  />
                  <button
                    type="button"
                    class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 focus:outline-none"
                    aria-label="切换密码可见性"
                    @click="showConfirmPassword = !showConfirmPassword"
                  >
                    <EyeOff v-if="showConfirmPassword" class="size-4" />
                    <Eye v-else class="size-4" />
                  </button>
                </div>
                <p v-if="errors.confirmPassword" class="text-sm text-red-600">
                  {{ errors.confirmPassword }}
                </p>
              </div>

              <div class="flex gap-3">
                <Button
                  type="button"
                  variant="outline"
                  class="h-10 flex-1 rounded-lg border-slate-200"
                  @click="handleBack"
                >
                  <ArrowLeft class="size-4" />
                  返回
                </Button>
                <Button type="submit" class="h-10 flex-1 rounded-lg bg-[#155e75] text-white hover:bg-[#164e63]">
                  重置密码
                  <ArrowRight class="size-4" />
                </Button>
              </div>
            </form>
          </div>

          <!-- 步骤4：完成 -->
          <div v-if="currentStep === 4" class="space-y-5">
            <Alert class="rounded-lg border-emerald-200 bg-emerald-50 text-emerald-900">
              <CircleCheckBig class="mt-0.5 size-4" />
              <AlertTitle>密码重置成功</AlertTitle>
              <AlertDescription>
                您的密码已成功重置，请使用新密码登录系统。
              </AlertDescription>
            </Alert>

            <div class="flex flex-col items-center gap-4 pt-2">
              <CheckCircle class="size-16 text-emerald-500" />
              <p class="text-center text-sm text-slate-600">
                密码已更新，请返回登录页使用新密码登录。
              </p>
            </div>

            <Button as-child class="h-10 w-full rounded-lg bg-[#155e75] text-white hover:bg-[#164e63]">
              <RouterLink to="/login">
                <ArrowLeft class="size-4" />
                返回登录页
              </RouterLink>
            </Button>
          </div>

          <!-- 底部链接 -->
          <div v-if="currentStep === 1" class="flex items-center justify-center text-sm">
            <RouterLink class="font-medium text-[#0f766e] hover:text-[#155e75]" to="/login">
              <ArrowLeft class="mr-1 inline-block size-4" />
              返回登录页
            </RouterLink>
          </div>
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