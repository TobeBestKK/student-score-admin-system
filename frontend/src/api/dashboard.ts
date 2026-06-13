import api from './index'

// ========== 通用类型 ==========

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

// ========== 学生端类型 ==========

export interface StudentProfile {
  id: number
  name: string
  studentNo: string
  className: string
  major: string
  grade: number
  enrollmentYear: number
}

export interface StudentCourseScore {
  courseId: number
  courseName: string
  scoreValue: number
  examType: string
  credit: number
  classRank: number
  classTotal: number
  gradeRank: number
  gradeTotal: number
}

export interface StudentStats {
  totalScore: number
  averageScore: number
  earnedCredit: number
  failCount: number
  classRank: number
  classTotal: number
  gradeRank: number
  gradeTotal: number
  totalCredit: number
  creditWarning: boolean
}

export interface ScoreTrend {
  examLabels: string[]
  studentScores: number[]
  classAverages: number[]
  gradeAverages: number[]
}

// ========== 学生端 API ==========

export function fetchStudentProfile(studentId: number): Promise<StudentProfile> {
  return api.get('/dashboard/student/profile', { params: { studentId } })
}

export function fetchStudentScores(params: {
  studentId: number
  academicYear?: string
  semester?: string
  examType?: string
}): Promise<StudentCourseScore[]> {
  return api.get('/dashboard/student/scores', { params })
}

export function fetchStudentStats(params: {
  studentId: number
  academicYear?: string
  semester?: string
  examType?: string
}): Promise<StudentStats> {
  return api.get('/dashboard/student/stats', { params })
}

export function fetchScoreTrend(studentId: number): Promise<ScoreTrend> {
  return api.get('/dashboard/student/trend', { params: { studentId } })
}

export interface RadarChart {
  courseNames: string[]
  studentScores: number[]
  classAverages: number[]
  gradeAverages: number[]
}

export function fetchRadarData(params: {
  studentId: number
  academicYear?: string
  semester?: string
  examType?: string
}): Promise<RadarChart> {
  return api.get('/dashboard/student/radar', { params })
}
