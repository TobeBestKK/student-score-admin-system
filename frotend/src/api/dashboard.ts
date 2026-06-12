import api from './index'

export interface CourseOption {
  id: number
  courseName: string
}

export interface SemesterOption {
  academicYear: string
  semester: string
  label: string
}

export interface DashboardStats {
  courseCount: number
  studentCount: number
  averageScore: number
  failingCount: number
  maxScore: number
  passRate: number
}

export interface ScoreDistribution {
  labels: string[]
  counts: number[]
}

export interface TopStudent {
  rank: number
  name: string
  className: string
  score: number
}

export interface Warning {
  type: string
  count: number
}

export interface RecentRecord {
  id: number
  studentName: string
  courseName: string
  scoreValue: number
  examType: string
  createTime: string
}

export function fetchCourseOptions(): Promise<CourseOption[]> {
  return api.get('/dashboard/courses')
}

export function fetchSemesterOptions(): Promise<SemesterOption[]> {
  return api.get('/dashboard/semesters')
}

export function fetchDashboardStats(params?: { academicYear?: string; semester?: string }): Promise<DashboardStats> {
  return api.get('/dashboard/stats', { params })
}

export function fetchScoreDistribution(params?: { courseId?: number }): Promise<ScoreDistribution> {
  return api.get('/dashboard/distribution', { params })
}

export function fetchTop5BySubject(params?: { courseId?: number }): Promise<TopStudent[]> {
  return api.get('/dashboard/top5/subject', { params })
}

export function fetchTop5Total(params?: { academicYear?: string; semester?: string }): Promise<TopStudent[]> {
  return api.get('/dashboard/top5/total', { params })
}

export function fetchWarnings(params?: { academicYear?: string; semester?: string }): Promise<Warning[]> {
  return api.get('/dashboard/warnings', { params })
}

export function fetchRecentRecords(limit?: number): Promise<RecentRecord[]> {
  return api.get('/dashboard/recent', { params: { limit: limit ?? 5 } })
}
