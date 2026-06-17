import api from './index'

export interface ScoreHistory {
  academicYear: string
  semester: string
  examType: string
  score: number
}

export interface WarningItem {
  type: string
  level: string
  courseName: string | null
  currentValue: string
  threshold: string
  reason: string
  history: ScoreHistory[]
}

export interface StudentWarnings {
  studentId: number
  studentNo: string
  studentName: string
  className: string
  maxLevel: string | null
  failCount: number
  warnings: WarningItem[]
}

export function fetchWarnings(params?: { grade?: number; classId?: number; level?: string; type?: string }): Promise<StudentWarnings[]> {
  return api.get('/warnings', { params })
}

export function fetchClassWarnings(classId: number): Promise<StudentWarnings[]> {
  return api.get(`/warnings/class/${classId}`)
}

export function fetchStudentWarnings(studentId: number, academicYear?: string, semester?: string): Promise<StudentWarnings> {
  return api.get('/student/warnings', { params: { studentId, academicYear, semester } })
}
