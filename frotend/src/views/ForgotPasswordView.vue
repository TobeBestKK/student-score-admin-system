<script setup lang="ts">
import { ArrowLeft, Building2, Landmark, LifeBuoy, MailQuestion } from "@lucide/vue"
import { RouterLink } from "vue-router"

import { Button } from "@/components/ui/button"
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
</script>

<template>
  <main class="portal-login-page relative min-h-screen overflow-hidden bg-[#f5f8fb] text-slate-900">
    <div class="pointer-events-none absolute inset-0">
      <div class="absolute inset-0 bg-[linear-gradient(rgba(20,83,111,0.055)_1px,transparent_1px),linear-gradient(90deg,rgba(20,83,111,0.055)_1px,transparent_1px)] bg-[size:44px_44px]" />
      <div class="campus-silhouette absolute inset-x-0 bottom-0 h-32 opacity-50" />
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
      <Card class="w-full max-w-[500px] rounded-lg border-slate-200 bg-white shadow-[0_18px_55px_rgba(15,23,42,0.12)]">
        <CardHeader class="space-y-3 border-b border-slate-100 px-7 pb-5 pt-7">
          <div class="flex items-start justify-between gap-5">
            <div>
              <CardTitle class="font-display text-2xl font-semibold text-slate-950">
                忘记密码
              </CardTitle>
              <CardDescription class="mt-2 text-sm leading-6 text-slate-500">
                当前页面为找回流程占位，请按身份联系对应管理人员。
              </CardDescription>
            </div>
            <div class="grid size-11 place-items-center rounded-lg bg-teal-50 text-[#0f766e]">
              <MailQuestion class="size-5" />
            </div>
          </div>
        </CardHeader>

        <CardContent class="space-y-5 px-7 pb-7 pt-6">
          <div class="rounded-lg border border-slate-200 bg-slate-50 p-4">
            <div class="flex gap-3">
              <div class="mt-0.5 grid size-8 shrink-0 place-items-center rounded-lg bg-white text-[#155e75]">
                <LifeBuoy class="size-4" />
              </div>
              <div>
                <p class="text-sm font-semibold text-slate-950">学生账号</p>
                <p class="mt-1 text-sm leading-6 text-slate-600">
                  请联系辅导员、学院教学秘书或校级教务管理员，完成身份核验后重置密码。
                </p>
              </div>
            </div>
          </div>

          <div class="rounded-lg border border-slate-200 bg-slate-50 p-4">
            <div class="flex gap-3">
              <div class="mt-0.5 grid size-8 shrink-0 place-items-center rounded-lg bg-white text-[#155e75]">
                <Building2 class="size-4" />
              </div>
              <div>
                <p class="text-sm font-semibold text-slate-950">教师账号</p>
                <p class="mt-1 text-sm leading-6 text-slate-600">
                  如遇工号或密码异常，请联系教务处或信息化服务中心进行账号核验与重置。
                </p>
              </div>
            </div>
          </div>

          <Button as-child class="h-10 w-full rounded-lg bg-[#155e75] text-white hover:bg-[#164e63]">
            <RouterLink to="/login">
              <ArrowLeft class="size-4" />
              返回登录页
            </RouterLink>
          </Button>
        </CardContent>
      </Card>
    </section>
  </main>
</template>
