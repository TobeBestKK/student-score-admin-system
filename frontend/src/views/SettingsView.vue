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
  CircleUser,
  Eye,
  EyeOff,
  Check,
  X,
} from "@lucide/vue"
import { getProfile, updateProfile, type ProfileResponse } from "@/api/auth"
import { useTheme } from "@/hooks/useTheme"
import { setLocale } from "@/i18n"

const { t } = useI18n()
const { setTheme } = useTheme()

const userInfo = computed(() => {
  const str = localStorage.getItem("userInfo")
  return str ? JSON.parse(str) : { id: 0, name: "用户", role: "student" }
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
  language: localStorage.getItem("locale") || "zh-CN",
  theme: localStorage.getItem("theme") || "light",
})

const showOldPassword = ref(false)
const showNewPassword = ref(false)
const showConfirmPassword = ref(false)

const toast = reactive({
  show: false,
  message: "",
  type: "success" as "success" | "error",
})

const isTeacher = computed(() => userInfo.value?.role === "teacher")

const tabs = computed(() => [
  { id: "profile", label: t("settings.profile"), icon: User },
  { id: "security", label: t("settings.security"), icon: Shield },
  { id: "preferences", label: t("settings.preferences"), icon: Palette },
])

const passwordStrength = computed(() => {
  const pwd = securityForm.newPassword
  if (!pwd) return { level: 0, label: "", color: "" }

  let score = 0
  if (pwd.length >= 6) score++
  if (pwd.length >= 10) score++
  if (/[A-Z]/.test(pwd)) score++
  if (/[0-9]/.test(pwd)) score++
  if (/[^A-Za-z0-9]/.test(pwd)) score++

  if (score <= 2) return { level: 1, label: t("settings.passwordWeak"), color: "bg-red-500" }
  if (score <= 3) return { level: 2, label: t("settings.passwordMedium"), color: "bg-yellow-500" }
  return { level: 3, label: t("settings.passwordStrong"), color: "bg-green-500" }
})

function showToast(message: string, type: "success" | "error" = "success") {
  toast.show = true
  toast.message = message
  toast.type = type
  setTimeout(() => {
    toast.show = false
  }, 3000)
}

onMounted(async () => {
  await loadProfile()
})

watch(
  () => preferences.language,
  (newLocale) => {
    setLocale(newLocale)
  }
)

watch(
  () => preferences.theme,
  (newTheme) => {
    setTheme(newTheme)
  }
)

async function loadProfile() {
  const user = userInfo.value
  if (!user?.id) return

  loading.value = true
  try {
    const params =
      user.role === "teacher" ? { teacherId: user.id } : { studentId: user.id }
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
    const params =
      user.role === "teacher" ? { teacherId: user.id } : { studentId: user.id }
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

    showToast(t("settings.profileSaved"))
  } catch (e) {
    console.error("Failed to save profile", e)
    showToast(t("settings.saveFailed"), "error")
  } finally {
    loading.value = false
  }
}

async function changePassword() {
  if (securityForm.newPassword !== securityForm.confirmPassword) {
    showToast(t("settings.passwordMismatch"), "error")
    return
  }
  if (securityForm.newPassword.length < 6) {
    showToast(t("settings.passwordMinLength"), "error")
    return
  }
  try {
    showToast(t("settings.passwordChanged"))
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
  showToast(t("settings.saveSuccess"))
}
</script>

<template>
  <div class="mx-auto max-w-5xl">
    <!-- Toast 通知 -->
    <Transition
      enter-active-class="transition-all duration-300 ease-out"
      enter-from-class="translate-y-[-100%] opacity-0"
      enter-to-class="translate-y-0 opacity-100"
      leave-active-class="transition-all duration-200 ease-in"
      leave-from-class="translate-y-0 opacity-100"
      leave-to-class="translate-y-[-100%] opacity-0"
    >
      <div
        v-if="toast.show"
        :class="[
          'fixed left-1/2 top-4 z-50 flex -translate-x-1/2 items-center gap-2 rounded-lg px-4 py-3 shadow-lg',
          toast.type === 'success'
            ? 'bg-green-50 text-green-800 dark:bg-green-900/30 dark:text-green-300'
            : 'bg-red-50 text-red-800 dark:bg-red-900/30 dark:text-red-300',
        ]"
      >
        <Check v-if="toast.type === 'success'" class="size-4" />
        <X v-else class="size-4" />
        <span class="text-sm font-medium">{{ toast.message }}</span>
      </div>
    </Transition>

    <div class="flex gap-6">
      <!-- 侧边栏 -->
      <div class="w-64 shrink-0">
        <!-- 用户头像卡片 -->
        <div
          class="mb-4 rounded-lg border border-[#e2e8f0] bg-white p-4 dark:bg-gray-800 dark:border-gray-700"
        >
          <div class="flex flex-col items-center text-center">
            <div
              class="mb-3 flex size-16 items-center justify-center rounded-full bg-[#ccfbf1] dark:bg-teal-900/30"
            >
              <CircleUser class="size-10 text-[#0f766e] dark:text-teal-400" />
            </div>
            <p class="text-base font-semibold text-[#0f172a] dark:text-white">
              {{ userInfo.name || t("settings.name") }}
            </p>
            <p class="mt-1 text-xs text-[#64748b] dark:text-gray-400">
              {{ isTeacher ? t("settings.roleTeacher") : t("settings.roleStudent") }}
            </p>
            <p
              class="mt-2 rounded-md bg-[#f1f5f9] px-2 py-1 text-xs text-[#475569] dark:bg-gray-700 dark:text-gray-300"
            >
              {{ isTeacher ? t("settings.teacherNo") : t("settings.studentId") }}:
              {{ isTeacher ? profileForm.position || "-" : profileForm.studentId || "-" }}
            </p>
          </div>
        </div>

        <!-- 导航菜单 -->
        <nav
          class="rounded-lg border border-[#e2e8f0] bg-white dark:bg-gray-800 dark:border-gray-700"
        >
          <!-- 账户设置 -->
          <div class="px-3 pt-3 pb-1">
            <p class="text-xs font-medium uppercase tracking-wider text-[#94a3b8] dark:text-gray-500">
              {{ t("settings.accountSettings") }}
            </p>
          </div>
          <div class="space-y-0.5 px-2 pb-2">
            <button
              v-for="tab in tabs.slice(0, 2)"
              :key="tab.id"
              :class="[
                'flex w-full items-center gap-2.5 rounded-md px-3 py-2 text-sm font-medium transition-colors',
                activeTab === tab.id
                  ? 'bg-[#ccfbf1] text-[#0f766e] dark:bg-teal-900 dark:text-teal-300'
                  : 'text-[#475569] hover:bg-[#f1f5f9] hover:text-[#0f172a] dark:text-gray-400 dark:hover:bg-gray-700 dark:hover:text-white',
              ]"
              @click="activeTab = tab.id"
            >
              <component :is="tab.icon" class="size-4" />
              {{ tab.label }}
            </button>
          </div>

          <!-- 系统设置 -->
          <div class="border-t border-[#e2e8f0] px-3 pt-3 pb-1 dark:border-gray-700">
            <p class="text-xs font-medium uppercase tracking-wider text-[#94a3b8] dark:text-gray-500">
              {{ t("settings.systemSettingsGroup") }}
            </p>
          </div>
          <div class="space-y-0.5 px-2 pb-3">
            <button
              :class="[
                'flex w-full items-center gap-2.5 rounded-md px-3 py-2 text-sm font-medium transition-colors',
                activeTab === 'preferences'
                  ? 'bg-[#ccfbf1] text-[#0f766e] dark:bg-teal-900 dark:text-teal-300'
                  : 'text-[#475569] hover:bg-[#f1f5f9] hover:text-[#0f172a] dark:text-gray-400 dark:hover:bg-gray-700 dark:hover:text-white',
              ]"
              @click="activeTab = 'preferences'"
            >
              <Palette class="size-4" />
              {{ t("settings.preferences") }}
            </button>
          </div>
        </nav>
      </div>

      <!-- 内容区域 -->
      <div class="flex-1">
        <!-- 个人信息 -->
        <div
          v-if="activeTab === 'profile'"
          class="space-y-6"
        >
          <!-- 加载状态 -->
          <div
            v-if="loading"
            class="flex justify-center rounded-lg border border-[#e2e8f0] bg-white py-12 dark:bg-gray-800 dark:border-gray-700"
          >
            <div
              class="size-8 animate-spin rounded-full border-4 border-[#0f766e] border-t-transparent"
            ></div>
          </div>

          <template v-else>
            <!-- 账户信息（只读） -->
            <div
              class="rounded-lg border border-[#e2e8f0] bg-white dark:bg-gray-800 dark:border-gray-700"
            >
              <div class="border-b border-[#e2e8f0] px-6 py-4 dark:border-gray-700">
                <h2 class="text-base font-semibold text-[#0f172a] dark:text-white">
                  {{ t("settings.accountInfo") }}
                </h2>
                <p class="mt-1 text-sm text-[#64748b] dark:text-gray-400">
                  {{ t("settings.accountInfoDesc") }}
                </p>
              </div>
              <div class="grid grid-cols-2 gap-6 p-6">
                <div>
                  <label
                    class="mb-1.5 block text-sm font-medium text-[#475569] dark:text-gray-300"
                    >{{ t("settings.email") }}</label
                  >
                  <div
                    class="flex h-10 w-full items-center rounded-md border border-[#e2e8f0] bg-[#f8fafc] px-3 text-sm text-[#64748b] dark:bg-gray-700 dark:border-gray-600 dark:text-gray-400"
                  >
                    <Mail class="mr-2 size-4 shrink-0" />
                    <span class="truncate">{{ profileForm.email || "-" }}</span>
                  </div>
                </div>
                <div>
                  <label
                    class="mb-1.5 block text-sm font-medium text-[#475569] dark:text-gray-300"
                    >{{ isTeacher ? t("settings.position") : t("settings.studentId") }}</label
                  >
                  <div
                    class="flex h-10 w-full items-center rounded-md border border-[#e2e8f0] bg-[#f8fafc] px-3 text-sm text-[#64748b] dark:bg-gray-700 dark:border-gray-600 dark:text-gray-400"
                  >
                    {{
                      isTeacher
                        ? profileForm.position || "-"
                        : profileForm.studentId || "-"
                    }}
                  </div>
                </div>
              </div>
            </div>

            <!-- 基本信息（可编辑） -->
            <div
              class="rounded-lg border border-[#e2e8f0] bg-white dark:bg-gray-800 dark:border-gray-700"
            >
              <div class="border-b border-[#e2e8f0] px-6 py-4 dark:border-gray-700">
                <h2 class="text-base font-semibold text-[#0f172a] dark:text-white">
                  {{ t("settings.basicInfo") }}
                </h2>
                <p class="mt-1 text-sm text-[#64748b] dark:text-gray-400">
                  {{ t("settings.basicInfoDesc") }}
                </p>
              </div>
              <div class="grid grid-cols-2 gap-6 p-6">
                <div>
                  <label
                    class="mb-1.5 block text-sm font-medium text-[#475569] dark:text-gray-300"
                    >{{ t("settings.name") }}
                    <span class="text-red-500">*</span></label
                  >
                  <input
                    v-model="profileForm.name"
                    type="text"
                    class="flex h-10 w-full rounded-md border border-[#e2e8f0] bg-white px-3 py-2 text-sm transition-colors focus:border-[#0f766e] focus:outline-none focus:ring-1 focus:ring-[#0f766e] dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                    :placeholder="t('settings.name')"
                  />
                </div>
                <div>
                  <label
                    class="mb-1.5 block text-sm font-medium text-[#475569] dark:text-gray-300"
                    >{{ t("settings.phone") }}</label
                  >
                  <input
                    v-model="profileForm.phone"
                    type="tel"
                    class="flex h-10 w-full rounded-md border border-[#e2e8f0] bg-white px-3 py-2 text-sm transition-colors focus:border-[#0f766e] focus:outline-none focus:ring-1 focus:ring-[#0f766e] dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                    :placeholder="t('settings.phone')"
                  />
                </div>
              </div>
              <div
                class="flex justify-end border-t border-[#e2e8f0] px-6 py-4 dark:border-gray-700"
              >
                <button
                  class="inline-flex items-center gap-1.5 rounded-md bg-[#0f766e] px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-[#0d9488] disabled:cursor-not-allowed disabled:opacity-50"
                  :disabled="loading"
                  @click="saveProfile"
                >
                  <Save class="size-4" />
                  {{ t("common.save") }}
                </button>
              </div>
            </div>
          </template>
        </div>

        <!-- 安全设置 -->
        <div
          v-if="activeTab === 'security'"
          class="rounded-lg border border-[#e2e8f0] bg-white dark:bg-gray-800 dark:border-gray-700"
        >
          <div class="border-b border-[#e2e8f0] px-6 py-4 dark:border-gray-700">
            <h2 class="text-base font-semibold text-[#0f172a] dark:text-white">
              {{ t("settings.changePassword") }}
            </h2>
            <p class="mt-1 text-sm text-[#64748b] dark:text-gray-400">
              {{ t("settings.changePasswordDesc") }}
            </p>
          </div>
          <div class="space-y-5 p-6">
            <!-- 当前密码 -->
            <div>
              <label class="mb-1.5 block text-sm font-medium text-[#475569] dark:text-gray-300">{{
                t("settings.currentPassword")
              }}</label>
              <div class="relative">
                <input
                  v-model="securityForm.oldPassword"
                  :type="showOldPassword ? 'text' : 'password'"
                  class="flex h-10 w-full rounded-md border border-[#e2e8f0] bg-white px-3 py-2 pr-10 text-sm transition-colors focus:border-[#0f766e] focus:outline-none focus:ring-1 focus:ring-[#0f766e] dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  :placeholder="t('settings.inputCurrentPassword')"
                />
                <button
                  type="button"
                  class="absolute right-3 top-1/2 -translate-y-1/2 text-[#94a3b8] hover:text-[#475569] dark:text-gray-500 dark:hover:text-gray-300"
                  @click="showOldPassword = !showOldPassword"
                >
                  <EyeOff v-if="showOldPassword" class="size-4" />
                  <Eye v-else class="size-4" />
                </button>
              </div>
            </div>

            <!-- 新密码 -->
            <div>
              <label class="mb-1.5 block text-sm font-medium text-[#475569] dark:text-gray-300">{{
                t("settings.newPassword")
              }}</label>
              <div class="relative">
                <input
                  v-model="securityForm.newPassword"
                  :type="showNewPassword ? 'text' : 'password'"
                  class="flex h-10 w-full rounded-md border border-[#e2e8f0] bg-white px-3 py-2 pr-10 text-sm transition-colors focus:border-[#0f766e] focus:outline-none focus:ring-1 focus:ring-[#0f766e] dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  :placeholder="t('settings.inputNewPassword')"
                />
                <button
                  type="button"
                  class="absolute right-3 top-1/2 -translate-y-1/2 text-[#94a3b8] hover:text-[#475569] dark:text-gray-500 dark:hover:text-gray-300"
                  @click="showNewPassword = !showNewPassword"
                >
                  <EyeOff v-if="showNewPassword" class="size-4" />
                  <Eye v-else class="size-4" />
                </button>
              </div>

              <!-- 密码强度指示器 -->
              <div v-if="securityForm.newPassword" class="mt-3">
                <div class="mb-1.5 flex items-center justify-between">
                  <span class="text-xs text-[#64748b] dark:text-gray-400">{{
                    t("settings.passwordStrength")
                  }}</span>
                  <span
                    :class="[
                      'text-xs font-medium',
                      passwordStrength.level === 1
                        ? 'text-red-500'
                        : passwordStrength.level === 2
                          ? 'text-yellow-500'
                          : 'text-green-500',
                    ]"
                    >{{ passwordStrength.label }}</span
                  >
                </div>
                <div class="flex gap-1">
                  <div
                    v-for="i in 3"
                    :key="i"
                    :class="[
                      'h-1.5 flex-1 rounded-full transition-colors',
                      i <= passwordStrength.level
                        ? passwordStrength.color
                        : 'bg-[#e2e8f0] dark:bg-gray-700',
                    ]"
                  ></div>
                </div>
              </div>

              <!-- 密码要求 -->
              <div
                class="mt-3 rounded-md bg-[#f8fafc] px-3 py-2 dark:bg-gray-700/50"
              >
                <p class="mb-1 text-xs font-medium text-[#475569] dark:text-gray-300">
                  {{ t("settings.passwordRequirement") }}:
                </p>
                <ul class="space-y-0.5">
                  <li class="flex items-center gap-1.5 text-xs text-[#64748b] dark:text-gray-400">
                    <div
                      :class="[
                        'size-1.5 rounded-full',
                        securityForm.newPassword.length >= 6
                          ? 'bg-green-500'
                          : 'bg-[#cbd5e1] dark:bg-gray-600',
                      ]"
                    ></div>
                    {{ t("settings.passwordMinChars") }}
                  </li>
                </ul>
              </div>
            </div>

            <!-- 确认新密码 -->
            <div>
              <label class="mb-1.5 block text-sm font-medium text-[#475569] dark:text-gray-300">{{
                t("settings.confirmPassword")
              }}</label>
              <div class="relative">
                <input
                  v-model="securityForm.confirmPassword"
                  :type="showConfirmPassword ? 'text' : 'password'"
                  class="flex h-10 w-full rounded-md border border-[#e2e8f0] bg-white px-3 py-2 pr-10 text-sm transition-colors focus:border-[#0f766e] focus:outline-none focus:ring-1 focus:ring-[#0f766e] dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  :placeholder="t('settings.inputConfirmPassword')"
                />
                <button
                  type="button"
                  class="absolute right-3 top-1/2 -translate-y-1/2 text-[#94a3b8] hover:text-[#475569] dark:text-gray-500 dark:hover:text-gray-300"
                  @click="showConfirmPassword = !showConfirmPassword"
                >
                  <EyeOff v-if="showConfirmPassword" class="size-4" />
                  <Eye v-else class="size-4" />
                </button>
              </div>
              <!-- 密码匹配提示 -->
              <p
                v-if="
                  securityForm.confirmPassword &&
                  securityForm.newPassword !== securityForm.confirmPassword
                "
                class="mt-1.5 text-xs text-red-500"
              >
                {{ t("settings.passwordMismatch") }}
              </p>
            </div>
          </div>
          <div
            class="flex justify-end gap-3 border-t border-[#e2e8f0] px-6 py-4 dark:border-gray-700"
          >
            <button
              class="inline-flex items-center gap-1.5 rounded-md border border-[#e2e8f0] bg-white px-4 py-2 text-sm font-medium text-[#475569] transition-colors hover:bg-[#f8fafc] dark:bg-gray-700 dark:text-gray-300 dark:border-gray-600"
              @click="resetForm"
            >
              <RotateCcw class="size-4" />
              {{ t("common.reset") }}
            </button>
            <button
              class="inline-flex items-center gap-1.5 rounded-md bg-[#0f766e] px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-[#0d9488]"
              @click="changePassword"
            >
              <Save class="size-4" />
              {{ t("settings.changePassword") }}
            </button>
          </div>
        </div>

        <!-- 系统偏好 -->
        <div
          v-if="activeTab === 'preferences'"
          class="rounded-lg border border-[#e2e8f0] bg-white dark:bg-gray-800 dark:border-gray-700"
        >
          <div class="border-b border-[#e2e8f0] px-6 py-4 dark:border-gray-700">
            <h2 class="text-base font-semibold text-[#0f172a] dark:text-white">
              {{ t("settings.interfaceSettings") }}
            </h2>
            <p class="mt-1 text-sm text-[#64748b] dark:text-gray-400">
              {{ t("settings.interfaceSettingsDesc") }}
            </p>
          </div>
          <div class="space-y-5 p-6">
            <div>
              <label class="mb-1.5 block text-sm font-medium text-[#475569] dark:text-gray-300">{{
                t("settings.language")
              }}</label>
              <select
                v-model="preferences.language"
                class="flex h-10 w-full rounded-md border border-[#e2e8f0] bg-white px-3 py-2 text-sm transition-colors focus:border-[#0f766e] focus:outline-none focus:ring-1 focus:ring-[#0f766e] dark:bg-gray-700 dark:border-gray-600 dark:text-white"
              >
                <option value="zh-CN">{{ t("language.zh-CN") }}</option>
                <option value="en-US">English</option>
              </select>
            </div>
            <div>
              <label class="mb-1.5 block text-sm font-medium text-[#475569] dark:text-gray-300">{{
                t("settings.theme")
              }}</label>
              <select
                v-model="preferences.theme"
                class="flex h-10 w-full rounded-md border border-[#e2e8f0] bg-white px-3 py-2 text-sm transition-colors focus:border-[#0f766e] focus:outline-none focus:ring-1 focus:ring-[#0f766e] dark:bg-gray-700 dark:border-gray-600 dark:text-white"
              >
                <option value="light">{{ t("theme.light") }}</option>
                <option value="dark">{{ t("theme.dark") }}</option>
              </select>
            </div>
          </div>
          <div
            class="flex justify-end border-t border-[#e2e8f0] px-6 py-4 dark:border-gray-700"
          >
            <button
              class="inline-flex items-center gap-1.5 rounded-md bg-[#0f766e] px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-[#0d9488]"
              @click="savePreferences"
            >
              <Save class="size-4" />
              {{ t("settings.savePreferences") }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
