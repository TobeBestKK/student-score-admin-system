import { ref, watch } from 'vue'

const theme = ref(localStorage.getItem('theme') || 'light')

export function useTheme() {
  const setTheme = (newTheme: string) => {
    theme.value = newTheme
    localStorage.setItem('theme', newTheme)
    
    if (newTheme === 'dark') {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }

  const toggleTheme = () => {
    setTheme(theme.value === 'light' ? 'dark' : 'light')
  }

  // 初始化主题
  if (theme.value === 'dark') {
    document.documentElement.classList.add('dark')
  }

  watch(theme, (newTheme) => {
    if (newTheme === 'dark') {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  })

  return {
    theme,
    setTheme,
    toggleTheme,
  }
}

export function getTheme() {
  return theme.value
}
