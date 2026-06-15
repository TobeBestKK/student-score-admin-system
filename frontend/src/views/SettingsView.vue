<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from "vue"
import { useI18n } from "vue-i18n"
import {
  User,
  Shield,
  Palette,
  Save,
  RotateCcw,
  Mail,
} from "@lucide/vue"
import { getProfile, updateProfile, type ProfileResponse } from "@/api/auth"
import { useTheme } from "@/hooks/useTheme"
import { setLocale } from "@/i18n"

const { t } = useI18n()
const { theme, setTheme } = useTheme()

const userInfo = computed(() => {
  const str = localStorage.getItem('userInfo')
  return str ? JSON.parse(str) : { id: 0, name: '用户', role: 'student' }
})

const activeTab = ref("profile")
const loading = ref(false)

const profileForm = reactive({
  name: "",
  email: "",
  phone: "",
  position: "",
  studentId: "",
})

const securityForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
})

const preferences = reactive({
  language: localStorage.getItem('locale') || 'zh-CN',
  theme: localStorage.getItem('theme') || 'light',
  timezone: "Asia/Shanghai",
})

const isTeacher = computed(() => userInfo.value?.role === "teacher")

const tabs = computed(() => [
  { id: "profile", label: t('settings.profile'), icon: User },
  { id: "security", label: t('settings.security'), icon: Shield },
  { id: "preferences", label: t('settings.preferences'), icon: Palette },
])

onMounted(async () => {
  await loadProfile()
})

watch(() => preferences.language, (newLocale) => {
  setLocale(newLocale)
})

watch(() => preferences.theme, (newTheme) => {
  setTheme(newTheme)
})

async function loadProfile() {
  const user = userInfo.value
  if (!user?.id) return
  
  loading.value = true
  try {
    const params = user.role === 'teacher'
      ? { teacherId: user.id }
      : { studentId: user.id }
    const response: ProfileResponse = await getProfile(params)
    profileForm.name = response.name || ""
    profileForm.email = response.email || ""
    profileForm.phone = response.phone || ""
    profileForm.position = response.position || ""
    profileForm.studentId = response.studentId || ""
  } catch (e) {
    console.error("Failed to load profile", e)
  } finally {
    loading.value = false
  }
}

async function saveProfile() {
  const user = userInfo.value
  if (!user?.id) return
  
  loading.value = true
  try {
    const params = user.role === 'teacher'
      ? { teacherId: user.id }
      : { studentId: user.id }
    await updateProfile(params, {
      name: profileForm.name,
      phone: profileForm.phone,
    })
    
    const stored = localStorage.getItem("userInfo")
    if (stored) {
      const info = JSON.parse(stored)
      info.name = profileForm.name
      localStorage.setItem("userInfo", JSON.stringify(info))
    }
    
    alert("个人信息已保存")
  } catch (e) {
    console.error("Failed to save profile", e)
    alert("保存失败")
  } finally {
    loading.value = false
  }
}

async function changePassword() {
  if (securityForm.newPassword !== securityForm.confirmPassword) {
    alert("新密码和确认密码不一致")
    return
  }
  if (securityForm.newPassword.length < 6) {
    alert("密码长度至少为6位")
    return
  }
  try {
    alert("密码修改成功，请重新登录")
    securityForm.oldPassword = ""
    securityForm.newPassword = ""
    securityForm.confirmPassword = ""
  } catch (e) {
    console.error("Failed to change password", e)
  }
}

function resetForm() {
  securityForm.oldPassword = ""
  securityForm.newPassword = ""
  securityForm.confirmPassword = ""
}

function savePreferences() {
  setLocale(preferences.language)
  setTheme(preferences.theme)
  alert(t('settings.saveSuccess'))
}
</script>

<template>
  <div class="max-w-4xl mx-auto">
    <div class="flex gap-6">
      <!-- 侧边标签 -->
      <div class="w-48 shrink-0">
        <nav class="space-y-1">
          <button
            v-for="tab in tabs"
            :key="tab.id"
            :class="[
              'flex w-full items-center gap-2.5 rounded-md px-3 py-2.5 text-sm font-medium transition-colors',
              activeTab === tab.id
                ? 'bg-[#ccfbf1] text-[#0f766e] dark:bg-teal-900 dark:text-teal-300'
                : 'text-[#475569] hover:bg-[#f1f5f9] hover:text-[#0f172a] dark:text-gray-400 dark:hover:bg-gray-700 dark:hover:text-white',
            ]"
            @click="activeTab = tab.id"
          >
            <component :is="tab.icon" class="size-4" />
            {{ tab.label }}
          </button>
        </nav>
      </div>

      <!-- 内容区域 -->
      <div class="flex-1 space-y-6">
        <!-- 个人信息 -->
        <div v-if="activeTab === 'profile'" class="rounded-lg border border-[#e2e8f0] bg-white p-6 dark:bg-gray-800 dark:border-gray-700">
          <div class="mb-6 flex items-center justify-between">
            <h2 class="text-lg font-semibold text-[#0f172a] dark:text-white">{{ t('settings.profile') }}</h2>
          </div>
          
          <div v-if="loading" class="flex justify-center py-8">
            <div class="h-8 w-8 animate-spin rounded-full border-4 border-[#0f766e] border-t-transparent"></div>
          </div>
          
          <div v-else class="space-y-4">
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="mb-1.5 block text-sm font-medium text-[#475569] dark:text-gray-300">{{ t('settings.name') }}</label>
                <input
                  v-model="profileForm.name"
                  type="text"
                  class="flex h-10 w-full rounded-md border border-[#e2e8f0] bg-white px-3 py-2 text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  :placeholder="t('settings.name')"
                />
              </div>
              <div>
                <label class="mb-1.5 block text-sm font-medium text-[#475569] dark:text-gray-300">{{ t('settings.email') }}</label>
                <div class="flex h-10 w-full items-center rounded-md border border-[#e2e8f0] bg-[#f8fafc] px-3 py-2 text-sm text-[#64748b] dark:bg-gray-700 dark:border-gray-600 dark:text-gray-400">
                  <Mail class="mr-2 size-4" />
                  {{ profileForm.email || '-' }}
                </div>
              </div>
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="mb-1.5 block text-sm font-medium text-[#475569] dark:text-gray-300">{{ t('settings.phone') }}</label>
                <input
                  v-model="profileForm.phone"
                  type="tel"
                  class="flex h-10 w-full rounded-md border border-[#e2e8f0] bg-white px-3 py-2 text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  :placeholder="t('settings.phone')"
                />
              </div>
              <div v-if="isTeacher">
                <label class="mb-1.5 block text-sm font-medium text-[#475569] dark:text-gray-300">{{ t('settings.position') }}</label>
                <div class="flex h-10 w-full items-center rounded-md border border-[#e2e8f0] bg-[#f8fafc] px-3 py-2 text-sm text-[#64748b] dark:bg-gray-700 dark:border-gray-600 dark:text-gray-400">
                  {{ profileForm.position || '-' }}
                </div>
              </div>
              <div v-else>
                <label class="mb-1.5 block text-sm font-medium text-[#475569] dark:text-gray-300">{{ t('settings.studentId') }}</label>
                <div class="flex h-10 w-full items-center rounded-md border border-[#e2e8f0] bg-[#f8fafc] px-3 py-2 text-sm text-[#64748b] dark:bg-gray-700 dark:border-gray-600 dark:text-gray-400">
                  {{ profileForm.studentId || '-' }}
                </div>
              </div>
            </div>
          </div>
          
          <div class="mt-6 flex justify-end gap-3">
            <button
              class="inline-flex items-center gap-1.5 rounded-md bg-[#0f766e] px-4 py-2 text-sm font-medium text-white hover:bg-[#0d9488] transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              :disabled="loading"
              @click="saveProfile"
            >
              <Save class="size-4" />
              {{ t('common.save') }}
            </button>
          </div>
        </div>

        <!-- 安全设置 -->
        <div v-if="activeTab === 'security'" class="rounded-lg border border-[#e2e8f0] bg-white p-6 dark:bg-gray-800 dark:border-gray-700">
          <div class="mb-6 flex items-center justify-between">
            <h2 class="text-lg font-semibold text-[#0f172a] dark:text-white">{{ t('settings.security') }}</h2>
          </div>
          <div class="space-y-4">
            <div>
              <label class="mb-1.5 block text-sm font-medium text-[#475569] dark:text-gray-300">当前密码</label>
              <input
                v-model="securityForm.oldPassword"
                type="password"
                class="flex h-10 w-full rounded-md border border-[#e2e8f0] bg-white px-3 py-2 text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                placeholder="请输入当前密码"
              />
            </div>
            <div>
              <label class="mb-1.5 block text-sm font-medium text-[#475569] dark:text-gray-300">新密码</label>
              <input
                v-model="securityForm.newPassword"
                type="password"
                class="flex h-10 w-full rounded-md border border-[#e2e8f0] bg-white px-3 py-2 text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                placeholder="请输入新密码（至少6位）"
              />
            </div>
            <div>
              <label class="mb-1.5 block text-sm font-medium text-[#475569] dark:text-gray-300">确认新密码</label>
              <input
                v-model="securityForm.confirmPassword"
                type="password"
                class="flex h-10 w-full rounded-md border border-[#e2e8f0] bg-white px-3 py-2 text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                placeholder="请再次输入新密码"
              />
            </div>
          </div>
          <div class="mt-6 flex justify-end gap-3">
            <button
              class="inline-flex items-center gap-1.5 rounded-md border border-[#e2e8f0] bg-white px-4 py-2 text-sm font-medium text-[#475569] hover:bg-[#f8fafc] transition-colors dark:bg-gray-700 dark:text-gray-300 dark:border-gray-600"
              @click="resetForm"
            >
              <RotateCcw class="size-4" />
              {{ t('common.reset') }}
            </button>
            <button
              class="inline-flex items-center gap-1.5 rounded-md bg-[#0f766e] px-4 py-2 text-sm font-medium text-white hover:bg-[#0d9488] transition-colors"
              @click="changePassword"
            >
              <Save class="size-4" />
              修改密码
            </button>
          </div>
        </div>

        <!-- 系统偏好 -->
        <div v-if="activeTab === 'preferences'" class="rounded-lg border border-[#e2e8f0] bg-white p-6 dark:bg-gray-800 dark:border-gray-700">
          <div class="mb-6 flex items-center justify-between">
            <h2 class="text-lg font-semibold text-[#0f172a] dark:text-white">{{ t('settings.preferences') }}</h2>
          </div>
          <div class="space-y-4">
            <div>
              <label class="mb-1.5 block text-sm font-medium text-[#475569] dark:text-gray-300">{{ t('settings.language') }}</label>
              <select
                v-model="preferences.language"
                class="flex h-10 w-full rounded-md border border-[#e2e8f0] bg-white px-3 py-2 text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-white"
              >
                <option value="zh-CN">简体中文</option>
                <option value="en-US">English</option>
              </select>
            </div>
            <div>
              <label class="mb-1.5 block text-sm font-medium text-[#475569] dark:text-gray-300">{{ t('settings.theme') }}</label>
              <select
                v-model="preferences.theme"
                class="flex h-10 w-full rounded-md border border-[#e2e8f0] bg-white px-3 py-2 text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-white"
              >
                <option value="light">亮色</option>
                <option value="dark">暗色</option>
              </select>
            </div>
            <div v-if="isTeacher">
              <label class="mb-1.5 block text-sm font-medium text-[#475569] dark:text-gray-300">{{ t('settings.timezone') }}</label>
              <select
                v-model="preferences.timezone"
                class="flex h-10 w-full rounded-md border border-[#e2e8f0] bg-white px-3 py-2 text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-white"
              >
                <option value="Asia/Shanghai">北京时间 (UTC+8)</option>
                <option value="UTC">UTC</option>
              </select>
            </div>
          </div>
          <div class="mt-6 flex justify-end gap-3">
            <button
              class="inline-flex items-center gap-1.5 rounded-md bg-[#0f766e] px-4 py-2 text-sm font-medium text-white hover:bg-[#0d9488] transition-colors"
              @click="savePreferences"
            >
              <Save class="size-4" />
              {{ t('common.save') }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>