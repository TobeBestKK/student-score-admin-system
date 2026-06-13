import api from './index'

export interface ClassItem {
  id: number
  classCode: string
  className: string
  major: string
  grade: number
  headTeacherName: string | null
  studentCount: number
  warningCount: number
}

export interface ClassQuery {
  grade?: number
  major?: string
  keyword?: string
}

export interface ClassInfo {
  id: number
  classCode: string
  className: string
  major: string
  grade: number
  headTeacherName: string | null
  studentCount: number
}

export interface ClassStudent {
  id: number
  studentNo: string
  name: string
  gender: number
  hasWarning: boolean
  warningLevel: string | null
}

export interface ClassStudentQuery {
  name?: string
  studentNo?: string
  hasWarning?: boolean
}

export interface CourseRanking {
  courseName: string
  averageScore: number
  passRate: number
  failCount: number
}

export interface ClassScoreStats {
  averageScore: number
  passRate: number
  failCount: number
  totalStudents: number
  distribution: Record<string, number>
  courseRankings: CourseRanking[]
}

export function fetchClasses(params?: ClassQuery): Promise<ClassItem[]> {
  return api.get('/classes', { params })
}

export function fetchClassInfo(id: number): Promise<ClassInfo> {
  return api.get(`/classes/${id}`)
}

export function fetchClassStudents(id: number, params?: ClassStudentQuery): Promise<ClassStudent[]> {
  return api.get(`/classes/${id}/students`, { params })
}

export function fetchClassScoreStats(id: number): Promise<ClassScoreStats> {
  return api.get(`/classes/${id}/score-stats`)
}
