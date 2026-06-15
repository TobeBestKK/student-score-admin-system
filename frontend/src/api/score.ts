import api from './index'

export interface ScoreRecord {
  id: number
  studentId: number
  studentName: string
  studentNo: string
  className: string
  courseId: number
  courseName: string
  scoreValue: number
  examType: string
  remark: string | null
  grade: string
  createTime: string
}

export interface ScoreRecordCreate {
  studentId: number
  courseId: number
  scoreValue: number
  examType: string
  remark?: string
}

export interface ScoreRecordUpdate {
  scoreValue?: number
  examType?: string
  remark?: string
}

export interface ScoreQuery {
  academicYear?: string
  semester?: string
  courseId?: number
  examType?: string
  keyword?: string
  page?: number
  size?: number
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export interface ScoreStats {
  totalRecords: number
  averageScore: number
  passRate: number
  failCount: number
}

export function fetchScoreRecords(params?: ScoreQuery): Promise<PageResponse<ScoreRecord>> {
  return api.get('/scores', { params })
}

export function createScoreRecord(data: ScoreRecordCreate): Promise<ScoreRecord> {
  return api.post('/scores', data)
}

export function updateScoreRecord(id: number, data: ScoreRecordUpdate): Promise<ScoreRecord> {
  return api.put(`/scores/${id}`, data)
}

export function deleteScoreRecord(id: number): Promise<void> {
  return api.delete(`/scores/${id}`)
}
